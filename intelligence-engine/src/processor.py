import pdfplumber
import re
import os
from dataclasses import dataclass, field, asdict
from typing import List, Optional
from visual_extractor import VisualExtractor


@dataclass
class Questao:
    numero: int
    texto_enunciado: str = ""
    tipo_visualizacao: str = "TEXTO"
    caminho_imagem: Optional[str] = None
    alternativas: List[str] = field(default_factory=list)
    page_index: int = 0
    bbox_top: float = 0.0
    bbox_bottom: float = 0.0
    coluna_bbox: tuple = (0, 0, 0, 0)


@dataclass
class BlocoConteudo:
    id_temp: str
    texto_base: str = ""
    caminho_imagem: Optional[str] = None
    questoes: List[Questao] = field(default_factory=list)


class PasExamProcessor:
    def __init__(self, pdf_path, output_dir):
        self.pdf_path = pdf_path
        self.output_dir = output_dir
        os.makedirs(output_dir, exist_ok=True)

        # Regex captura: (Grupo 1: Palavra "QUESTÃO" opcional), (Grupo 2: O Número)
        self.re_inicio_questao = re.compile(r'^(QUESTÃO\s+)?(\d{1,3})[.)]?\s')
        self.re_alternativa = re.compile(r'^([A-D])\s+(.*)')

        self.ultimo_numero_detectado = 0
        self.em_secao_linguas = True

    def processar(self):
        todos_blocos = []
        contador_blocos = 0
        buffer_texto_apoio = ""

        bloco_atual: Optional[BlocoConteudo] = None
        questao_em_aberto: Optional[Questao] = None

        print(f"🚀 ETAPA 1: Processando Texto de {os.path.basename(self.pdf_path)}")

        with pdfplumber.open(self.pdf_path) as pdf:
            for i, page in enumerate(pdf.pages[1:], start=2):
                real_page_index = i - 1
                w, h = page.width, page.height
                m_top, m_bot = 55, 50

                cols = [
                    (0, m_top, w / 2, h - m_bot),
                    (w / 2, m_top, w, h - m_bot)
                ]

                for col_idx, col_bbox in enumerate(cols):
                    crop_col = page.crop(col_bbox)
                    lines = crop_col.extract_text_lines(layout=True, x_tolerance=2)

                    if not lines: continue

                    for line in lines:
                        text = line['text'].strip()
                        match_q = self.re_inicio_questao.match(text)

                        # --- DETECTOU POSSÍVEL QUESTÃO ---
                        if match_q:
                            tem_palavra_chave = bool(match_q.group(1))  # Se tem "QUESTÃO" escrito
                            num_q = int(match_q.group(2))

                            # >>> VALIDAÇÃO BLINDADA <<<
                            if not self._validar_sequencia(num_q, tem_palavra_chave, text):
                                # Falso positivo: trata como texto comum do enunciado/bloco
                                if questao_em_aberto:
                                    questao_em_aberto.texto_enunciado += " " + text
                                else:
                                    buffer_texto_apoio += "\n" + text
                                continue

                            # Passou no teste! É uma questão real.
                            self.ultimo_numero_detectado = num_q

                            # Fecha a questão anterior
                            if questao_em_aberto:
                                questao_em_aberto.bbox_bottom = line['top']

                            # Novo Bloco se o buffer estiver cheio
                            if len(buffer_texto_apoio.strip()) > 10 or bloco_atual is None:
                                contador_blocos += 1
                                bloco_atual = BlocoConteudo(
                                    id_temp=f"bloco_{contador_blocos}",
                                    texto_base=self._limpar_texto(buffer_texto_apoio)
                                )
                                todos_blocos.append(bloco_atual)
                                buffer_texto_apoio = ""

                            nova_q = Questao(
                                numero=num_q,
                                texto_enunciado=text,
                                page_index=real_page_index,
                                bbox_top=line['top'],
                                coluna_bbox=col_bbox
                            )
                            bloco_atual.questoes.append(nova_q)
                            questao_em_aberto = nova_q

                        # --- CONTINUAÇÃO DE TEXTO ---
                        else:
                            if questao_em_aberto:
                                match_alt = self.re_alternativa.match(text)
                                if match_alt:
                                    questao_em_aberto.alternativas.append(text)
                                else:
                                    questao_em_aberto.texto_enunciado += " " + text
                            else:
                                buffer_texto_apoio += "\n" + text

                    if questao_em_aberto:
                        questao_em_aberto.bbox_bottom = col_bbox[3]
                        questao_em_aberto = None

        print(f"👁️ ETAPA 2: Detectando imagens (OpenCV)...")
        visual_extractor = VisualExtractor(self.pdf_path, self.output_dir)
        elementos_visuais = visual_extractor.extrair_elementos_visuais()

        self._associar_imagens(todos_blocos, elementos_visuais)

        return [asdict(b) for b in todos_blocos]

    def _associar_imagens(self, blocos, elementos_visuais):
        print(f"🧩 ETAPA 3: Associando {len(elementos_visuais)} imagens às questões...")

        for visual in elementos_visuais:
            melhor_candidato = None
            menor_distancia = float('inf')
            tipo_alvo = None

            vis_y = visual['centro_y']
            vis_x = visual['centro_x']
            vis_page = visual['page_index']

            for bloco in blocos:
                if bloco.questoes:
                    page_bloco = bloco.questoes[0].page_index
                    if page_bloco == vis_page:
                        q1 = bloco.questoes[0]
                        if vis_y < q1.bbox_top:
                            dist = abs(vis_y - q1.bbox_top)
                            if dist < menor_distancia:
                                menor_distancia = dist
                                melhor_candidato = bloco
                                tipo_alvo = 'bloco'

                for questao in bloco.questoes:
                    if questao.page_index != vis_page: continue

                    col_x0, _, col_x1, _ = questao.coluna_bbox
                    margem = 20
                    if not (col_x0 - margem < vis_x < col_x1 + margem):
                        continue

                    esta_dentro = (vis_y >= questao.bbox_top - 10) and (vis_y <= questao.bbox_bottom + 10)

                    if esta_dentro:
                        melhor_candidato = questao
                        tipo_alvo = 'questao'
                        menor_distancia = 0
                        break

                    dist = abs(vis_y - questao.bbox_bottom)
                    if dist < 60 and dist < menor_distancia:
                        menor_distancia = dist
                        melhor_candidato = questao
                        tipo_alvo = 'questao'

            if melhor_candidato and menor_distancia < 250:
                caminho = visual['caminho_imagem']
                if tipo_alvo == 'questao':
                    melhor_candidato.caminho_imagem = caminho
                    melhor_candidato.tipo_visualizacao = "MISTO"
                elif tipo_alvo == 'bloco':
                    if not melhor_candidato.caminho_imagem:
                        melhor_candidato.caminho_imagem = caminho

    def _validar_sequencia(self, num_lido, tem_palavra_chave, texto_raw):
        """
        Lógica 'Anti-Burrice' para evitar que números aleatórios virem questões.
        """
        anterior = self.ultimo_numero_detectado

        # 1. TRAVA ABSOLUTA: PAS não tem mais de 150 questões
        if num_lido > 150:
            print(f"   ⚠️ Ignorado (Muito alto): '{texto_raw}'")
            return False

        # 2. INÍCIO DA PROVA
        if anterior == 0:
            # Só aceita começar se for 1, ou se for algo baixo tipo 2, 3 (caso o 1 falhe)
            return num_lido <= 10

        # 3. MODO ESTRITO (Se NÃO tem a palavra 'QUESTÃO' escrita)
        # Ex: Achou apenas "60." -> Tem que ser exatamente anterior + 1
        if not tem_palavra_chave:
            if num_lido == anterior + 1:
                if num_lido > 10: self.em_secao_linguas = False
                return True
            else:
                # Se pulou de 60 para 255, cai aqui
                # print(f"   ⚠️ Ignorado (Fora de sequência): '{texto_raw}' (Esperava {anterior+1})")
                return False

        # 4. MODO FLEXÍVEL (Se TEM a palavra 'QUESTÃO' escrita)
        # Ex: "QUESTÃO 60" -> Aceitamos pequenos saltos (caso o OCR tenha comido uma questão)
        if num_lido > anterior:
            # Salto máximo permitido de 5 questões (pra evitar pular de 60 pra 90 do nada)
            if num_lido > anterior + 5:
                print(f"   ⚠️ Ignorado (Salto suspeito): '{texto_raw}'")
                return False
            return True

        # Reset de línguas (Espanhol -> Inglês)
        if self.em_secao_linguas and num_lido == 1:
            return True

        return False

    def _limpar_texto(self, texto):
        texto = texto.replace('\n', ' ')
        return re.sub(r'\s+', ' ', texto).strip()
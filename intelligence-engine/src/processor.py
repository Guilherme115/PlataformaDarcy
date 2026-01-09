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
    secao_detectada: int = 1


@dataclass
class BlocoConteudo:
    id_temp: str
    texto_base: str = ""
    caminho_imagem: Optional[str] = None
    questoes: List[Questao] = field(default_factory=list)
    secao_detectada: int = 1


class PasExamProcessor:
    def __init__(self, pdf_path, output_dir):
        self.pdf_path = pdf_path
        self.output_dir = output_dir
        os.makedirs(output_dir, exist_ok=True)

        self.re_inicio_questao = re.compile(r'^(QUESTÃO\s+)?(\d{1,3})([.)]|\s|$)', re.IGNORECASE)
        self.re_alternativa = re.compile(r'^([A-D])\s+(.*)')

        self.ultimo_numero_detectado = 0
        self.em_secao_linguas = True
        self.contador_secao_linguas = 1

    def processar(self, ignorar_imagens=False, verbose=False):
        todos_blocos = []
        contador_blocos = 0
        buffer_texto_apoio = ""

        bloco_atual: Optional[BlocoConteudo] = None
        questao_em_aberto: Optional[Questao] = None

        print(f"🚀 [Processor] Lendo: {os.path.basename(self.pdf_path)}")

        try:
            if not os.path.exists(self.pdf_path):
                return []

            with pdfplumber.open(self.pdf_path) as pdf:
                # === ALTERAÇÃO AQUI: PULA A CAPA (PÁGINA 1) ===
                # pdf.pages[1:] pega da segunda página em diante.
                # start=2 ajusta o log para mostrar "PÁGINA 2" corretamente.
                paginas_para_ler = pdf.pages[1:]

                if not paginas_para_ler:
                    print("❌ PDF tem apenas 1 página (capa). Nada a processar.")
                    return []

                for i, page in enumerate(paginas_para_ler, start=2):
                    if verbose: print(f"\n--- PÁGINA {i} ---")

                    # Ajusta índice real (0-based) para extração de imagens
                    real_page_index = i - 1

                    w, h = page.width, page.height
                    m_top, m_bot = 50, 50

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

                            if match_q:
                                num_q = int(match_q.group(2))
                                tem_palavra_chave = bool(match_q.group(1))

                                # 1. LÓGICA DE CICLOS (O RESET)
                                if self.em_secao_linguas and num_q == 1 and self.ultimo_numero_detectado >= 5:
                                    if self.contador_secao_linguas < 3:
                                        if verbose: print(
                                            f"   🔄 FIM DO CICLO {self.contador_secao_linguas}. INICIANDO CICLO {self.contador_secao_linguas + 1}")
                                        self.contador_secao_linguas += 1
                                        self.ultimo_numero_detectado = 0

                                # 2. VALIDAÇÃO
                                if not self._validar_sequencia(num_q, tem_palavra_chave, verbose):
                                    if questao_em_aberto:
                                        questao_em_aberto.texto_enunciado += " " + text
                                    else:
                                        buffer_texto_apoio += "\n" + text
                                    continue

                                # 3. ACEITOU
                                if verbose: print(f"   ✅ QUESTÃO {num_q} [Sessão {self.contador_secao_linguas}]")
                                secao_agora = self.contador_secao_linguas

                                # Quebra de Bloco se mudou a seção
                                if bloco_atual and secao_agora != bloco_atual.secao_detectada:
                                    todos_blocos.append(bloco_atual)
                                    bloco_atual = None

                                self.ultimo_numero_detectado = num_q

                                if questao_em_aberto:
                                    questao_em_aberto.bbox_bottom = line['top']

                                if len(buffer_texto_apoio.strip()) > 5 or bloco_atual is None:
                                    contador_blocos += 1
                                    if bloco_atual and bloco_atual not in todos_blocos:
                                        todos_blocos.append(bloco_atual)

                                    bloco_atual = BlocoConteudo(
                                        id_temp=f"bloco_{contador_blocos}",
                                        texto_base=self._limpar_texto(buffer_texto_apoio),
                                        secao_detectada=secao_agora
                                    )
                                    buffer_texto_apoio = ""
                                else:
                                    if bloco_atual: bloco_atual.secao_detectada = self.contador_secao_linguas

                                nova_q = Questao(
                                    numero=num_q,
                                    texto_enunciado=text,
                                    page_index=real_page_index,
                                    bbox_top=line['top'],
                                    coluna_bbox=col_bbox,
                                    secao_detectada=secao_agora
                                )

                                bloco_atual.questoes.append(nova_q)
                                questao_em_aberto = nova_q

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

                if bloco_atual and bloco_atual not in todos_blocos:
                    todos_blocos.append(bloco_atual)

            if not ignorar_imagens:
                print(f"👁️ Detectando imagens...")
                visual_extractor = VisualExtractor(self.pdf_path, self.output_dir)
                elementos_visuais = visual_extractor.extrair_elementos_visuais()
                self._associar_imagens(todos_blocos, elementos_visuais)

            return [asdict(b) for b in todos_blocos]

        except Exception as e:
            print(f"❌ Erro no processamento: {e}")
            import traceback
            traceback.print_exc()
            return []

    def _associar_imagens(self, blocos, elementos_visuais):
        for visual in elementos_visuais:
            melhor_candidato = None
            menor_distancia = float('inf')
            tipo_alvo = None
            vis_y = visual['centro_y']
            vis_x = visual['centro_x']
            vis_page = visual['page_index']
            for bloco in blocos:
                if not bloco.questoes: continue
                q1 = bloco.questoes[0]
                if q1.page_index == vis_page:
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
                    if not (col_x0 - margem < vis_x < col_x1 + margem): continue
                    esta_dentro = (vis_y >= questao.bbox_top - 15) and (vis_y <= questao.bbox_bottom + 15)
                    if esta_dentro:
                        melhor_candidato = questao
                        tipo_alvo = 'questao'
                        menor_distancia = 0
                        break
                    dist = abs(vis_y - questao.bbox_bottom)
                    if dist < 80 and dist < menor_distancia:
                        menor_distancia = dist
                        melhor_candidato = questao
                        tipo_alvo = 'questao'
            if melhor_candidato and menor_distancia < 300:
                caminho = visual['caminho_imagem']
                if tipo_alvo == 'questao':
                    melhor_candidato.caminho_imagem = caminho
                    melhor_candidato.tipo_visualizacao = "MISTO"
                elif tipo_alvo == 'bloco':
                    if not melhor_candidato.caminho_imagem:
                        melhor_candidato.caminho_imagem = caminho

    def _validar_sequencia(self, num_lido, tem_palavra_chave, verbose=False):
        anterior = self.ultimo_numero_detectado

        if self.em_secao_linguas:
            # Início de prova (Espanhol)
            if anterior == 0 and num_lido == 1: return True

            # Reset de ciclo (Francês/Inglês)
            if num_lido == 1 and anterior >= 5: return True

            if num_lido == anterior + 1:
                # Se ler "11"
                if num_lido > 10:
                    # Se não passou pelos 3 ciclos, REJEITA (é falso positivo na pag 2 ou 3)
                    if self.contador_secao_linguas < 3:
                        if verbose: print(
                            f"   🛡️ BLOQUEIO: Ignorando '11' falso no ciclo {self.contador_secao_linguas}")
                        return False

                        # Se já estamos no ciclo 3 (Inglês), LIBERA
                    if verbose: print(f"   🏁 Inglês completo. Liberando Parte Geral.")
                    self.em_secao_linguas = False
                    self.contador_secao_linguas = 4
                return True

            if tem_palavra_chave and num_lido > anterior and num_lido <= 10: return True
            return False

        # Parte Geral
        if num_lido > 150: return False
        if num_lido == anterior + 1: return True
        if tem_palavra_chave and num_lido > anterior and num_lido < anterior + 6: return True
        return False

    def _limpar_texto(self, texto):
        return re.sub(r'\s+', ' ', texto).strip()
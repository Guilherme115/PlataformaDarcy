import fitz  # PyMuPDF
import os
import io
from PIL import Image


class VisualExtractor:
    def __init__(self, pdf_path, output_dir):
        self.pdf_path = pdf_path
        self.output_dir = output_dir

        # Garante que a pasta existe
        if not os.path.exists(self.output_dir):
            os.makedirs(self.output_dir)

    def extrair_elementos_visuais(self):
        """
        Extrai imagens matriciais originais do PDF e retorna metadados
        compatíveis com o processor.py para vinculação com questões.
        """
        elementos_visuais = []

        try:
            doc = fitz.open(self.pdf_path)
        except Exception as e:
            print(f"❌ Erro ao abrir PDF: {e}")
            return []

        print(f"🔍 [Visual] Analisando {len(doc)} páginas via PyMuPDF...")

        for page_index in range(len(doc)):
            page = doc[page_index]
            page_height = page.rect.height

            # Pega lista de imagens da página (full=True pega imagens mascaradas também)
            image_list = page.get_images(full=True)

            for img_index, img in enumerate(image_list):
                xref = img[0]

                # --- 1. LOCALIZAÇÃO (Crucial para vincular com a questão) ---
                # Descobre ONDE a imagem está desenhada na página
                rects = page.get_image_rects(xref)

                if not rects:
                    continue  # Imagem existe no arquivo mas não aparece nesta página

                # Pega a primeira aparição da imagem (bbox = x0, y0, x1, y1)
                bbox = rects[0]

                # --- 2. FILTRO DE CABEÇALHO/RODAPÉ ---
                # Ignora logos da UnB no topo ou rodapés
                MARGEM_CABECALHO = 100
                MARGEM_RODAPE = 80

                if bbox.y1 < MARGEM_CABECALHO or bbox.y0 > (page_height - MARGEM_RODAPE):
                    continue

                # --- 3. EXTRAÇÃO E FILTROS ---
                try:
                    base_image = doc.extract_image(xref)
                    image_bytes = base_image["image"]
                    ext = base_image["ext"]

                    try:
                        pil_image = Image.open(io.BytesIO(image_bytes))
                    except:
                        # Fallback para imagens com encoding complexo (CMYK, etc)
                        pix = fitz.Pixmap(doc, xref)
                        # Se não for RGB, converte
                        if pix.n - pix.alpha > 3:
                            pix = fitz.Pixmap(fitz.csRGB, pix)
                        pil_image = Image.open(io.BytesIO(pix.tobytes()))
                        ext = "png"

                    # Filtros de Tamanho (Ignora ícones, bullet points, linhas finas)
                    w, h = pil_image.width, pil_image.height

                    if w < 60 or h < 60:
                        continue  # Muito pequena

                    ratio = w / h
                    if ratio > 15 or ratio < 0.08:
                        continue  # Muito fina ou muito larga

                    # --- 4. SALVAMENTO ---
                    # Nome padronizado: p{PAGINA}_{XREF}.{EXT}
                    nome_arquivo = f"p{page_index + 1}_{xref}.{ext}"
                    caminho_completo = os.path.join(self.output_dir, nome_arquivo)

                    pil_image.save(caminho_completo)

                    # --- 5. RETORNO PADRONIZADO ---
                    # Calculamos o centro para o processor.py saber se pertence a uma questão
                    centro_x = (bbox.x0 + bbox.x1) / 2
                    centro_y = (bbox.y0 + bbox.y1) / 2

                    elementos_visuais.append({
                        "page_index": page_index,
                        "caminho_imagem": nome_arquivo,
                        "centro_x": centro_x,
                        "centro_y": centro_y,
                        "width": w,
                        "height": h,
                        "bbox_pdf": (bbox.x0, bbox.y0, bbox.x1, bbox.y1)
                    })

                except Exception as e:
                    print(f"⚠️ Erro ao processar img XREF {xref}: {e}")
                    continue

        return elementos_visuais
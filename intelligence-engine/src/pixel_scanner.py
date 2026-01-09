import pdfplumber
import os

INPUT_DIR = "/app/input"
# Coloque o nome da prova que você quer calibrar
ARQUIVO = "pas-1-prova-2016.pdf"


def scan_pixels():
    caminho = os.path.join(INPUT_DIR, ARQUIVO)
    if not os.path.exists(caminho):
        print(f"❌ Arquivo {ARQUIVO} não encontrado!")
        return

    print(f"🔍 ESCANEANDO COORDENADAS: {ARQUIVO}\n")

    with pdfplumber.open(caminho) as pdf:
        # Analisamos as 3 primeiras páginas (onde o ruído é maior)
        for i, page in enumerate(pdf.pages[:3], start=1):
            print(f"--- PÁGINA {i} ---")

            # Simulamos as duas colunas do Cebraspe
            w = page.width
            cols = [(0, 0, w / 2, page.height), (w / 2, 0, w, page.height)]

            for idx, col_bbox in enumerate(cols):
                crop = page.crop(col_bbox)
                lines = crop.extract_text_lines(layout=True)

                for line in lines:
                    text = line['text'].strip()
                    # Filtramos apenas linhas que começam com números (1., 10, 22, etc)
                    if text and text[0].isdigit():
                        x0 = line['x0']
                        # Mostra o texto e a posição horizontal (Pixel X)
                        print(f"📍 X={x0:>6.2f} | Texto: {text[:40]}...")


if __name__ == "__main__":
    scan_pixels()
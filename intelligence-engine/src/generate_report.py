import os
import html
from processor import PasExamProcessor

INPUT_DIR = "/app/input"
OUTPUT_DIR = "/app/output"
RELATORIO_PATH = os.path.join(OUTPUT_DIR, "prova_visualizada.html")


def get_cor_secao(secao_id):
    # Retorna cor de fundo e nome da seção
    mapa = {
        1: ("#fff3cd", "🇪🇸 Espanhol"),  # Amarelo claro
        2: ("#d1ecf1", "🇫🇷 Francês"),  # Azul claro
        3: ("#f8d7da", "🇺🇸 Inglês"),  # Vermelho claro
        4: ("#ffffff", "🇧🇷 Parte Geral")  # Branco
    }
    return mapa.get(secao_id, ("#e2e3e5", "Desconhecido"))


def gerar_html():
    print("🎨 GERANDO RELATÓRIO VISUAL...")

    if not os.path.exists(INPUT_DIR):
        print("❌ Pasta input não encontrada.")
        return

    arquivos = [f for f in os.listdir(INPUT_DIR) if f.lower().endswith('.pdf')]
    if not arquivos:
        print("❌ Nenhum PDF encontrado.")
        return

    arquivo = arquivos[0]
    print(f"📄 Processando: {arquivo}")

    # Processa (sem ignorar imagens, para ver caminhos se tiver)
    processor = PasExamProcessor(os.path.join(INPUT_DIR, arquivo), OUTPUT_DIR)
    dados = processor.processar(ignorar_imagens=True, verbose=True)

    # --- MONTAGEM DO HTML ---
    html_content = f"""
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <title>Debug Prova: {arquivo}</title>
        <style>
            body {{ font-family: Arial, sans-serif; background-color: #f0f0f0; padding: 20px; }}
            .container {{ max-width: 900px; margin: 0 auto; }}
            .bloco {{ 
                background: white; 
                border: 2px solid #333; 
                border-radius: 8px; 
                margin-bottom: 30px; 
                padding: 15px;
                box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            }}
            .bloco-header {{
                font-size: 12px;
                color: #666;
                text-transform: uppercase;
                margin-bottom: 10px;
                border-bottom: 1px solid #eee;
                padding-bottom: 5px;
            }}
            .texto-apoio {{
                font-family: 'Georgia', serif;
                font-size: 16px;
                line-height: 1.6;
                background-color: #fafafa;
                padding: 10px;
                border-left: 5px solid #666;
                white-space: pre-wrap; /* Mantém quebras de linha */
            }}
            .questao {{
                margin-top: 15px;
                padding: 10px;
                border: 1px solid #ddd;
                border-radius: 5px;
            }}
            .badge {{
                display: inline-block;
                padding: 5px 10px;
                border-radius: 15px;
                font-weight: bold;
                font-size: 14px;
                margin-bottom: 10px;
            }}
            .alternativa {{ margin-left: 20px; color: #555; }}
            .aviso-imagem {{ color: red; font-weight: bold; font-size: 12px; }}
        </style>
    </head>
    <body>
        <div class="container">
            <h1>Relatório de Extração: {arquivo}</h1>
            <p>Este relatório mostra como o algoritmo agrupou Textos e Questões.</p>
            <hr>
    """

    for i, bloco in enumerate(dados):
        texto = html.escape(bloco['texto_base'])
        secao_id = bloco['secao_detectada']
        cor_fundo, nome_secao = get_cor_secao(secao_id)

        # Adiciona o card do bloco
        html_content += f"""
        <div class="bloco">
            <div class="bloco-header">
                BLOCO #{i + 1} • <span class="badge" style="background-color:{cor_fundo}">{nome_secao}</span>
            </div>
        """

        # Se tiver texto de apoio
        if texto.strip():
            html_content += f"<div class='texto-apoio'>{texto}</div>"
        else:
            html_content += "<div class='texto-apoio' style='color:#999'>[Sem texto de apoio - Questões Soltas]</div>"

        # Lista as questões deste bloco
        for q in bloco['questoes']:
            enunciado = html.escape(q['texto_enunciado'])
            num = q['numero']
            # Verifica se a seção da questão bate com a do bloco (para debug visual)
            cor_q, nome_q = get_cor_secao(q['secao_detectada'])

            style_q = f"background-color: {cor_q};"

            html_content += f"""
            <div class="questao" style="{style_q}">
                <strong>QUESTÃO {num}</strong> ({nome_q})<br>
                {enunciado}
            """

            if q['alternativas']:
                html_content += "<ul>"
                for alt in q['alternativas']:
                    html_content += f"<li class='alternativa'>{html.escape(alt)}</li>"
                html_content += "</ul>"

            if q['caminho_imagem']:
                html_content += f"<div class='aviso-imagem'>🖼️ Contém Imagem: {q['caminho_imagem']}</div>"

            html_content += "</div>"  # fecha questao

        html_content += "</div>"  # fecha bloco

    html_content += """
        </div>
    </body>
    </html>
    """

    with open(RELATORIO_PATH, "w", encoding="utf-8") as f:
        f.write(html_content)

    print(f"\n✅ RELATÓRIO CRIADO COM SUCESSO!")
    print(f"📂 Caminho no Docker: {RELATORIO_PATH}")
    print(f"👉 Abra este arquivo no seu navegador para conferir o texto.")


if __name__ == "__main__":
    gerar_html()
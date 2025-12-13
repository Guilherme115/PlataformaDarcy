import json
import os
import re
from processor import PasExamProcessor
from db_saver import DatabaseSaver

# Caminhos no Docker
INPUT_DIR = "/app/input"
OUTPUT_DIR = "/app/output"


def extrair_info_arquivo(nome_arquivo):
    """
    Tenta extrair Etapa e Ano do nome do arquivo.
    Padrões esperados: 'pas-1-prova-2013.pdf' ou 'PAS_2_2015.pdf'
    """
    # Regex flexível: procura um dígito isolado (etapa) e 4 dígitos (ano)
    match_etapa = re.search(r'[\-_ ]([123])[\-_ ]', nome_arquivo)
    match_ano = re.search(r'(20[12]\d)', nome_arquivo)

    if match_etapa and match_ano:
        return int(match_etapa.group(1)), int(match_ano.group(1))

    # Valores padrão se o nome do arquivo for ruim (você pode editar aqui)
    print(f"⚠️ Aviso: Não consegui detectar ano/etapa no arquivo '{nome_arquivo}'. Usando padrão.")
    return 1, 2013


def main():
    if not os.path.exists(INPUT_DIR):
        print(f"❌ Pasta de entrada não encontrada: {INPUT_DIR}")
        return

    # Garante saída
    if not os.path.exists(OUTPUT_DIR):
        os.makedirs(OUTPUT_DIR)

    # Varre todos os PDFs da pasta input
    arquivos = [f for f in os.listdir(INPUT_DIR) if f.lower().endswith('.pdf')]

    if not arquivos:
        print("❌ Nenhum PDF encontrado na pasta input.")
        return

    print(f"📂 Encontrados {len(arquivos)} arquivos para processar.")

    db = DatabaseSaver()

    for arquivo in arquivos:
        caminho_completo = os.path.join(INPUT_DIR, arquivo)

        # 1. Detectar Metadados (Ano e Etapa)
        etapa, ano = extrair_info_arquivo(arquivo)
        print(f"\n==========================================")
        print(f"🚀 Iniciando: {arquivo} | PAS {etapa} | {ano}")
        print(f"==========================================")

        # 2. Processar PDF (Texto + Imagens)
        # Cria uma subpasta para as imagens não misturarem (ex: output/2013_1/)
        pasta_imagens_prova = os.path.join(OUTPUT_DIR, f"{ano}_{etapa}")
        if not os.path.exists(pasta_imagens_prova):
            os.makedirs(pasta_imagens_prova)

        parser = PasExamProcessor(pdf_path=caminho_completo, output_dir=pasta_imagens_prova)
        dados = parser.processar()

        # 3. Salvar no Banco
        try:
            # Passamos os metadados para o banco
            db.salvar_dados(dados, ano, etapa, arquivo)
        except Exception as e:
            print(f"❌ Erro ao salvar {arquivo}: {e}")

    print("\n🏁 TODOS OS PROCESSOS CONCLUÍDOS!")


if __name__ == "__main__":
    main()
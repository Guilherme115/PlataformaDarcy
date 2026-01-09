import os
import json
from processor import PasExamProcessor

# Configurações de teste
INPUT_DIR = "/app/input"
OUTPUT_DIR = "/app/output/debug"


def run_deep_debug():
    print("🕵️ INICIANDO INSPEÇÃO PROFUNDA - FOCO: PAS 1 2024")

    if not os.path.exists(INPUT_DIR):
        print("❌ Pasta input não existe.")
        return

    if not os.path.exists(OUTPUT_DIR):
        os.makedirs(OUTPUT_DIR)

    # Lista todos os arquivos para conferência
    todos_arquivos = [f for f in os.listdir(INPUT_DIR) if f.lower().endswith('.pdf')]

    # --- MUDANÇA AQUI: BUSCA O ARQUIVO ESPECÍFICO ---
    arquivo_alvo = "pas-1-prova-2024.pdf"

    if arquivo_alvo not in todos_arquivos:
        print(f"❌ Erro: O arquivo '{arquivo_alvo}' não foi encontrado na pasta input.")
        print(f"Arquivos detectados na pasta: {todos_arquivos}")
        return

    caminho_pdf = os.path.join(INPUT_DIR, arquivo_alvo)

    print(f"📄 Analisando: {arquivo_alvo}")
    print(f"--------------------------------------------------")

    # Executa o processador com VERBOSE para vermos as linhas e gatilhos no terminal
    processor = PasExamProcessor(caminho_pdf, OUTPUT_DIR)
    dados = processor.processar(ignorar_imagens=True, verbose=True)

    print(f"\n✅ FIM DA LEITURA.")

    # Resumo Final para conferência de Seções
    total_questoes = 0
    questoes_por_disciplina = {}
    mapa_secoes = {1: "Espanhol", 2: "Francês", 3: "Inglês", 4: "Geral"}

    for bloco in dados:
        secao_bloco = bloco.get('secao_detectada', 4)
        nome_disc = mapa_secoes.get(secao_bloco, f"Seção {secao_bloco}")

        if nome_disc not in questoes_por_disciplina:
            questoes_por_disciplina[nome_disc] = []

        for q in bloco['questoes']:
            questoes_por_disciplina[nome_disc].append(q['numero'])
            total_questoes += 1

    print(f"\n📊 RESUMO DA EXTRAÇÃO (PAS 1 2024):")
    for disc in ["Espanhol", "Francês", "Inglês", "Geral"]:
        if disc in questoes_por_disciplina:
            nums = sorted(list(set(questoes_por_disciplina[disc])))
            print(f"   👉 {disc}: {len(nums)} questões encontradas. IDs: {nums}")
        else:
            print(f"   ⚠️ {disc}: NENHUMA questão detectada!")

    # Exporta o JSON para inspeção detalhada se precisar
    json_path = os.path.join(OUTPUT_DIR, "debug_2024_result.json")
    with open(json_path, 'w', encoding='utf-8') as f:
        json.dump(dados, f, indent=2, ensure_ascii=False)

    print(f"\n💾 Arquivo JSON exportado para conferência: {json_path}")


if __name__ == "__main__":
    run_deep_debug()
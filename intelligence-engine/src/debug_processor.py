import os
import json
from processor import PasExamProcessor

# Configurações de teste
INPUT_DIR = "/app/input"
OUTPUT_DIR = "/app/output/debug"


def debug_run():
    print("🐞 INICIANDO DEBUG DO PROCESSADOR (MODO RÁPIDO)...")

    if not os.path.exists(INPUT_DIR):
        print("❌ Pasta input não existe.")
        return

    if not os.path.exists(OUTPUT_DIR):
        os.makedirs(OUTPUT_DIR)

    # Pega o primeiro PDF da lista
    arquivos = [f for f in os.listdir(INPUT_DIR) if f.lower().endswith('.pdf')]
    if not arquivos:
        print("❌ Nenhum PDF na pasta input.")
        return

    arquivo_teste = arquivos[0]
    caminho_pdf = os.path.join(INPUT_DIR, arquivo_teste)

    print(f"📄 Testando com o arquivo: {arquivo_teste}")
    print(f"--------------------------------------------------")

    # Instancia e processa IGNORANDO IMAGENS para ser rápido
    processor = PasExamProcessor(caminho_pdf, OUTPUT_DIR)
    dados = processor.processar(ignorar_imagens=True)

    # --- RELATÓRIO DO DEBUG ---
    print(f"\n✅ PROCESSAMENTO FINALIZADO.")
    print(f"📦 Total de Blocos extraídos: {len(dados)}")

    total_questoes = 0
    questoes_por_disciplina = {}

    mapa_secoes = {
        1: "Espanhol (Provável)",
        2: "Francês (Provável)",
        3: "Inglês (Provável)",
        4: "Parte Geral"
    }

    # === CONTAGEM CIRÚRGICA (QUESTÃO POR QUESTÃO) ===
    # Agora olhamos para a propriedade 'secao_detectada' de CADA questão individual
    for bloco in dados:
        for q in bloco['questoes']:
            secao_id = q.get('secao_detectada', 4)
            nome_disc = mapa_secoes.get(secao_id, f"Seção {secao_id}")

            # CORREÇÃO AQUI (removido o acento de 'questões')
            if nome_disc not in questoes_por_disciplina:
                questoes_por_disciplina[nome_disc] = []

            questoes_por_disciplina[nome_disc].append(q['numero'])
            total_questoes += 1

    print(f"❓ Total de Questões extraídas: {total_questoes}")
    print(f"\n📊 DISTRIBUIÇÃO POR DISCIPLINA (Real):")

    # Ordena para mostrar 1 -> 2 -> 3 -> 4
    chaves_ordenadas = sorted(questoes_por_disciplina.keys())

    for disc in chaves_ordenadas:
        nums = questoes_por_disciplina[disc]
        nums.sort()
        if nums:
            print(f"   👉 {disc}: {len(nums)} questões (De {min(nums)} até {max(nums)})")
            # Se for língua, imprime a lista para conferir se pegou todas (1..10)
            if "Provável" in disc:
                print(f"      [IDs]: {nums}")
        else:
            print(f"   👉 {disc}: Nenhuma questão.")

    # Salva um JSON de debug
    json_path = os.path.join(OUTPUT_DIR, "debug_result.json")
    with open(json_path, 'w', encoding='utf-8') as f:
        json.dump(dados, f, indent=2, ensure_ascii=False)

    print(f"\n💾 JSON completo salvo em: {json_path}")


if __name__ == "__main__":
    debug_run()
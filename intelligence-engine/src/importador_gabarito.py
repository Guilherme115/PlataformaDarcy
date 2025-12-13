import mysql.connector
import re
import sys

# Configuração do Banco
db_config = {'user': 'root', 'password': 'root', 'host': 'localhost', 'database': 'pas_db'}


def detectar_tipo(resposta):
    resposta = resposta.upper().strip()
    if re.match(r'^\d+$', resposta): return 'B'  # Numérica
    if resposta in ['A', 'B', 'D']: return 'C'  # Múltipla (Cuidado com 'C' de Certo)
    return 'A'  # Padrão (Certo/Errado/Anulada)


def processar_arquivo(caminho):
    print(f"📂 Lendo gabaritos de: {caminho}")
    conn = mysql.connector.connect(**db_config)
    cursor = conn.cursor()

    with open(caminho, 'r', encoding='utf-8') as f:
        linhas = f.readlines()

    for linha in linhas:
        if ";" not in linha: continue
        cabecalho, dados = linha.split(";")
        match = re.match(r'PAS(\d)(\d{4})', cabecalho.strip().upper())
        if not match: continue

        etapa, ano = int(match.group(1)), int(match.group(2))
        respostas = dados.split(",")

        # Busca ID da Prova
        cursor.execute("SELECT id FROM provas WHERE ano = %s AND etapa = %s", (ano, etapa))
        prova = cursor.fetchone()
        if not prova: continue
        prova_id = prova[0]

        for item in respostas:
            if "-" not in item: continue
            num, gab = item.strip().split("-")

            tipo = detectar_tipo(gab)

            # Se for Tipo C e a resposta for 'C', o script pode confundir com Certo.
            # Ajuste manual ou lógica extra aqui se necessário.

            sql = "UPDATE questoes SET gabarito = %s, tipo = %s WHERE prova_id = %s AND numero = %s"
            cursor.execute(sql, (gab.strip(), tipo, prova_id, num.strip()))

        conn.commit()
        print(f"✅ PAS {etapa}/{ano} atualizado.")
    conn.close()


if __name__ == "__main__":
    processar_arquivo("gabaritos.txt")
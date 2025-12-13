import mysql.connector
import json
import os

class DatabaseSaver:
    def __init__(self):
        # Configuração Docker
        self.config = {
            'user': 'root',
            'password': 'root',
            'host': 'db',        # Nome do serviço no docker-compose
            'database': 'pas_db',
            'raise_on_warnings': False # IMPEDE O ERRO SE A TABELA JÁ EXISTIR
        }

    def salvar_dados(self, lista_blocos, ano, etapa, nome_pdf):
        print(f"💾 Salvando dados para PAS {etapa} - Ano {ano}...")

        conexao = None
        try:
            conexao = mysql.connector.connect(**self.config)
            cursor = conexao.cursor()

            # --- SEGURANÇA: Garante que as tabelas existem com BIGINT ---
            self._criar_tabelas(cursor)

            # 1. CRIAR OU RECUPERAR A PROVA
            sql_check = "SELECT id FROM provas WHERE ano = %s AND etapa = %s"
            cursor.execute(sql_check, (ano, etapa))
            resultado = cursor.fetchone()

            if resultado:
                prova_id = resultado[0]
                print(f"   -> Prova já existe (ID: {prova_id}). Adicionando novos dados...")
            else:
                sql_insert_prova = "INSERT INTO provas (ano, etapa, nome_arquivo_pdf) VALUES (%s, %s, %s)"
                cursor.execute(sql_insert_prova, (ano, etapa, nome_pdf))
                prova_id = cursor.lastrowid
                print(f"   -> Nova prova criada (ID: {prova_id}).")

            # 2. SALVAR BLOCOS E QUESTÕES
            for bloco in lista_blocos:
                img_bloco = os.path.basename(bloco['caminho_imagem']) if bloco['caminho_imagem'] else None

                # Insere Bloco
                sql_bloco = "INSERT INTO blocos (texto_base, caminho_imagem, prova_id) VALUES (%s, %s, %s)"
                cursor.execute(sql_bloco, (bloco['texto_base'], img_bloco, prova_id))
                bloco_id = cursor.lastrowid

                for q in bloco['questoes']:
                    alts_json = json.dumps(q['alternativas'], ensure_ascii=False)

                    # Insere Questão (COM STATUS 'PENDENTE')
                    sql_questao = """
                        INSERT INTO questoes (numero, enunciado, alternativas, status, bloco_id, prova_id)
                        VALUES (%s, %s, %s, 'PENDENTE', %s, %s)
                    """
                    val_questao = (q['numero'], q['texto_enunciado'], alts_json, bloco_id, prova_id)
                    cursor.execute(sql_questao, val_questao)
                    questao_id = cursor.lastrowid

                    # Insere Imagem da Questão
                    if q['caminho_imagem']:
                        img_q = os.path.basename(q['caminho_imagem'])
                        sql_img = "INSERT INTO imagens_questoes (caminho_arquivo, questao_id) VALUES (%s, %s)"
                        cursor.execute(sql_img, (img_q, questao_id))

            conexao.commit()
            print("✅ Dados salvos com sucesso!")

        except mysql.connector.Error as err:
            print(f"❌ Erro de Banco de Dados: {err}")
        finally:
            if conexao and conexao.is_connected():
                cursor.close()
                conexao.close()

    def _criar_tabelas(self, cursor):
        """
        Cria as tabelas se não existirem, usando BIGINT para compatibilidade com Java (Long).
        """
        # 1. Tabela PROVAS
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS provas (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                ano INT NOT NULL,
                etapa INT NOT NULL,
                nome_arquivo_pdf VARCHAR(255),
                CONSTRAINT uk_prova UNIQUE (ano, etapa)
            )
        """)

        # 2. Tabela BLOCOS
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS blocos (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                texto_base TEXT,
                caminho_imagem VARCHAR(255),
                prova_id BIGINT NOT NULL,
                FOREIGN KEY (prova_id) REFERENCES provas(id) ON DELETE CASCADE
            )
        """)

        # 3. Tabela QUESTOES (Com STATUS)
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS questoes (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                numero INT NOT NULL,
                enunciado TEXT,
                alternativas TEXT,
                status VARCHAR(20) DEFAULT 'PENDENTE',
                bloco_id BIGINT NOT NULL,
                prova_id BIGINT NOT NULL,
                FOREIGN KEY (bloco_id) REFERENCES blocos(id) ON DELETE CASCADE,
                FOREIGN KEY (prova_id) REFERENCES provas(id) ON DELETE CASCADE
            )
        """)

        # 4. Tabela IMAGENS_QUESTOES
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS imagens_questoes (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                caminho_arquivo VARCHAR(255),
                questao_id BIGINT NOT NULL,
                FOREIGN KEY (questao_id) REFERENCES questoes(id) ON DELETE CASCADE
            )
        """)
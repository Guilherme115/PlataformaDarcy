import mysql.connector
import json
import os



class DatabaseSaver:
        def __init__(self):
            # Configuração Docker (ajuste host/pass se necessário)
            self.config = {
                'user': 'root',
                'password': 'root',
                'host': 'db',
                'database': 'pas_db',
                'charset': 'utf8mb4',
                'raise_on_warnings': True
            }
            # Mapeamento para a coluna 'disciplina' da tabela 'blocos'
            self.mapa_disciplinas = {
                1: "ESPANHOL",
                2: "FRANCÊS",
                3: "INGLÊS",
                4: "GERAL"
            }

        def _get_connection(self):
            return mysql.connector.connect(**self.config)

        def salvar_dados(self, lista_blocos, ano, etapa, nome_pdf):
            print(f"💾 Salvando dados no banco: PAS {etapa} ({ano})...")

            conexao = self._get_connection()
            cursor = conexao.cursor(dictionary=True)

            try:
                # 0. VERIFICAR E DELETAR PROVA EXISTENTE (evita duplicatas ao reprocessar)
                cursor.execute("SELECT id FROM provas WHERE ano = %s AND etapa = %s", (ano, etapa))
                prova_existente = cursor.fetchone()
                
                if prova_existente:
                    prova_id_antiga = prova_existente['id']
                    print(f"   ⚠️ Prova existente encontrada (ID: {prova_id_antiga}). Deletando dados antigos...")
                    
                    # Deleta em cascata: imagens -> questões -> blocos -> prova
                    cursor.execute("DELETE FROM imagens_questoes WHERE questao_id IN (SELECT id FROM questoes WHERE prova_id = %s)", (prova_id_antiga,))
                    cursor.execute("DELETE FROM questoes WHERE prova_id = %s", (prova_id_antiga,))
                    cursor.execute("DELETE FROM blocos WHERE prova_id = %s", (prova_id_antiga,))
                    cursor.execute("DELETE FROM provas WHERE id = %s", (prova_id_antiga,))
                    conexao.commit()
                    print(f"   ✅ Dados antigos removidos com sucesso!")

                # 1. CRIAR A PROVA
                sql_prova = """
                            INSERT INTO provas (ano, etapa, nome_arquivo_pdf, titulo, origem)
                            VALUES (%s, %s, %s, %s, 'PDF_INGESTAO')
                            """
                titulo_prova = f"PAS {etapa} - {ano}"
                cursor.execute(sql_prova, (ano, etapa, nome_pdf, titulo_prova))
                prova_id = cursor.lastrowid

                print(f"   -> Prova ID: {prova_id}")

                # 2. SALVAR BLOCOS E QUESTÕES
                for bloco in lista_blocos:
                    disciplina = self.mapa_disciplinas.get(bloco.get('secao_detectada'), "GERAL")
                    img_bloco = os.path.basename(bloco['caminho_imagem']) if bloco['caminho_imagem'] else None

                    # Inserir Bloco
                    sql_bloco = """
                                INSERT INTO blocos (texto_base, caminho_imagem, prova_id, disciplina)
                                VALUES (%s, %s, %s, %s) \
                                """
                    cursor.execute(sql_bloco, (bloco['texto_base'], img_bloco, prova_id, disciplina))
                    bloco_id = cursor.lastrowid

                    for q in bloco['questoes']:
                        # Identificar Tipo: Se tem alternativas, é C (Múltipla Escolha). Se não, é A.
                        tipo_q = 'C' if (q['alternativas'] and len(q['alternativas']) > 0) else 'A'
                        alts_json = json.dumps(q['alternativas'], ensure_ascii=False)
                        
                        # Pré-popular tags com a disciplina do bloco
                        tags_iniciais = disciplina if disciplina != "GERAL" else None

                        # Inserir Questão
                        sql_questao = """
                                      INSERT INTO questoes (numero, enunciado, alternativas, status, tipo, tags, bloco_id, prova_id)
                                      VALUES (%s, %s, %s, 'PENDENTE', %s, %s, %s, %s) \
                                      """
                        cursor.execute(sql_questao, (
                            q['numero'],
                            q['texto_enunciado'],
                            alts_json,
                            tipo_q,
                            tags_iniciais,
                            bloco_id,
                            prova_id
                        ))
                        questao_id = cursor.lastrowid

                        # Inserir Imagem da Questão (se houver)
                        if q['caminho_imagem']:
                            img_q = os.path.basename(q['caminho_imagem'])
                            sql_img = "INSERT INTO imagens_questoes (caminho_arquivo, tag, questao_id) VALUES (%s, %s, %s)"
                            cursor.execute(sql_img, (img_q, 'ENUNCIADO', questao_id))

                conexao.commit()
                print(f"✅ Sucesso! {len(lista_blocos)} blocos integrados ao acervo.")

            except mysql.connector.Error as err:
                print(f"❌ Erro de Banco de Dados: {err}")
                conexao.rollback()
            finally:
                cursor.close()
                conexao.close()
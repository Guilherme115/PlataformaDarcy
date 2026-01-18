package com.example.PlataformaDarcy.service;

import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaxonomyService {

        private final Map<String, Map<String, List<String>>> taxonomy;

        public TaxonomyService() {
                taxonomy = new LinkedHashMap<>();

                // ==================== LÍNGUAS ESTRANGEIRAS ====================
                taxonomy.put("Espanhol", Map.of(
                                "Interpretação", List.of("Compreensão Textual", "Vocabulário", "Contexto Cultural"),
                                "Gramática", List.of("Verbos", "Pronomes", "Artigos", "Preposições", "Conectivos"),
                                "Cultura Hispânica", List.of("Literatura", "História", "Costumes")));

                taxonomy.put("Francês", Map.of(
                                "Interpretação", List.of("Compreensão Textual", "Vocabulário", "Contexto Cultural"),
                                "Gramática", List.of("Verbos", "Pronomes", "Artigos", "Preposições", "Conectivos"),
                                "Cultura Francófona", List.of("Literatura", "História", "Países Francófonos")));

                taxonomy.put("Inglês", Map.of(
                                "Interpretação", List.of("Compreensão Textual", "Vocabulário", "Contexto Cultural"),
                                "Gramática",
                                List.of("Verb Tenses", "Pronouns", "Articles", "Prepositions", "Connectors"),
                                "Cultura Anglófona", List.of("Literatura", "História", "Países Anglófonos")));

                // ==================== PORTUGUÊS ====================
                taxonomy.put("Português", Map.of(
                                "Gramática",
                                List.of("Fonética", "Morfologia", "Sintaxe", "Concordância", "Regência", "Crase",
                                                "Pontuação"),
                                "Interpretação",
                                List.of("Gêneros Textuais", "Figuras de Linguagem", "Coesão e Coerência",
                                                "Funções da Linguagem"),
                                "Literatura Brasileira",
                                List.of("Quinhentismo", "Barroco", "Arcadismo", "Romantismo", "Realismo", "Modernismo",
                                                "Contemporânea"),
                                "Literatura Portuguesa",
                                List.of("Trovadorismo", "Classicismo", "Barroco", "Romantismo", "Fernando Pessoa"),
                                "Redação",
                                List.of("Dissertação-Argumentativa", "Proposta de Intervenção", "Estrutura Textual")));

                // ==================== MATEMÁTICA (11 áreas - usar LinkedHashMap)
                // ====================
                Map<String, List<String>> matematica = new LinkedHashMap<>();
                matematica.put("Matemática Básica", List.of("Conjuntos", "Frações", "Potenciação", "Radiciação",
                                "Razão e Proporção", "Regra de Três", "Porcentagem"));
                matematica.put("Funções", List.of("Conceito de Função", "Função Afim", "Função Quadrática",
                                "Função Modular", "Função Exponencial", "Função Logarítmica"));
                matematica.put("Sequências", List.of("Progressão Aritmética (PA)", "Progressão Geométrica (PG)",
                                "Soma de PA", "Soma de PG"));
                matematica.put("Trigonometria", List.of("Ciclo Trigonométrico", "Funções Trigonométricas",
                                "Identidades", "Equações Trigonométricas"));
                matematica.put("Geometria Plana", List.of("Ângulos", "Triângulos", "Semelhança", "Teorema de Pitágoras",
                                "Circunferência", "Áreas"));
                matematica.put("Geometria Espacial",
                                List.of("Poliedros", "Prismas", "Pirâmides", "Cilindros", "Cones", "Esferas"));
                matematica.put("Geometria Analítica", List.of("Ponto e Reta", "Circunferência", "Cônicas"));
                matematica.put("Estatística", List.of("Média", "Moda", "Mediana", "Desvio Padrão", "Gráficos"));
                matematica.put("Probabilidade",
                                List.of("Espaço Amostral", "Probabilidade Simples", "Probabilidade Condicional"));
                matematica.put("Análise Combinatória",
                                List.of("Princípio Fundamental", "Arranjo", "Permutação", "Combinação"));
                matematica.put("Álgebra Linear", List.of("Matrizes", "Determinantes", "Sistemas Lineares"));
                taxonomy.put("Matemática", matematica);

                // ==================== FÍSICA (18 áreas - usar LinkedHashMap)
                // ====================
                Map<String, List<String>> fisica = new LinkedHashMap<>();
                fisica.put("Cinemática", List.of("MRU", "MRUV", "Queda Livre", "Lançamento Oblíquo", "MCU", "Vetores"));
                fisica.put("Dinâmica",
                                List.of("Leis de Newton", "Força de Atrito", "Plano Inclinado", "Força Elástica"));
                fisica.put("Energia e Trabalho", List.of("Trabalho", "Potência", "Energia Cinética",
                                "Energia Potencial", "Conservação"));
                fisica.put("Impulso e Momentum", List.of("Impulso", "Quantidade de Movimento", "Colisões"));
                fisica.put("Gravitação", List.of("Lei da Gravitação Universal", "Leis de Kepler", "Satélites"));
                fisica.put("Estática", List.of("Equilíbrio de Corpos", "Momento de Força", "Alavancas"));
                fisica.put("Hidrostática", List.of("Pressão", "Pascal", "Arquimedes", "Empuxo"));
                fisica.put("Termologia", List.of("Temperatura", "Escalas", "Dilatação", "Calorimetria"));
                fisica.put("Termodinâmica", List.of("Leis da Termodinâmica", "Gases Ideais", "Máquinas Térmicas"));
                fisica.put("Óptica", List.of("Reflexão", "Refração", "Espelhos", "Lentes"));
                fisica.put("Ondulatória", List.of("Ondas Mecânicas", "Eletromagnéticas", "Difração", "Interferência"));
                fisica.put("Acústica", List.of("Som", "Efeito Doppler", "Cordas Vibrantes"));
                fisica.put("Eletrostática", List.of("Lei de Coulomb", "Campo Elétrico", "Potencial Elétrico"));
                fisica.put("Eletrodinâmica",
                                List.of("Corrente Elétrica", "Leis de Ohm", "Resistores", "Potência Elétrica"));
                fisica.put("Magnetismo", List.of("Campo Magnético", "Força Magnética", "Lei de Ampère"));
                fisica.put("Eletromagnetismo", List.of("Indução", "Lei de Faraday", "Lei de Lenz", "Transformadores"));
                fisica.put("Física Moderna", List.of("Relatividade", "Efeito Fotoelétrico", "Radioatividade"));
                fisica.put("Astronomia", List.of("Sistema Solar", "Estrelas", "Galáxias", "Cosmologia"));
                taxonomy.put("Física", fisica);

                // ==================== QUÍMICA (13 áreas - usar LinkedHashMap)
                // ====================
                Map<String, List<String>> quimica = new LinkedHashMap<>();
                quimica.put("Química Geral",
                                List.of("Propriedades da Matéria", "Estados Físicos", "Misturas", "Separação"));
                quimica.put("Atomística", List.of("Modelos Atômicos", "Distribuição Eletrônica"));
                quimica.put("Tabela Periódica", List.of("Organização", "Propriedades Periódicas"));
                quimica.put("Ligações Químicas",
                                List.of("Iônica", "Covalente", "Metálica", "Geometria Molecular", "Polaridade"));
                quimica.put("Funções Inorgânicas", List.of("Ácidos", "Bases", "Sais", "Óxidos"));
                quimica.put("Estequiometria", List.of("Mol", "Massa Molar", "Relações Estequiométricas"));
                quimica.put("Soluções", List.of("Concentração", "Diluição", "Propriedades Coligativas"));
                quimica.put("Termoquímica", List.of("Entalpia", "Lei de Hess", "Energia de Ligação"));
                quimica.put("Cinética Química", List.of("Velocidade de Reação", "Fatores", "Catalisadores"));
                quimica.put("Equilíbrio Químico", List.of("Constante de Equilíbrio", "Le Chatelier", "pH e pOH"));
                quimica.put("Eletroquímica", List.of("Pilhas", "Eletrólise", "Potencial de Redução"));
                quimica.put("Química Orgânica", List.of("Hidrocarbonetos", "Funções Oxigenadas", "Funções Nitrogenadas",
                                "Isomeria", "Polímeros"));
                quimica.put("Química Ambiental", List.of("Poluição", "Efeito Estufa", "Camada de Ozônio"));
                taxonomy.put("Química", quimica);

                // ==================== BIOLOGIA (18 áreas - usar LinkedHashMap)
                // ====================
                Map<String, List<String>> biologia = new LinkedHashMap<>();
                biologia.put("Bioquímica",
                                List.of("Água", "Carboidratos", "Lipídios", "Proteínas", "Ácidos Nucleicos"));
                biologia.put("Citologia", List.of("Membrana", "Organelas", "Núcleo", "Mitose", "Meiose"));
                biologia.put("Metabolismo", List.of("Respiração Celular", "Fotossíntese", "Fermentação"));
                biologia.put("Histologia", List.of("Tecido Epitelial", "Conjuntivo", "Muscular", "Nervoso"));
                biologia.put("Fisiologia Humana", List.of("Digestório", "Respiratório", "Circulatório", "Excretor",
                                "Nervoso", "Endócrino"));
                biologia.put("Embriologia", List.of("Gametogênese", "Fecundação", "Segmentação", "Gastrulação"));
                biologia.put("Genética", List.of("Leis de Mendel", "Herança ao Sexo", "Sistema ABO", "Fator Rh"));
                biologia.put("Biologia Molecular", List.of("DNA", "RNA", "Replicação", "Transcrição", "Tradução"));
                biologia.put("Biotecnologia", List.of("Engenharia Genética", "Clonagem", "Transgênicos"));
                biologia.put("Evolução", List.of("Lamarckismo", "Darwinismo", "Especiação"));
                biologia.put("Ecologia",
                                List.of("Cadeia Alimentar", "Ciclos Biogeoquímicos", "Relações Ecológicas", "Biomas"));
                biologia.put("Taxonomia", List.of("Classificação", "Nomenclatura"));
                biologia.put("Vírus", List.of("Estrutura", "Ciclos", "Viroses"));
                biologia.put("Monera", List.of("Bactérias", "Bacterioses"));
                biologia.put("Protista", List.of("Protozoários", "Protozooses", "Algas"));
                biologia.put("Fungi", List.of("Classificação", "Micoses"));
                biologia.put("Plantae", List.of("Briófitas", "Pteridófitas", "Gimnospermas", "Angiospermas",
                                "Fisiologia Vegetal"));
                biologia.put("Animalia", List.of("Invertebrados", "Vertebrados"));
                taxonomy.put("Biologia", biologia);

                // ==================== HISTÓRIA (11 áreas - usar LinkedHashMap)
                // ====================
                Map<String, List<String>> historia = new LinkedHashMap<>();
                historia.put("Antiguidade Oriental", List.of("Mesopotâmia", "Egito", "Hebreus", "Fenícios"));
                historia.put("Antiguidade Clássica", List.of("Grécia Antiga", "Roma Antiga"));
                historia.put("Idade Média", List.of("Feudalismo", "Igreja Medieval", "Cruzadas"));
                historia.put("Idade Moderna",
                                List.of("Renascimento", "Reformas Religiosas", "Absolutismo", "Iluminismo"));
                historia.put("Revoluções", List.of("Revolução Francesa", "Revolução Industrial", "Independência EUA"));
                historia.put("Século XIX", List.of("Imperialismo", "Unificações"));
                historia.put("Século XX", List.of("1ª Guerra", "2ª Guerra", "Guerra Fria"));
                historia.put("Contemporânea", List.of("Globalização", "Conflitos Atuais"));
                historia.put("Brasil Colônia", List.of("Capitanias", "Economia Colonial", "Mineração", "Revoltas"));
                historia.put("Brasil Império", List.of("Independência", "1º Reinado", "2º Reinado", "Abolição"));
                historia.put("Brasil República",
                                List.of("República Velha", "Era Vargas", "Ditadura Militar", "Redemocratização"));
                taxonomy.put("História", historia);

                // ==================== GEOGRAFIA (12 áreas - usar LinkedHashMap)
                // ====================
                Map<String, List<String>> geografia = new LinkedHashMap<>();
                geografia.put("Cartografia", List.of("Coordenadas", "Projeções", "Escalas", "Fusos Horários"));
                geografia.put("Geologia", List.of("Estrutura da Terra", "Placas Tectônicas", "Vulcanismo"));
                geografia.put("Relevo", List.of("Tipos de Relevo", "Agentes Modeladores"));
                geografia.put("Climatologia", List.of("Elementos do Clima", "Fatores Climáticos", "Tipos de Clima"));
                geografia.put("Hidrografia", List.of("Ciclo da Água", "Bacias Hidrográficas", "Rios"));
                geografia.put("Biogeografia", List.of("Biomas Mundiais", "Biomas Brasileiros"));
                geografia.put("População", List.of("Demografia", "Teorias", "Pirâmides Etárias", "Migrações"));
                geografia.put("Urbanização", List.of("Redes Urbanas", "Problemas Urbanos"));
                geografia.put("Agrária", List.of("Sistemas Agrícolas", "Agronegócio", "Reforma Agrária"));
                geografia.put("Industrial", List.of("Revoluções Industriais", "Industrialização Brasileira"));
                geografia.put("Economia", List.of("Capitalismo", "Globalização", "Blocos Econômicos"));
                geografia.put("Geopolítica", List.of("Ordem Mundial", "Conflitos", "Organizações Internacionais"));
                taxonomy.put("Geografia", geografia);

                // ==================== FILOSOFIA ====================
                taxonomy.put("Filosofia", Map.of(
                                "Filosofia Antiga", List.of("Pré-Socráticos", "Sócrates", "Platão", "Aristóteles"),
                                "Filosofia Medieval", List.of("Patrística", "Escolástica"),
                                "Filosofia Moderna", List.of("Racionalismo", "Empirismo", "Kant", "Contratualistas"),
                                "Filosofia Contemporânea",
                                List.of("Marx", "Nietzsche", "Existencialismo", "Escola de Frankfurt"),
                                "Ética", List.of("Ética e Moral", "Liberdade", "Justiça")));

                // ==================== SOCIOLOGIA ====================
                taxonomy.put("Sociologia", Map.of(
                                "Clássicos", List.of("Durkheim", "Weber", "Marx"),
                                "Conceitos", List.of("Fato Social", "Ação Social", "Classes Sociais"),
                                "Cultura", List.of("Indústria Cultural", "Ideologia"),
                                "Trabalho", List.of("Divisão do Trabalho", "Alienação"),
                                "Política", List.of("Estado", "Movimentos Sociais", "Cidadania"),
                                "Sociologia Brasileira",
                                List.of("Gilberto Freyre", "Sérgio Buarque", "Darcy Ribeiro")));

                // ==================== ARTES ====================
                taxonomy.put("Artes", Map.of(
                                "História da Arte",
                                List.of("Pré-História", "Antiguidade", "Medieval", "Renascimento", "Barroco",
                                                "Impressionismo", "Vanguardas", "Contemporânea"),
                                "Arte no Brasil",
                                List.of("Arte Indígena", "Barroco Brasileiro", "Semana de 22", "Modernismo"),
                                "Linguagem Visual", List.of("Elementos Visuais", "Composição", "Cores"),
                                "Música", List.of("Elementos da Música", "Gêneros", "MPB"),
                                "Teatro", List.of("História do Teatro", "Teatro Brasileiro"),
                                "Cinema", List.of("História do Cinema", "Cinema Brasileiro")));

                // ==================== EDUCAÇÃO FÍSICA ====================
                taxonomy.put("Educação Física", Map.of(
                                "Esportes", List.of("Individuais", "Coletivos", "Regras"),
                                "Saúde", List.of("Atividade Física", "Sedentarismo", "Alimentação"),
                                "Cultura Corporal", List.of("Ginástica", "Lutas", "Danças")));
        }

        public Map<String, Map<String, List<String>>> getTaxonomy() {
                return taxonomy;
        }
}

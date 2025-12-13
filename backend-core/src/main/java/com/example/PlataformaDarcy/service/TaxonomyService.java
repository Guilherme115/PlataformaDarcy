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

        // ARTES
        taxonomy.put("Artes", Map.of(
                "História da Arte Geral", List.of("Pré-História", "Arte Egípcia", "Grega e Romana", "Arte Medieval", "Renascimento", "Barroco", "Rococó", "Neoclassicismo", "Romantismo", "Realismo", "Impressionismo", "Vanguardas Europeias", "Arte Contemporânea"),
                "História da Arte no Brasil", List.of("Arte Indígena", "Barroco Mineiro", "Missão Artística Francesa", "Semana de 22", "Modernismo", "Arte Contemporânea Brasileira"),
                "Linguagem Visual", List.of("Ponto", "Linha", "Forma", "Teoria das Cores", "Textura", "Luz e Sombra", "Perspectiva"),
                "Música", List.of("Timbre", "Altura", "Intensidade", "Duração", "Gêneros Brasileiros", "Erudita x Popular"),
                "Teatro e Dança", List.of("Elementos da Encenação", "Teatro Grego", "Teatro Brasileiro", "Dança Contemporânea", "Folclore")
        ));

        // MATEMÁTICA
        taxonomy.put("Matemática", Map.of(
                "Matemática Básica", List.of("Conjuntos", "Frações", "Potenciação", "Radiciação", "Notação Científica", "Razão e Proporção", "Regra de Três", "Porcentagem"),
                "Funções", List.of("Conceito de Função", "Função Afim", "Função Quadrática", "Função Modular", "Função Exponencial", "Função Logarítmica"),
                "Geometria Plana", List.of("Ângulos", "Triângulos", "Semelhança", "Teorema de Pitágoras", "Polígonos", "Circunferências", "Áreas e Perímetros"),
                "Geometria Espacial", List.of("Prismas", "Pirâmides", "Cilindros", "Cones", "Esferas", "Poliedros"),
                "Geometria Analítica", List.of("Ponto e Reta", "Circunferência", "Cônicas"),
                "Trigonometria", List.of("Ciclo Trigonométrico", "Seno, Cosseno e Tangente", "Equações Trigonométricas"),
                "Estatística e Probabilidade", List.of("Média, Moda e Mediana", "Desvio Padrão", "Gráficos", "Probabilidade", "Análise Combinatória"),
                "Álgebra Linear", List.of("Matrizes", "Determinantes", "Sistemas Lineares")
        ));

        // FÍSICA
        taxonomy.put("Física", Map.of(
                "Cinemática", List.of("MRU", "MRUV", "Vetores", "Lançamento Vertical", "Lançamento Oblíquo", "Movimento Circular (MCU)"),
                "Dinâmica", List.of("Leis de Newton", "Força de Atrito", "Plano Inclinado", "Trabalho e Energia", "Potência", "Impulso e Quantidade de Movimento", "Gravitação Universal"),
                "Estática e Hidrostática", List.of("Estática de Corpo Rígido", "Princípio de Arquimedes", "Princípio de Pascal", "Pressão"),
                "Termologia", List.of("Escalas Termométricas", "Dilatação Térmica", "Calorimetria", "Termodinâmica", "Gases Ideais"),
                "Óptica e Ondulatória", List.of("Espelhos", "Lentes", "Refração", "Reflexão", "Difração", "Interferência", "Acústica"),
                "Eletricidade", List.of("Lei de Coulomb", "Campo Elétrico", "Potencial Elétrico", "Leis de Ohm", "Resistores", "Leis de Kirchhoff", "Potência Elétrica"),
                "Magnetismo", List.of("Campo Magnético", "Força Magnética", "Indução (Faraday-Lenz)"),
                "Física Moderna", List.of("Relatividade", "Efeito Fotoelétrico", "Radioatividade")
        ));

        // QUÍMICA
        taxonomy.put("Química", Map.of(
                "Química Geral", List.of("Modelos Atômicos", "Distribuição Eletrônica", "Tabela Periódica", "Propriedades Periódicas"),
                "Ligações e Estrutura", List.of("Ligação Iônica", "Ligação Covalente", "Ligação Metálica", "Geometria Molecular", "Polaridade", "Forças Intermoleculares"),
                "Físico-Química", List.of("Soluções", "Propriedades Coligativas", "Termoquímica", "Cinética Química", "Equilíbrio Químico", "pH e pOH", "Eletroquímica"),
                "Química Orgânica", List.of("Cadeias Carbônicas", "Hidrocarbonetos", "Funções Oxigenadas", "Funções Nitrogenadas", "Isomeria", "Reações Orgânicas", "Polímeros")
        ));

        // BIOLOGIA
        taxonomy.put("Biologia", Map.of(
                "Citologia", List.of("Bioquímica Celular", "Membrana Plasmática", "Organelas", "Mitose e Meiose"),
                "Metabolismo", List.of("Respiração Celular", "Fotossíntese", "Fermentação"),
                "Fisiologia Humana", List.of("Digestório", "Respiratório", "Circulatório", "Excretor", "Endócrino", "Nervoso", "Imunológico", "Reprodutor"),
                "Genética", List.of("Leis de Mendel", "Sistema ABO/Rh", "Biotecnologia"),
                "Evolução e Ecologia", List.of("Teorias Evolutivas", "Cadeia Alimentar", "Ciclos Biogeoquímicos", "Biomas", "Relações Ecológicas"),
                "Taxonomia", List.of("Vírus", "Bactérias", "Protozoários", "Fungos", "Plantas (Botânica)", "Animais (Zoologia)")
        ));

        // HISTÓRIA
        taxonomy.put("História", Map.of(
                "História Geral", List.of("Grécia Antiga", "Roma Antiga", "Feudalismo", "Renascimento", "Iluminismo", "Revolução Francesa", "Revolução Industrial", "Guerras Mundiais", "Guerra Fria"),
                "História do Brasil", List.of("Brasil Colônia", "Ciclo do Ouro", "Brasil Império", "República Velha", "Era Vargas", "Ditadura Militar", "Redemocratização")
        ));

        // GEOGRAFIA
        taxonomy.put("Geografia", Map.of(
                "Geografia Física", List.of("Cartografia", "Relevo e Solo", "Climatologia", "Hidrografia", "Biomas"),
                "Geografia Humana", List.of("Demografia", "Urbanização", "Agrária", "Indústria", "Globalização", "Geopolítica"),
                "Geografia do Brasil", List.of("Clima Brasileiro", "Relevo Brasileiro", "Economia Brasileira", "Matriz Energética")
        ));

        // PORTUGUÊS
        taxonomy.put("Português", Map.of(
                "Gramática", List.of("Fonética", "Morfologia", "Sintaxe", "Crase", "Concordância", "Pontuação"),
                "Interpretação", List.of("Gêneros Textuais", "Figuras de Linguagem", "Coesão e Coerência", "Funções da Linguagem"),
                "Literatura", List.of("Quinhentismo", "Barroco", "Arcadismo", "Romantismo", "Realismo", "Modernismo", "Literatura Contemporânea")
        ));

        // FILOSOFIA/SOCIOLOGIA
        taxonomy.put("Filosofia_Sociologia", Map.of(
                "Filosofia", List.of("Pré-socráticos", "Clássica (Sócrates/Platão)", "Medieval", "Moderna", "Contratualistas", "Contemporânea"),
                "Sociologia", List.of("Clássicos (Durkheim/Marx/Weber)", "Cultura", "Trabalho", "Movimentos Sociais", "Sociologia Brasileira")
        ));
    }

    public Map<String, Map<String, List<String>>> getTaxonomy() {
        return taxonomy;
    }
}
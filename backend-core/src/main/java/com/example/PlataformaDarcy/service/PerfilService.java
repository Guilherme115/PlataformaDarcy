package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PerfilService {

    @Autowired
    private SimuladoRepository simuladoRepo;
    @Autowired
    private RegistroErroRepository erroRepo;
    @Autowired
    private WikiPostRepository wikiPostRepo;
    @Autowired
    private WikiFavoritoRepository wikiFavRepo;
    @Autowired
    private UsuarioRepository usuarioRepo;

    /**
     * Carrega todos os dados do perfil para exibição na página
     */
    public Map<String, Object> carregarDadosPerfil(Usuario usuario) {
        Map<String, Object> data = new HashMap<>();

        // === INFORMAÇÕES BÁSICAS ===
        data.put("usuario", usuario);
        data.put("plano", detectarPlano(usuario));
        data.put("diasAtivo", calcularDiasAtivo(usuario));

        // === ESTATÍSTICAS DE SIMULADOS ===
        List<Simulado> simulados = simuladoRepo.findByUsuarioOrderByDataInicioDesc(usuario);
        data.put("totalSimulados", simulados.size());
        data.put("simuladosRecentes", simulados.stream().limit(5).toList());

        // Média de notas
        double mediaNotas = simulados.stream()
                .filter(s -> s.getNotaFinal() != null)
                .mapToDouble(Simulado::getNotaFinal)
                .average()
                .orElse(0.0);
        data.put("mediaNotas", Math.round(mediaNotas * 100.0) / 100.0);

        // Melhor nota
        double melhorNota = simulados.stream()
                .filter(s -> s.getNotaFinal() != null)
                .mapToDouble(Simulado::getNotaFinal)
                .max()
                .orElse(0.0);
        data.put("melhorNota", melhorNota);

        // === ESTATÍSTICAS DE ERROS ===
        List<RegistroErro> erros = erroRepo.findByUsuarioOrderByTemperaturaDesc(usuario);
        data.put("totalErrosRegistrados", erros.size());

        int errosPendentes = (int) erros.stream()
                .filter(e -> e.getStatus() == RegistroErro.StatusCiclo.PENDENTE_TRIAGEM
                        || e.getStatus() == RegistroErro.StatusCiclo.PROTOCOLO_DIARIO)
                .count();
        data.put("errosPendentes", errosPendentes);

        int errosDominados = (int) erros.stream()
                .filter(e -> e.getStatus() == RegistroErro.StatusCiclo.DOMINADA
                        || e.getStatus() == RegistroErro.StatusCiclo.EXPURGADO)
                .count();
        data.put("errosDominados", errosDominados);

        // Temperatura média
        double tempMedia = erros.stream()
                .filter(e -> e.getTemperatura() != null)
                .mapToInt(RegistroErro::getTemperatura)
                .average()
                .orElse(0.0);
        data.put("temperaturaMedia", (int) tempMedia);

        // Top 5 erros mais críticos
        data.put("errosCriticos", erros.stream()
                .filter(e -> e.getTemperatura() != null && e.getTemperatura() >= 70)
                .limit(5)
                .toList());

        // === ESTATÍSTICAS WIKI ===
        List<WikiPost> meusPosts = wikiPostRepo.findByAutorOrderByDataCriacaoDesc(usuario);
        data.put("totalPosts", meusPosts.size());

        int totalCurtidas = meusPosts.stream().mapToInt(WikiPost::getCurtidas).sum();
        data.put("totalCurtidas", totalCurtidas);

        int totalVisualizacoes = meusPosts.stream().mapToInt(WikiPost::getVisualizacoes).sum();
        data.put("totalVisualizacoes", totalVisualizacoes);

        List<WikiPost> favoritos = wikiFavRepo.findByUsuarioOrderByDataFavoritoDesc(usuario)
                .stream().map(WikiFavorito::getPost).toList();
        data.put("totalFavoritos", favoritos.size());

        // === BADGES E CONQUISTAS ===
        data.put("badges", calcularBadges(usuario, simulados, meusPosts, errosDominados));

        // === GRÁFICO DE EVOLUÇÃO (últimos 7 simulados) ===
        List<Map<String, Object>> evolucao = new ArrayList<>();
        List<Simulado> ultimos7 = simulados.stream()
                .filter(s -> s.getNotaFinal() != null)
                .limit(7)
                .toList();
        Collections.reverse(new ArrayList<>(ultimos7));
        for (Simulado s : ultimos7) {
            Map<String, Object> ponto = new HashMap<>();
            ponto.put("titulo", s.getTitulo() != null ? s.getTitulo() : "Simulado");
            ponto.put("nota", s.getNotaFinal());
            evolucao.add(ponto);
        }
        data.put("evolucaoNotas", evolucao);

        return data;
    }

    private String detectarPlano(Usuario usuario) {
        // TODO: Integrar com sistema de pagamento real
        // Por enquanto, retorna baseado no perfil
        if ("ADMIN".equals(usuario.getPerfil())) {
            return "ADMIN";
        }
        // Placeholder - integrar com Stripe/sistema de assinatura
        return "CALANGO"; // Plano grátis por padrão
    }

    private long calcularDiasAtivo(Usuario usuario) {
        // Aproximação - usar data de criação se existir
        // Por enquanto, retorna valor fixo ou baseado em atividade
        return 30; // TODO: Implementar rastreamento de data de cadastro
    }

    private List<Map<String, Object>> calcularBadges(Usuario usuario, List<Simulado> simulados,
            List<WikiPost> posts, int errosDominados) {
        List<Map<String, Object>> badges = new ArrayList<>();

        // Badge: Primeiro Simulado
        if (!simulados.isEmpty()) {
            badges.add(createBadge("Iniciante", "Completou seu primeiro simulado", "target", true));
        }

        // Badge: 10 Simulados
        if (simulados.size() >= 10) {
            badges.add(createBadge("Dedicado", "Completou 10 simulados", "trophy", true));
        } else {
            badges.add(createBadge("Dedicado", "Complete 10 simulados", "trophy", false));
        }

        // Badge: Nota alta
        boolean temNotaAlta = simulados.stream()
                .anyMatch(s -> s.getNotaFinal() != null && s.getNotaFinal() >= 80);
        if (temNotaAlta) {
            badges.add(createBadge("Excelência", "Alcançou nota >= 80 em simulado", "star", true));
        }

        // Badge: Contribuidor Wiki
        if (!posts.isEmpty()) {
            badges.add(createBadge("Contribuidor", "Publicou conteúdo na Wiki", "pen-tool", true));
        } else {
            badges.add(createBadge("Contribuidor", "Publique na Wiki", "pen-tool", false));
        }

        // Badge: Expurgador
        if (errosDominados >= 10) {
            badges.add(createBadge("Expurgador", "Dominou 10 erros no Protocolo", "skull", true));
        }

        // Badge: Veterano (30 dias)
        badges.add(createBadge("Veterano", "30 dias na plataforma", "calendar", true));

        return badges;
    }

    private Map<String, Object> createBadge(String nome, String descricao, String icone, boolean conquistado) {
        Map<String, Object> badge = new HashMap<>();
        badge.put("nome", nome);
        badge.put("descricao", descricao);
        badge.put("icone", icone);
        badge.put("conquistado", conquistado);
        return badge;
    }

    /**
     * Atualiza informações básicas do perfil
     */
    public Usuario atualizarPerfil(Long usuarioId, String nome, String regiao, Integer etapaAlvo) {
        Usuario u = usuarioRepo.findById(usuarioId).orElseThrow();
        if (nome != null && !nome.isBlank())
            u.setNome(nome);
        if (regiao != null && !regiao.isBlank())
            u.setRegiao(regiao);
        if (etapaAlvo != null && etapaAlvo >= 1 && etapaAlvo <= 3)
            u.setEtapaAlvo(etapaAlvo);
        return usuarioRepo.save(u);
    }

    /**
     * Exporta todos os dados do usuário para conformidade LGPD
     */
    public Map<String, Object> exportarDados(Usuario usuario) {
        Map<String, Object> dados = new LinkedHashMap<>();

        // 1. Dados Pessoais
        Map<String, Object> pessoal = new LinkedHashMap<>();
        pessoal.put("matricula", usuario.getMatricula());
        pessoal.put("nome", usuario.getNome());
        pessoal.put("email", usuario.getEmail());
        pessoal.put("regiao", usuario.getRegiao());
        pessoal.put("etapa_alvo", usuario.getEtapaAlvo());
        pessoal.put("perfil", usuario.getPerfil());
        pessoal.put("plano", usuario.getPlano());
        pessoal.put("data_expiracao_plano", usuario.getDataExpiracaoPlano());
        dados.put("dados_pessoais", pessoal);

        // 2. Histórico de Simulados
        List<Map<String, Object>> simuladosExport = new ArrayList<>();
        List<Simulado> simulados = simuladoRepo.findByUsuarioOrderByDataInicioDesc(usuario);
        for (Simulado s : simulados) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", s.getId());
            map.put("titulo", s.getTitulo());
            map.put("data", s.getDataInicio());
            map.put("nota_final", s.getNotaFinal());
            if (s.getDataInicio() != null && s.getDataFim() != null) {
                map.put("tempo_gasto_segundos",
                        java.time.Duration.between(s.getDataInicio(), s.getDataFim()).toSeconds());
            } else {
                map.put("tempo_gasto_segundos", null);
            }
            simuladosExport.add(map);
        }
        dados.put("historico_simulados", simuladosExport);

        // 3. Registro de Erros
        List<Map<String, Object>> errosExport = new ArrayList<>();
        List<RegistroErro> erros = erroRepo.findByUsuarioOrderByTemperaturaDesc(usuario);
        for (RegistroErro e : erros) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", e.getId());
            map.put("que_estao", e.getQuestaoOriginal() != null ? e.getQuestaoOriginal().getId() : "N/A");
            map.put("causa", e.getCausa());
            map.put("temperatura", e.getTemperatura());
            map.put("status", e.getStatus());
            errosExport.add(map);
        }
        dados.put("registro_erros", errosExport);

        // 4. Contribuições Wiki
        List<Map<String, Object>> wikiExport = new ArrayList<>();
        List<WikiPost> posts = wikiPostRepo.findByAutorOrderByDataCriacaoDesc(usuario);
        for (WikiPost p : posts) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", p.getId());
            map.put("titulo", p.getTitulo());
            map.put("etapa", p.getEtapa());
            map.put("disciplina", p.getDisciplina());
            map.put("topico", p.getTopico());
            map.put("tipo", p.getTipoConteudo());
            map.put("data_criacao", p.getDataCriacao());
            map.put("curtidas", p.getCurtidas());
            wikiExport.add(map);
        }
        dados.put("contribuicoes_wiki", wikiExport);

        return dados;
    }
}

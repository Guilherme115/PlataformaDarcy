package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.repository.*;
import com.example.PlataformaDarcy.service.ImageService;
import com.example.PlataformaDarcy.service.TaxonomyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cms")
public class CmsController {

    @Autowired
    private BlocoRepository blocoRepo;
    @Autowired
    private QuestaoRepository questaoRepo;
    @Autowired
    private ProvaRepository provaRepo;
    @Autowired
    private ImageService imageService;
    @Autowired
    private TaxonomyService taxonomyService;
    @Autowired
    private com.example.PlataformaDarcy.service.TaxonomiaService taxonomiaService;

    // TELA PRINCIPAL
    @GetMapping
    public String dashboard(@RequestParam(defaultValue = "1") Integer etapa,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Long blocoId,
            Model model) {

        model.addAttribute("etapaAtiva", etapa);
        model.addAttribute("anoAtivo", ano);

        try {
            // Carrega taxonomia da etapa do banco de dados
            java.util.Map<String, java.util.Map<String, java.util.List<String>>> taxonomy = taxonomiaService
                    .getTaxonomyByEtapa(etapa);
            model.addAttribute("taxonomyJson", new ObjectMapper().writeValueAsString(taxonomy));
        } catch (Exception e) {
            model.addAttribute("taxonomyJson", "{}");
        }

        if (ano == null) {
            List<Integer> anosDisponiveis = provaRepo.findAnosByEtapa(etapa);
            model.addAttribute("anosDisponiveis", anosDisponiveis);
            // Refatorado: admin/cms-selecao-ano.html
            return "admin/cms-selecao-ano";
        }

        List<Bloco> blocos = blocoRepo.findAllByProvaEtapaAndAno(etapa, ano);
        model.addAttribute("blocosSidebar", blocos);

        Bloco blocoAtivo = null;
        if (blocoId != null) {
            blocoAtivo = blocoRepo.findById(blocoId).orElse(null);
        } else if (!blocos.isEmpty()) {
            blocoAtivo = blocos.get(0);
        }

        model.addAttribute("blocoAtivo", blocoAtivo);
        // Refatorado: admin/cms.html
        return "admin/cms";
    }

    @PostMapping("/salvar-bloco/{id}")
    @ResponseBody
    public String salvarBloco(@PathVariable Long id, @RequestParam String textoBase) {
        Bloco bloco = blocoRepo.findById(id).orElseThrow();
        bloco.setTextoBase(textoBase);
        blocoRepo.save(bloco);
        return "<span class='text-green-600 font-bold text-xs'>TEXTO SALVO!</span>";
    }

    @PostMapping("/salvar-questao/{id}")
    @ResponseBody
    public String salvarQuestao(@PathVariable Long id,
            @RequestParam(required = false) String enunciado,
            @RequestParam(required = false) TipoQuestao tipo,
            @RequestParam(required = false) String gabarito,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String altA,
            @RequestParam(required = false) String altB,
            @RequestParam(required = false) String altC,
            @RequestParam(required = false) String altD,
            @RequestParam(required = false) String statusCheck) {

        Questao q = questaoRepo.findById(id).orElseThrow();

        if (enunciado != null)
            q.setEnunciado(enunciado);
        if (tipo != null)
            q.setTipo(tipo);
        if (gabarito != null)
            q.setGabarito(gabarito.toUpperCase());
        if (tags != null)
            q.setTags(tags.toUpperCase());

        q.setStatus("REVISADO".equals(statusCheck) ? StatusRevisao.REVISADO : StatusRevisao.PENDENTE);

        if (q.getTipo() == TipoQuestao.C) {
            try {
                Map<String, String> map = new HashMap<>();
                map.put("A", altA != null ? altA : q.getAltTexto("A"));
                map.put("B", altB != null ? altB : q.getAltTexto("B"));
                map.put("C", altC != null ? altC : q.getAltTexto("C"));
                map.put("D", altD != null ? altD : q.getAltTexto("D"));
                q.setAlternativas(new ObjectMapper().writeValueAsString(map));
            } catch (Exception e) {
            }
        }

        questaoRepo.save(q);
        String cor = q.getStatus() == StatusRevisao.REVISADO ? "text-green-600" : "text-orange-500";
        String texto = q.getStatus() == StatusRevisao.REVISADO ? "REVISADO" : "PENDENTE";

        return "<span class='" + cor
                + " font-bold text-[10px] bg-white border border-black px-2 uppercase tracking-widest'>" + texto
                + "</span>";
    }

    // --- GALERIA ---
    @GetMapping("/galeria")
    public String carregarGaleria(@RequestParam Integer ano,
            @RequestParam Integer etapa,
            @RequestParam Long qid,
            @RequestParam String tag,
            Model model) {
        List<String> imagens = imageService.listarImagensDisponiveis(ano, etapa);

        model.addAttribute("imagensGaleria", imagens);
        model.addAttribute("questaoId", qid);
        model.addAttribute("tagAlvo", tag);
        model.addAttribute("pasta", ano + "_" + etapa);

        // Corrigido: template está em admin/cms-modal.html
        return "admin/cms-modal :: modal-galeria";
    }

    @PostMapping("/vincular-imagem")
    public String vincularImagem(@RequestParam Long qid, @RequestParam String caminho, @RequestParam String tag,
            Model model) {
        imageService.vincularImagemExistente(qid, caminho, tag);
        Questao q = questaoRepo.findById(qid).orElseThrow();
        model.addAttribute("q", q);
        return "admin/cms :: lista-imagens-questao";
    }

    @PostMapping("/upload-tag")
    public String uploadTag(@RequestParam("file") MultipartFile file, @RequestParam("qid") Long qid,
            @RequestParam("tag") String tag, Model model) throws IOException {
        imageService.uploadImagemComTag(qid, file, tag);
        Questao q = questaoRepo.findById(qid).orElseThrow();
        model.addAttribute("q", q);
        return "admin/cms :: lista-imagens-questao";
    }

    @DeleteMapping("/imagem/{id}")
    @ResponseBody
    public String deletarImagem(@PathVariable Long id) {
        imageService.excluirImagem(id);
        return "";
    }

    // ==================== CRUD: CRIAR PROVA ====================
    @PostMapping("/criar-prova")
    public String criarProva(@RequestParam Integer ano,
            @RequestParam Integer etapa,
            @RequestParam(required = false) String titulo) {
        // Verifica se já existe
        List<Prova> existentes = provaRepo.findAllByEtapaOrderByAnoDesc(etapa);
        boolean jaExiste = existentes.stream().anyMatch(p -> p.getAno().equals(ano));

        if (!jaExiste) {
            Prova prova = new Prova();
            prova.setAno(ano);
            prova.setEtapa(etapa);
            prova.setTitulo(titulo != null && !titulo.isBlank() ? titulo : "PAS " + etapa + " - " + ano);
            prova.setOrigem("CMS_MANUAL");
            provaRepo.save(prova);
        }

        return "redirect:/cms?etapa=" + etapa + "&ano=" + ano;
    }

    // ==================== CRUD: CRIAR BLOCO ====================
    @PostMapping("/criar-bloco")
    public String criarBloco(@RequestParam Integer etapa,
            @RequestParam Integer ano,
            @RequestParam(required = false) String textoBase,
            @RequestParam(required = false) String disciplina) {
        // Busca a prova
        Prova prova = provaRepo.findAllByEtapaOrderByAnoDesc(etapa).stream()
                .filter(p -> p.getAno().equals(ano))
                .findFirst()
                .orElse(null);

        if (prova == null) {
            return "redirect:/cms?etapa=" + etapa + "&erro=prova_nao_encontrada";
        }

        Bloco bloco = new Bloco();
        bloco.setProva(prova);
        bloco.setTextoBase(textoBase != null ? textoBase : "");
        bloco.setDisciplina(disciplina != null ? disciplina : "GERAL");
        bloco = blocoRepo.save(bloco);

        return "redirect:/cms?etapa=" + etapa + "&ano=" + ano + "&blocoId=" + bloco.getId();
    }

    // ==================== CRUD: CRIAR QUESTÃO ====================
    @PostMapping("/criar-questao")
    public String criarQuestao(@RequestParam Long blocoId,
            @RequestParam Integer etapa,
            @RequestParam Integer ano) {
        Bloco bloco = blocoRepo.findById(blocoId).orElse(null);
        if (bloco == null) {
            return "redirect:/cms?etapa=" + etapa + "&ano=" + ano + "&erro=bloco_nao_encontrado";
        }

        // Calcula próximo número
        int proximoNumero = 1;
        if (bloco.getQuestoes() != null && !bloco.getQuestoes().isEmpty()) {
            proximoNumero = bloco.getQuestoes().stream()
                    .mapToInt(q -> q.getNumero() != null ? q.getNumero() : 0)
                    .max()
                    .orElse(0) + 1;
        }

        Questao q = new Questao();
        q.setBloco(bloco);
        q.setProva(bloco.getProva());
        q.setNumero(proximoNumero);
        q.setEnunciado("Nova questão - edite aqui");
        q.setTipo(TipoQuestao.A);
        q.setStatus(StatusRevisao.PENDENTE);
        q.setGabarito("C");
        questaoRepo.save(q);

        return "redirect:/cms?etapa=" + etapa + "&ano=" + ano + "&blocoId=" + blocoId;
    }

    // ==================== CRUD: DELETAR BLOCO ====================
    @DeleteMapping("/bloco/{id}")
    @ResponseBody
    public String deletarBloco(@PathVariable Long id) {
        Bloco bloco = blocoRepo.findById(id).orElse(null);
        if (bloco != null) {
            // Deleta questões do bloco primeiro
            if (bloco.getQuestoes() != null) {
                questaoRepo.deleteAll(bloco.getQuestoes());
            }
            blocoRepo.delete(bloco);
        }
        return "";
    }

    // ==================== CRUD: DELETAR QUESTÃO ====================
    @DeleteMapping("/questao/{id}")
    @ResponseBody
    public String deletarQuestao(@PathVariable Long id) {
        questaoRepo.deleteById(id);
        return "";
    }

    // ==================== CRUD: DUPLICAR QUESTÃO ====================
    @PostMapping("/duplicar-questao/{id}")
    @ResponseBody
    public String duplicarQuestao(@PathVariable Long id) {
        Questao original = questaoRepo.findById(id).orElse(null);
        if (original == null) {
            return "{\"erro\": \"Questão não encontrada\"}";
        }

        Questao copia = new Questao();
        copia.setBloco(original.getBloco());
        copia.setProva(original.getProva());
        copia.setNumero(original.getNumero() != null ? original.getNumero() + 100 : 999);
        copia.setEnunciado("[CÓPIA] " + original.getEnunciado());
        copia.setTipo(original.getTipo());
        copia.setGabarito(original.getGabarito());
        copia.setTags(original.getTags());
        copia.setAlternativas(original.getAlternativas());
        copia.setStatus(StatusRevisao.PENDENTE);
        questaoRepo.save(copia);

        return "{\"id\": " + copia.getId() + "}";
    }

    // ==================== BUSCA DE QUESTÕES ====================
    @GetMapping("/buscar-questoes")
    @ResponseBody
    public List<Map<String, Object>> buscarQuestoes(@RequestParam String termo) {
        List<Questao> questoes = questaoRepo.findByTagsContainingIgnoreCase(termo);
        return questoes.stream().limit(20).map(q -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", q.getId());
            map.put("numero", q.getNumero());
            map.put("enunciado",
                    q.getEnunciado() != null ? q.getEnunciado().substring(0, Math.min(100, q.getEnunciado().length()))
                            : "");
            map.put("tipo", q.getTipo());
            map.put("tags", q.getTags());
            return map;
        }).toList();
    }

    // ==================== ESTATÍSTICAS DO BANCO ====================
    @GetMapping("/estatisticas")
    @ResponseBody
    public Map<String, Object> getEstatisticas() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProvas", provaRepo.count());
        stats.put("totalBlocos", blocoRepo.count());
        stats.put("totalQuestoes", questaoRepo.count());
        stats.put("questoesPendentes",
                questaoRepo.findFirstByStatusOrderByIdAsc(StatusRevisao.PENDENTE).isPresent() ? "Há pendentes"
                        : "Todas revisadas");
        return stats;
    }

    // ==================== IA AUTO-CLASSIFICAÇÃO ====================
    @Autowired
    private com.example.PlataformaDarcy.service.GeminiService geminiService;

    @GetMapping("/ia/pendentes")
    @ResponseBody
    public List<Long> listarPendentesIA(@RequestParam Integer etapa, @RequestParam Integer ano) {
        return questaoRepo.findIdsNaoClassificados(etapa, ano);
    }

    @GetMapping("/ia/stats")
    @ResponseBody
    public Map<String, Object> statsIA(@RequestParam Integer etapa, @RequestParam Integer ano) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("pendentes", questaoRepo.countNaoClassificados(etapa, ano));
        stats.put("total", questaoRepo.countByProvaEtapaAndAno(etapa, ano));
        return stats;
    }

    @PostMapping("/ia/classificar/{id}")
    @ResponseBody
    public Map<String, Object> classificarComIA(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            Questao q = questaoRepo.findById(id).orElseThrow();
            String textoBloco = q.getBloco() != null ? q.getBloco().getTextoBase() : "";
            String disciplinaHint = q.getBloco() != null ? q.getBloco().getDisciplina() : "GERAL";

            // Monta o prompt com a taxonomia
            String taxonomiaResumo = gerarTaxonomiaResumo();

            String prompt = """
                    Você é um classificador de questões do PAS/UnB (vestibular seriado).

                    TAXONOMIA VÁLIDA (escolha APENAS destas opções):
                    %s

                    QUESTÃO A CLASSIFICAR:
                    - Disciplina do Bloco (dica): %s
                    - Texto Base: %s
                    - Enunciado: %s

                    RESPONDA APENAS no formato: MATÉRIA, ÁREA, TÓPICO
                    Exemplo: BIOLOGIA, GENÉTICA, 1ª LEI DE MENDEL

                    Se não conseguir classificar com certeza, responda apenas a matéria mais provável.
                    """.formatted(
                    taxonomiaResumo,
                    disciplinaHint,
                    textoBloco != null ? textoBloco.substring(0, Math.min(500, textoBloco.length())) : "",
                    q.getEnunciado() != null ? q.getEnunciado().substring(0, Math.min(500, q.getEnunciado().length()))
                            : "");

            String classificacao = geminiService.gerarTexto(prompt);

            // Limpa e salva
            String tags = classificacao.trim().toUpperCase()
                    .replaceAll("[\\n\\r]", "")
                    .replaceAll("\\s+", " ");

            // Limita a 255 caracteres (tamanho do campo)
            if (tags.length() > 255) {
                tags = tags.substring(0, 255);
            }

            q.setTags(tags);
            questaoRepo.save(q);

            result.put("success", true);
            result.put("id", id);
            result.put("tags", tags);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    private String gerarTaxonomiaResumo() {
        StringBuilder sb = new StringBuilder();

        // Busca taxonomias das 3 etapas do banco de dados
        for (int etapa = 1; etapa <= 3; etapa++) {
            try {
                var taxonomy = taxonomiaService.getTaxonomyByEtapa(etapa);
                sb.append("\n=== PAS ").append(etapa).append(" ===").append("\n");

                for (var entry : taxonomy.entrySet()) {
                    sb.append("- ").append(entry.getKey().toUpperCase()).append(": ");
                    var areas = entry.getValue().keySet();
                    sb.append(String.join(", ", areas));
                    sb.append("\n");
                }
            } catch (Exception e) {
                // Continua se não houver taxonomia para a etapa
            }
        }

        return sb.toString();
    }
}
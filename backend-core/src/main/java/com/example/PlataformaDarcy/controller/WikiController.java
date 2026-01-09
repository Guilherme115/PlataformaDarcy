package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.model.Usuario;
import com.example.PlataformaDarcy.model.WikiPost;
import com.example.PlataformaDarcy.model.WikiPost.TipoConteudo;
import com.example.PlataformaDarcy.repository.WikiPostRepository;
import com.example.PlataformaDarcy.service.AuthService;
import com.example.PlataformaDarcy.service.TaxonomyService;
import com.example.PlataformaDarcy.service.WikiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/wiki")
public class WikiController {

    @Autowired
    private WikiPostRepository wikiRepo;
    @Autowired
    private WikiService wikiService;
    @Autowired
    private AuthService authService;
    @Autowired
    private TaxonomyService taxonomyService;

    // ==================== CATÁLOGO (Netflix-Style) ====================

    @GetMapping
    public String catalogo(Model model) {
        model.addAllAttributes(wikiService.carregarCatalogo());
        model.addAttribute("taxonomy", taxonomyService.getTaxonomy());
        return "wiki/catalogo";
    }

    // ==================== HUB DA DISCIPLINA ====================

    @GetMapping("/hub/{disciplina}")
    public String hubDisciplina(@PathVariable String disciplina, Model model) {
        model.addAllAttributes(wikiService.carregarHubDisciplina(disciplina.toUpperCase()));
        return "wiki/hub-disciplina";
    }

    // ==================== EXPLORAR (Filtros) ====================

    @GetMapping("/explorar")
    public String explorar(@RequestParam(required = false) Integer etapa,
            @RequestParam(required = false) String disciplina,
            @RequestParam(required = false) String topico,
            @RequestParam(required = false) String tipo,
            Model model) {
        List<WikiPost> posts;

        if (tipo != null && !tipo.isEmpty()) {
            posts = wikiRepo.findByTipoConteudoAndRascunhoFalseOrderByVisualizacoesDesc(
                    TipoConteudo.valueOf(tipo.toUpperCase()));
        } else if (topico != null && !topico.isEmpty()) {
            posts = wikiRepo.findByTopicoOrderByCurtidasDesc(topico);
        } else if (disciplina != null && !disciplina.isEmpty()) {
            posts = wikiRepo.findByDisciplinaOrderByCurtidasDesc(disciplina);
        } else if (etapa != null) {
            posts = wikiRepo.findByEtapaOrderByCurtidasDesc(etapa);
        } else {
            posts = wikiRepo.findTop6ByRascunhoFalseOrderByDataCriacaoDesc();
        }

        model.addAttribute("posts", posts);
        model.addAttribute("filtroAtual", buildFilterLabel(etapa, disciplina, tipo));
        return "wiki/explorar";
    }

    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "q", required = false) String termo, Model model) {
        model.addAttribute("posts", wikiService.buscar(termo));
        model.addAttribute("termo", termo);
        return "wiki/explorar";
    }

    // ==================== LEITURA (Reader) ====================

    @GetMapping("/{id}")
    public String lerPost(@PathVariable Long id, Model model) {
        WikiPost post = wikiService.abrirParaLeitura(id);
        Usuario usuario = authService.getUsuarioLogado();

        model.addAttribute("p", post);
        model.addAttribute("relacionados", wikiService.buscarRelacionados(post));

        if (usuario != null) {
            model.addAttribute("isFavorito", wikiService.isFavorito(post, usuario));
        }

        return "wiki/leitura";
    }

    // ==================== FAVORITOS E CURTIDAS ====================

    @PostMapping("/{id}/favoritar")
    @ResponseBody
    public String toggleFavorito(@PathVariable Long id) {
        Usuario usuario = authService.getUsuarioLogado();
        if (usuario == null)
            return "<span class='text-red-500'>Login necessário</span>";

        WikiPost post = wikiRepo.findById(id).orElseThrow();
        boolean adicionado = wikiService.toggleFavorito(post, usuario);

        if (adicionado) {
            return "<button hx-post='/wiki/" + id
                    + "/favoritar' hx-swap='outerHTML' class='flex items-center gap-2 bg-darcy-yellow text-black px-4 py-2 font-bold border-2 border-black'>"
                    +
                    "<i data-lucide='bookmark-check' class='w-5 h-5'></i> Salvo</button>";
        } else {
            return "<button hx-post='/wiki/" + id
                    + "/favoritar' hx-swap='outerHTML' class='flex items-center gap-2 bg-white text-black px-4 py-2 font-bold border-2 border-black hover:bg-darcy-yellow transition'>"
                    +
                    "<i data-lucide='bookmark' class='w-5 h-5'></i> Salvar</button>";
        }
    }

    @PostMapping("/{id}/curtir")
    @ResponseBody
    public String curtir(@PathVariable Long id) {
        int novoTotal = wikiService.curtir(id);
        return "<span class='font-bold'>" + novoTotal + "</span>";
    }

    @GetMapping("/favoritos")
    public String meusFavoritos(Model model) {
        Usuario usuario = authService.getUsuarioLogado();
        model.addAttribute("posts", wikiService.listarFavoritos(usuario));
        model.addAttribute("titulo", "Meus Favoritos");
        return "wiki/explorar";
    }

    // ==================== CRIAÇÃO E EDIÇÃO ====================

    @GetMapping("/criar")
    public String novoPost(Model model) {
        model.addAttribute("post", new WikiPost());
        model.addAttribute("taxonomy", taxonomyService.getTaxonomy());
        model.addAttribute("tipos", Arrays.asList(TipoConteudo.values()));
        model.addAttribute("editando", false);
        return "wiki/editor";
    }

    @GetMapping("/{id}/editar")
    public String editarPost(@PathVariable Long id, Model model) {
        WikiPost post = wikiRepo.findById(id).orElseThrow();
        model.addAttribute("post", post);
        model.addAttribute("taxonomy", taxonomyService.getTaxonomy());
        model.addAttribute("tipos", Arrays.asList(TipoConteudo.values()));
        model.addAttribute("editando", true);
        return "wiki/editor";
    }

    @PostMapping("/salvar")
    public String salvarPost(@ModelAttribute WikiPost post,
            @RequestParam(defaultValue = "true") boolean rascunho) {
        Usuario autor = authService.getUsuarioLogado();
        post.setRascunho(rascunho);
        WikiPost saved = wikiService.salvarPost(post, autor);

        if (rascunho) {
            return "redirect:/wiki/meus-posts?saved=draft";
        }
        return "redirect:/wiki/" + saved.getId();
    }

    @PostMapping("/{id}/publicar")
    public String publicar(@PathVariable Long id) {
        wikiService.publicar(id);
        return "redirect:/wiki/" + id;
    }

    @PostMapping("/{id}/deletar")
    public String deletar(@PathVariable Long id) {
        Usuario usuario = authService.getUsuarioLogado();
        wikiService.deletar(id, usuario);
        return "redirect:/wiki/meus-posts";
    }

    // ==================== MEUS POSTS ====================

    @GetMapping("/meus-posts")
    public String meusPostsPage(Model model) {
        Usuario usuario = authService.getUsuarioLogado();
        model.addAttribute("publicados", wikiService.meusPostsPublicados(usuario));
        model.addAttribute("rascunhos", wikiService.meusRascunhos(usuario));
        return "wiki/meus-posts";
    }

    // ==================== HELPERS ====================

    private String buildFilterLabel(Integer etapa, String disciplina, String tipo) {
        StringBuilder sb = new StringBuilder();
        if (etapa != null)
            sb.append("PAS ").append(etapa).append(" ");
        if (disciplina != null)
            sb.append(disciplina).append(" ");
        if (tipo != null)
            sb.append(tipo);
        return sb.toString().trim();
    }
}
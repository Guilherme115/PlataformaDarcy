package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.model.Usuario;
import com.example.PlataformaDarcy.model.WikiPost;
import com.example.PlataformaDarcy.repository.WikiPostRepository;
import com.example.PlataformaDarcy.service.AuthService;
import com.example.PlataformaDarcy.service.TaxonomyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/wiki")
public class WikiController {

    @Autowired private WikiPostRepository wikiRepo;
    @Autowired private AuthService authService;
    @Autowired private TaxonomyService taxonomyService; // Para pegar as matérias oficiais

    // 1. A BIBLIOTECA (HUB) - O aluno escolhe onde quer entrar
    @GetMapping
    public String hub(Model model) {
        // Envia as matérias para o filtro
        model.addAttribute("taxonomy", taxonomyService.getTaxonomy());

        // Pega os posts mais populares (Top 3 de cada etapa para destaque)
        model.addAttribute("destaquesPas1", wikiRepo.findTop3ByEtapaOrderByCurtidasDesc(1));
        model.addAttribute("destaquesPas2", wikiRepo.findTop3ByEtapaOrderByCurtidasDesc(2));
        model.addAttribute("destaquesPas3", wikiRepo.findTop3ByEtapaOrderByCurtidasDesc(3));

        return "wiki-hub";
    }

    // 2. LISTA DE CONTEÚDO (Quando filtra por Matéria/Tópico)
    @GetMapping("/explorar")
    public String explorar(@RequestParam(required = false) Integer etapa,
                           @RequestParam(required = false) String disciplina,
                           @RequestParam(required = false) String topico,
                           Model model) {

        // Busca dinâmica (Poderia ser um Specification, mas aqui simplificado)
        List<WikiPost> posts;
        if (topico != null && !topico.isEmpty()) {
            posts = wikiRepo.findByTopicoOrderByCurtidasDesc(topico);
        } else if (disciplina != null && !disciplina.isEmpty()) {
            posts = wikiRepo.findByDisciplinaOrderByCurtidasDesc(disciplina);
        } else if (etapa != null) {
            posts = wikiRepo.findByEtapaOrderByCurtidasDesc(etapa);
        } else {
            posts = wikiRepo.findAll();
        }

        model.addAttribute("posts", posts);
        model.addAttribute("filtroAtual", (etapa != null ? "PAS " + etapa : "") + " " + (disciplina != null ? disciplina : ""));
        return "wiki-lista";
    }

    // 3. EDITOR (Novo Post)
    @GetMapping("/novo")
    public String novoPost(Model model) {
        model.addAttribute("taxonomy", taxonomyService.getTaxonomy());
        return "wiki-editor";
    }

    @PostMapping("/salvar")
    public String salvarPost(WikiPost post) {
        Usuario autor = authService.getUsuarioLogado();
        post.setAutor(autor);
        wikiRepo.save(post);
        return "redirect:/wiki/explorar?disciplina=" + post.getDisciplina();
    }

    // 4. LER POST (Detalhe)
    @GetMapping("/{id}")
    public String lerPost(@PathVariable Long id, Model model) {
        WikiPost post = wikiRepo.findById(id).orElseThrow();
        model.addAttribute("p", post);
        return "wiki-leitura";
    }
}
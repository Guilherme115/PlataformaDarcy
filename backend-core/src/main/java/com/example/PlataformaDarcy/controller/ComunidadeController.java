package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.service.ComunidadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller da Comunidade.
 */
@Controller
@RequestMapping("/comunidade")
public class ComunidadeController {

    @Autowired
    private ComunidadeService comunidadeService;

    // ==================== FEED ====================

    /**
     * Feed principal (todos os posts).
     */
    @GetMapping
    public String feed(@RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String q,
            Model model) {
        Page<PostComunidade> posts;

        if (q != null && !q.isBlank()) {
            posts = comunidadeService.buscar(q, page);
            model.addAttribute("termoBusca", q);
        } else {
            posts = comunidadeService.getFeed(page);
        }

        model.addAttribute("posts", posts);
        model.addAttribute("categorias", CategoriaComunidade.values());
        model.addAttribute("contagem", comunidadeService.getContagemPorCategoria());
        model.addAttribute("categoriaAtual", null);

        return "comunidade/feed";
    }

    /**
     * Feed por categoria.
     */
    @GetMapping("/c/{categoria}")
    public String feedPorCategoria(@PathVariable String categoria,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        CategoriaComunidade cat;
        try {
            cat = CategoriaComunidade.valueOf(categoria.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            return "redirect:/comunidade";
        }

        Page<PostComunidade> posts = comunidadeService.getFeedPorCategoria(cat, page);

        model.addAttribute("posts", posts);
        model.addAttribute("categorias", CategoriaComunidade.values());
        model.addAttribute("contagem", comunidadeService.getContagemPorCategoria());
        model.addAttribute("categoriaAtual", cat);

        return "comunidade/feed";
    }

    /**
     * Posts em trending.
     */
    @GetMapping("/trending")
    public String trending(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<PostComunidade> posts = comunidadeService.getTrending(page);

        model.addAttribute("posts", posts);
        model.addAttribute("categorias", CategoriaComunidade.values());
        model.addAttribute("contagem", comunidadeService.getContagemPorCategoria());
        model.addAttribute("trending", true);

        return "comunidade/feed";
    }

    // ==================== POST INDIVIDUAL ====================

    /**
     * Ver post com comentários.
     */
    @GetMapping("/post/{id}")
    public String verPost(@PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario,
            Model model) {
        var post = comunidadeService.getPost(id);

        if (post.isEmpty() || !post.get().isAtivo()) {
            return "redirect:/comunidade";
        }

        var p = post.get();
        var comentarios = comunidadeService.getComentarios(p);

        // Verifica se usuário já curtiu
        boolean curtiu = p.getUsuariosLike().stream()
                .anyMatch(u -> u.getId().equals(usuario.getId()));

        model.addAttribute("post", p);
        model.addAttribute("comentarios", comentarios);
        model.addAttribute("curtiu", curtiu);
        model.addAttribute("usuario", usuario);

        return "comunidade/post";
    }

    // ==================== CRIAR/EDITAR POST ====================

    /**
     * Form para novo post.
     */
    @GetMapping("/novo")
    public String formNovoPost(Model model) {
        model.addAttribute("categorias", CategoriaComunidade.values());
        return "comunidade/novo";
    }

    /**
     * Criar post.
     */
    @PostMapping("/novo")
    public String criarPost(@AuthenticationPrincipal Usuario usuario,
            @RequestParam String titulo,
            @RequestParam String conteudo,
            @RequestParam CategoriaComunidade categoria,
            @RequestParam(required = false) String tags) {
        var post = comunidadeService.criarPost(usuario, titulo, conteudo, categoria, tags);
        return "redirect:/comunidade/post/" + post.getId();
    }

    /**
     * Editar post.
     */
    @PostMapping("/post/{id}/editar")
    public String editarPost(@PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam String titulo,
            @RequestParam String conteudo) {
        comunidadeService.editarPost(id, usuario, titulo, conteudo);
        return "redirect:/comunidade/post/" + id;
    }

    /**
     * Deletar post.
     */
    @PostMapping("/post/{id}/deletar")
    public String deletarPost(@PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        comunidadeService.deletarPost(id, usuario);
        return "redirect:/comunidade";
    }

    // ==================== LIKES ====================

    /**
     * Like/unlike em post.
     */
    @PostMapping("/post/{id}/like")
    @ResponseBody
    public Map<String, Object> likePost(@PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        boolean curtiu = comunidadeService.toggleLikePost(id, usuario);
        var post = comunidadeService.getPost(id).orElseThrow();
        return Map.of("curtiu", curtiu, "likes", post.getLikes());
    }

    /**
     * Like/unlike em comentário.
     */
    @PostMapping("/comentario/{id}/like")
    @ResponseBody
    public Map<String, Object> likeComentario(@PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        boolean curtiu = comunidadeService.toggleLikeComentario(id, usuario);
        return Map.of("curtiu", curtiu);
    }

    // ==================== COMENTÁRIOS ====================

    /**
     * Adicionar comentário.
     */
    @PostMapping("/post/{id}/comentar")
    public String comentar(@PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam String conteudo) {
        comunidadeService.comentar(id, usuario, conteudo);
        return "redirect:/comunidade/post/" + id;
    }

    /**
     * Deletar comentário.
     */
    @PostMapping("/comentario/{id}/deletar")
    public String deletarComentario(@PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam Long postId) {
        comunidadeService.deletarComentario(id, usuario);
        return "redirect:/comunidade/post/" + postId;
    }

    // ==================== MEUS POSTS ====================

    @GetMapping("/meus-posts")
    public String meusPosts(@AuthenticationPrincipal Usuario usuario, Model model) {
        model.addAttribute("posts", comunidadeService.getMeusPosts(usuario));
        return "comunidade/meus-posts";
    }
}

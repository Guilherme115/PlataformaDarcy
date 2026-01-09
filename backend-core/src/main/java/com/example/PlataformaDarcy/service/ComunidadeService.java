package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Serviço da Comunidade.
 */
@Service
public class ComunidadeService {

    @Autowired
    private PostComunidadeRepository postRepo;
    @Autowired
    private ComentarioComunidadeRepository comentarioRepo;

    private static final int PAGE_SIZE = 20;

    // ==================== POSTS ====================

    /**
     * Feed geral (todos os posts).
     */
    public Page<PostComunidade> getFeed(int page) {
        return postRepo.findByAtivoTrueOrderByCriadoEmDesc(PageRequest.of(page, PAGE_SIZE));
    }

    /**
     * Feed por categoria.
     */
    public Page<PostComunidade> getFeedPorCategoria(CategoriaComunidade cat, int page) {
        return postRepo.findByCategoriaAndAtivoTrueOrderByCriadoEmDesc(cat, PageRequest.of(page, PAGE_SIZE));
    }

    /**
     * Posts em trending (mais curtidos).
     */
    public Page<PostComunidade> getTrending(int page) {
        return postRepo.findTrending(PageRequest.of(page, PAGE_SIZE));
    }

    /**
     * Buscar posts.
     */
    public Page<PostComunidade> buscar(String termo, int page) {
        return postRepo.buscar(termo, PageRequest.of(page, PAGE_SIZE));
    }

    /**
     * Obter post por ID.
     */
    public Optional<PostComunidade> getPost(Long id) {
        return postRepo.findById(id);
    }

    /**
     * Criar novo post.
     */
    @Transactional
    public PostComunidade criarPost(Usuario autor, String titulo, String conteudo,
            CategoriaComunidade categoria, String tags) {
        PostComunidade post = new PostComunidade();
        post.setAutor(autor);
        post.setTitulo(titulo.trim());
        post.setConteudo(conteudo);
        post.setCategoria(categoria);
        post.setTags(tags);
        post.setCriadoEm(LocalDateTime.now());

        return postRepo.save(post);
    }

    /**
     * Editar post (apenas autor).
     */
    @Transactional
    public PostComunidade editarPost(Long postId, Usuario usuario, String titulo, String conteudo) {
        PostComunidade post = postRepo.findById(postId).orElseThrow();

        if (!post.getAutor().getId().equals(usuario.getId())) {
            throw new RuntimeException("Sem permissão para editar");
        }

        post.setTitulo(titulo.trim());
        post.setConteudo(conteudo);
        post.setEditadoEm(LocalDateTime.now());

        return postRepo.save(post);
    }

    /**
     * Deletar post (soft delete).
     */
    @Transactional
    public void deletarPost(Long postId, Usuario usuario) {
        PostComunidade post = postRepo.findById(postId).orElseThrow();

        // Permite autor ou admin
        if (!post.getAutor().getId().equals(usuario.getId()) &&
                !"ADMIN".equals(usuario.getPerfil())) {
            throw new RuntimeException("Sem permissão para deletar");
        }

        post.setAtivo(false);
        postRepo.save(post);
    }

    // ==================== LIKES ====================

    /**
     * Toggle like em post.
     */
    @Transactional
    public boolean toggleLikePost(Long postId, Usuario usuario) {
        PostComunidade post = postRepo.findById(postId).orElseThrow();

        boolean jaCurtiu = post.getUsuariosLike().stream()
                .anyMatch(u -> u.getId().equals(usuario.getId()));

        if (jaCurtiu) {
            post.getUsuariosLike().removeIf(u -> u.getId().equals(usuario.getId()));
            post.decrementarLikes();
        } else {
            post.getUsuariosLike().add(usuario);
            post.incrementarLikes();
        }

        postRepo.save(post);
        return !jaCurtiu; // retorna true se curtiu, false se descurtiu
    }

    /**
     * Toggle like em comentário.
     */
    @Transactional
    public boolean toggleLikeComentario(Long comentarioId, Usuario usuario) {
        ComentarioComunidade com = comentarioRepo.findById(comentarioId).orElseThrow();

        boolean jaCurtiu = com.getUsuariosLike().stream()
                .anyMatch(u -> u.getId().equals(usuario.getId()));

        if (jaCurtiu) {
            com.getUsuariosLike().removeIf(u -> u.getId().equals(usuario.getId()));
            com.decrementarLikes();
        } else {
            com.getUsuariosLike().add(usuario);
            com.incrementarLikes();
        }

        comentarioRepo.save(com);
        return !jaCurtiu;
    }

    // ==================== COMENTÁRIOS ====================

    /**
     * Obter comentários de um post.
     */
    public List<ComentarioComunidade> getComentarios(PostComunidade post) {
        return comentarioRepo.findByPostAndAtivoTrueOrderByCriadoEmAsc(post);
    }

    /**
     * Adicionar comentário.
     */
    @Transactional
    public ComentarioComunidade comentar(Long postId, Usuario autor, String conteudo) {
        PostComunidade post = postRepo.findById(postId).orElseThrow();

        ComentarioComunidade com = new ComentarioComunidade();
        com.setPost(post);
        com.setAutor(autor);
        com.setConteudo(conteudo.trim());
        com.setCriadoEm(LocalDateTime.now());

        post.incrementarComentarios();
        postRepo.save(post);

        return comentarioRepo.save(com);
    }

    /**
     * Deletar comentário.
     */
    @Transactional
    public void deletarComentario(Long comentarioId, Usuario usuario) {
        ComentarioComunidade com = comentarioRepo.findById(comentarioId).orElseThrow();

        if (!com.getAutor().getId().equals(usuario.getId()) &&
                !"ADMIN".equals(usuario.getPerfil())) {
            throw new RuntimeException("Sem permissão");
        }

        com.setAtivo(false);
        comentarioRepo.save(com);
    }

    // ==================== ESTATÍSTICAS ====================

    /**
     * Contagem por categoria.
     */
    public Map<CategoriaComunidade, Long> getContagemPorCategoria() {
        return Map.of(
                CategoriaComunidade.PAS_1, postRepo.countByCategoriaAndAtivoTrue(CategoriaComunidade.PAS_1),
                CategoriaComunidade.PAS_2, postRepo.countByCategoriaAndAtivoTrue(CategoriaComunidade.PAS_2),
                CategoriaComunidade.PAS_3, postRepo.countByCategoriaAndAtivoTrue(CategoriaComunidade.PAS_3),
                CategoriaComunidade.INTERVALO, postRepo.countByCategoriaAndAtivoTrue(CategoriaComunidade.INTERVALO));
    }

    /**
     * Posts do usuário.
     */
    public List<PostComunidade> getMeusPosts(Usuario usuario) {
        return postRepo.findByAutorAndAtivoTrueOrderByCriadoEmDesc(usuario);
    }
}

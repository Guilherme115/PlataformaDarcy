package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.Usuario;
import com.example.PlataformaDarcy.model.WikiFavorito;
import com.example.PlataformaDarcy.model.WikiPost;
import com.example.PlataformaDarcy.model.WikiPost.TipoConteudo;
import com.example.PlataformaDarcy.repository.WikiFavoritoRepository;
import com.example.PlataformaDarcy.repository.WikiPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class WikiService {

    @Autowired
    private WikiPostRepository postRepo;
    @Autowired
    private WikiFavoritoRepository favoritoRepo;

    // ==================== CATÁLOGO ====================

    /**
     * Carrega dados para a página principal do catálogo (Netflix-style)
     */
    public Map<String, Object> carregarCatalogo() {
        Map<String, Object> data = new HashMap<>();

        // Carrosséis por disciplina
        data.put("populares", postRepo.findTop6ByRascunhoFalseOrderByVisualizacoesDesc());
        data.put("recentes", postRepo.findTop6ByRascunhoFalseOrderByDataCriacaoDesc());

        // Por tipo de conteúdo
        data.put("videos", postRepo.findByTipoConteudoAndRascunhoFalseOrderByVisualizacoesDesc(TipoConteudo.VIDEO));
        data.put("resumos", postRepo.findByTipoConteudoAndRascunhoFalseOrderByVisualizacoesDesc(TipoConteudo.RESUMO));

        // Por etapa (destaques)
        data.put("pas1", postRepo.findTop3ByEtapaAndRascunhoFalseOrderByCurtidasDesc(1));
        data.put("pas2", postRepo.findTop3ByEtapaAndRascunhoFalseOrderByCurtidasDesc(2));
        data.put("pas3", postRepo.findTop3ByEtapaAndRascunhoFalseOrderByCurtidasDesc(3));

        return data;
    }

    /**
     * Hub de uma disciplina específica
     */
    public Map<String, Object> carregarHubDisciplina(String disciplina) {
        Map<String, Object> data = new HashMap<>();

        data.put("disciplina", disciplina);
        data.put("todos", postRepo.findByDisciplinaAndRascunhoFalseOrderByCurtidasDesc(disciplina));
        data.put("artigos", postRepo.findByDisciplinaAndRascunhoFalseOrderByCurtidasDesc(disciplina)
                .stream().filter(p -> p.getTipoConteudo() == TipoConteudo.ARTIGO).toList());
        data.put("videos", postRepo.findByDisciplinaAndRascunhoFalseOrderByCurtidasDesc(disciplina)
                .stream().filter(p -> p.getTipoConteudo() == TipoConteudo.VIDEO).toList());

        return data;
    }

    /**
     * Busca por termo
     */
    public List<WikiPost> buscar(String termo) {
        if (termo == null || termo.isBlank()) {
            return postRepo.findTop6ByRascunhoFalseOrderByDataCriacaoDesc();
        }
        return postRepo.buscarPorTermo(termo);
    }

    // ==================== LEITURA ====================

    /**
     * Incrementa visualização e retorna o post
     */
    @Transactional
    public WikiPost abrirParaLeitura(Long id) {
        WikiPost post = postRepo.findById(id).orElseThrow();
        post.setVisualizacoes(post.getVisualizacoes() + 1);
        return postRepo.save(post);
    }

    /**
     * Posts relacionados (mesma disciplina)
     */
    public List<WikiPost> buscarRelacionados(WikiPost post) {
        return postRepo.findTop5ByDisciplinaAndIdNotAndRascunhoFalseOrderByCurtidasDesc(
                post.getDisciplina(), post.getId());
    }

    // ==================== FAVORITOS ====================

    @Transactional
    public boolean toggleFavorito(WikiPost post, Usuario usuario) {
        Optional<WikiFavorito> existing = favoritoRepo.findByUsuarioAndPost(usuario, post);
        if (existing.isPresent()) {
            favoritoRepo.delete(existing.get());
            return false; // Removido
        } else {
            WikiFavorito fav = new WikiFavorito();
            fav.setUsuario(usuario);
            fav.setPost(post);
            favoritoRepo.save(fav);
            return true; // Adicionado
        }
    }

    public boolean isFavorito(WikiPost post, Usuario usuario) {
        return favoritoRepo.existsByUsuarioAndPost(usuario, post);
    }

    public List<WikiPost> listarFavoritos(Usuario usuario) {
        return favoritoRepo.findByUsuarioOrderByDataFavoritoDesc(usuario)
                .stream().map(WikiFavorito::getPost).toList();
    }

    // ==================== CURTIDAS ====================

    @Transactional
    public int curtir(Long postId) {
        WikiPost post = postRepo.findById(postId).orElseThrow();
        post.setCurtidas(post.getCurtidas() + 1);
        postRepo.save(post);
        return post.getCurtidas();
    }

    // ==================== CRIAÇÃO/EDIÇÃO ====================

    /**
     * Salva ou atualiza um post
     */
    @Transactional
    public WikiPost salvarPost(WikiPost post, Usuario autor) {
        if (post.getId() == null) {
            post.setAutor(autor);
        }
        return postRepo.save(post);
    }

    /**
     * Publica um rascunho
     */
    @Transactional
    public WikiPost publicar(Long postId) {
        WikiPost post = postRepo.findById(postId).orElseThrow();
        post.setRascunho(false);
        return postRepo.save(post);
    }

    /**
     * Meus posts (autor)
     */
    public List<WikiPost> meusPostsPublicados(Usuario autor) {
        return postRepo.findByAutorOrderByDataCriacaoDesc(autor)
                .stream().filter(p -> !p.isRascunho()).toList();
    }

    public List<WikiPost> meusRascunhos(Usuario autor) {
        return postRepo.findByAutorAndRascunhoTrueOrderByDataAtualizacaoDesc(autor);
    }

    /**
     * Deleta um post
     */
    @Transactional
    public void deletar(Long postId, Usuario usuario) {
        WikiPost post = postRepo.findById(postId).orElseThrow();
        if (post.getAutor().getId().equals(usuario.getId())) {
            postRepo.delete(post);
        }
    }
}

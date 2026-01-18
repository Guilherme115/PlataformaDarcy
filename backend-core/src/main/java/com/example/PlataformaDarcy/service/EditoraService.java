package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EditoraService {

    @Autowired
    private ColecaoRepository colecaoRepository;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private VolumeRepository volumeRepository;

    @Autowired
    private PaginaLivroRepository paginaLivroRepository;

    @Autowired
    private NotaMarginalRepository notaMarginalRepository;

    @Autowired
    private TemplatePaginaRepository templatePaginaRepository;

    // ==================== COLEÇÕES ====================

    public List<Colecao> listarColecoes() {
        return colecaoRepository.findAllByOrderByOrdemAsc();
    }

    public List<Colecao> listarColecoesAtivas() {
        return colecaoRepository.findByAtivaTrueOrderByOrdemAsc();
    }

    public Optional<Colecao> buscarColecaoPorId(Long id) {
        return colecaoRepository.findById(id);
    }

    public Colecao criarColecao(String nome, String descricao, Usuario criador) {
        Colecao colecao = new Colecao(nome, descricao);
        colecao.setCriadoPor(criador);
        return colecaoRepository.save(colecao);
    }

    public Colecao atualizarColecao(Long id, String nome, String descricao, String imagemCapa) {
        Colecao colecao = colecaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coleção não encontrada"));

        colecao.setNome(nome);
        colecao.setDescricao(descricao);
        if (imagemCapa != null) {
            colecao.setImagemCapa(imagemCapa);
        }

        return colecaoRepository.save(colecao);
    }

    public void toggleAtivaColecao(Long id) {
        Colecao colecao = colecaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coleção não encontrada"));
        colecao.setAtiva(!colecao.getAtiva());
        colecaoRepository.save(colecao);
    }

    public void deletarColecao(Long id) {
        colecaoRepository.deleteById(id);
    }

    // ==================== LIVROS ====================

    public List<Livro> listarLivrosPorColecao(Long colecaoId) {
        return livroRepository.findByColecaoIdOrderByOrdemAsc(colecaoId);
    }

    public List<Livro> listarLivrosAtivosPorColecao(Long colecaoId) {
        return livroRepository.findByColecaoIdAndAtivoTrueOrderByOrdemAsc(colecaoId);
    }

    public Optional<Livro> buscarLivroPorId(Long id) {
        return livroRepository.findById(id);
    }

    public Livro criarLivro(Long colecaoId, String titulo, String subtitulo) {
        Colecao colecao = colecaoRepository.findById(colecaoId)
                .orElseThrow(() -> new RuntimeException("Coleção não encontrada"));

        Livro livro = new Livro(titulo, colecao);
        livro.setSubtitulo(subtitulo);

        return livroRepository.save(livro);
    }

    public Livro atualizarLivro(Long id, String titulo, String subtitulo, String imagemCapa, String cor) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado"));

        livro.setTitulo(titulo);
        livro.setSubtitulo(subtitulo);
        if (imagemCapa != null) {
            livro.setImagemCapa(imagemCapa);
        }
        if (cor != null) {
            livro.setCor(cor);
        }

        return livroRepository.save(livro);
    }

    public void toggleAtivoLivro(Long id) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado"));
        livro.setAtivo(!livro.getAtivo());
        livroRepository.save(livro);
    }

    public void deletarLivro(Long id) {
        livroRepository.deleteById(id);
    }

    // ==================== VOLUMES ====================

    public List<Volume> listarVolumesPorLivro(Long livroId) {
        return volumeRepository.findByLivroIdOrderByNumeroAsc(livroId);
    }

    public List<Volume> listarVolumesAtivosPorLivro(Long livroId) {
        return volumeRepository.findByLivroIdAndAtivoTrueOrderByNumeroAsc(livroId);
    }

    public Optional<Volume> buscarVolumePorId(Long id) {
        return volumeRepository.findById(id);
    }

    public Volume criarVolume(Long livroId, Integer numero, String titulo, String descricao) {
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado"));

        Volume volume = new Volume(livro, numero, titulo);
        volume.setDescricao(descricao);

        return volumeRepository.save(volume);
    }

    public Volume atualizarVolume(Long id, String titulo, String descricao) {
        Volume volume = volumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Volume não encontrado"));

        volume.setTitulo(titulo);
        volume.setDescricao(descricao);

        return volumeRepository.save(volume);
    }

    public void toggleAtivoVolume(Long id) {
        Volume volume = volumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Volume não encontrado"));
        volume.setAtivo(!volume.getAtivo());
        volumeRepository.save(volume);
    }

    public void deletarVolume(Long id) {
        volumeRepository.deleteById(id);
    }

    // ==================== PÁGINAS (CORE) ====================

    public List<PaginaLivro> listarPaginasPorVolume(Long volumeId) {
        return paginaLivroRepository.findByVolumeIdOrderByNumeroPaginaAsc(volumeId);
    }

    public List<PaginaLivro> listarPaginasAtivasPorVolume(Long volumeId) {
        return paginaLivroRepository.findByVolumeIdAndAtivaTrueOrderByNumeroPaginaAsc(volumeId);
    }

    public Optional<PaginaLivro> buscarPaginaPorId(Long id) {
        return paginaLivroRepository.findById(id);
    }

    public PaginaLivro criarPagina(Long volumeId, String titulo, String modulo, LayoutPagina layout, Usuario editor) {
        Volume volume = volumeRepository.findById(volumeId)
                .orElseThrow(() -> new RuntimeException("Volume não encontrado"));

        // Calcular próximo número de página
        Integer maxNumero = paginaLivroRepository.findMaxNumeroPaginaByVolumeId(volumeId);
        Integer proximoNumero = (maxNumero == null) ? 1 : maxNumero + 1;

        PaginaLivro pagina = new PaginaLivro(volume, proximoNumero, titulo);
        pagina.setModulo(modulo);
        pagina.setLayout(layout);
        pagina.setHtmlContent("<p class=\"font-serif text-lg\">Comece a escrever aqui...</p>");
        pagina.setUltimaEdicaoPor(editor);

        return paginaLivroRepository.save(pagina);
    }

    public PaginaLivro atualizarPagina(Long id, String titulo, String modulo, LayoutPagina layout,
            String htmlContent, Usuario editor) {
        PaginaLivro pagina = paginaLivroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Página não encontrada"));

        pagina.setTitulo(titulo);
        pagina.setModulo(modulo);
        pagina.setLayout(layout);

        if (htmlContent != null) {
            pagina.setHtmlContent(htmlContent);
            // Incrementar versão quando o conteúdo HTML é alterado
            pagina.setVersao(pagina.getVersao() + 1);
        }

        pagina.setUltimaEdicaoPor(editor);

        return paginaLivroRepository.save(pagina);
    }

    public PaginaLivro duplicarPagina(Long id, Usuario editor) {
        PaginaLivro original = paginaLivroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Página não encontrada"));

        // Calcular próximo número
        Integer maxNumero = paginaLivroRepository.findMaxNumeroPaginaByVolumeId(original.getVolume().getId());
        Integer proximoNumero = (maxNumero == null) ? 1 : maxNumero + 1;

        PaginaLivro copia = new PaginaLivro(original.getVolume(), proximoNumero, original.getTitulo() + " (Cópia)");
        copia.setModulo(original.getModulo());
        copia.setLayout(original.getLayout());
        copia.setHtmlContent(original.getHtmlContent());
        copia.setUltimaEdicaoPor(editor);

        return paginaLivroRepository.save(copia);
    }

    public void deletarPagina(Long id) {
        paginaLivroRepository.deleteById(id);
    }

    public void reordenarPaginas(Long volumeId, List<Long> novaOrdem) {
        for (int i = 0; i < novaOrdem.size(); i++) {
            Long paginaId = novaOrdem.get(i);
            PaginaLivro pagina = paginaLivroRepository.findById(paginaId)
                    .orElseThrow(() -> new RuntimeException("Página não encontrada: " + paginaId));
            pagina.setNumeroPagina(i + 1);
            paginaLivroRepository.save(pagina);
        }
    }

    // ==================== NOTAS MARGINAIS ====================

    public List<NotaMarginal> listarNotasPorPagina(Long paginaId) {
        return notaMarginalRepository.findByPaginaIdOrderByOrdemAsc(paginaId);
    }

    public NotaMarginal criarNotaMarginal(Long paginaId, TipoNotaMarginal tipo, String titulo, String conteudo) {
        PaginaLivro pagina = paginaLivroRepository.findById(paginaId)
                .orElseThrow(() -> new RuntimeException("Página não encontrada"));

        NotaMarginal nota = new NotaMarginal(pagina, tipo, conteudo);
        nota.setTitulo(titulo);

        return notaMarginalRepository.save(nota);
    }

    public void deletarNotaMarginal(Long id) {
        notaMarginalRepository.deleteById(id);
    }

    // ==================== TEMPLATES ====================

    public List<TemplatePagina> listarTemplatesPublicos() {
        return templatePaginaRepository.findByPublicoTrue();
    }

    public List<TemplatePagina> listarTemplatesDoUsuario(Long usuarioId) {
        return templatePaginaRepository.findPublicosOuDoUsuario(usuarioId);
    }

    public TemplatePagina criarTemplate(String nome, String descricao, LayoutPagina layout,
            String htmlTemplate, Usuario criador) {
        TemplatePagina template = new TemplatePagina(nome, layout, htmlTemplate);
        template.setDescricao(descricao);
        template.setCriadoPor(criador);

        return templatePaginaRepository.save(template);
    }

    public PaginaLivro aplicarTemplate(Long templateId, Long volumeId, Usuario editor) {
        TemplatePagina template = templatePaginaRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template não encontrado"));

        Volume volume = volumeRepository.findById(volumeId)
                .orElseThrow(() -> new RuntimeException("Volume não encontrado"));

        // Calcular próximo número
        Integer maxNumero = paginaLivroRepository.findMaxNumeroPaginaByVolumeId(volumeId);
        Integer proximoNumero = (maxNumero == null) ? 1 : maxNumero + 1;

        PaginaLivro pagina = new PaginaLivro(volume, proximoNumero, "Nova Página");
        pagina.setLayout(template.getLayout());
        pagina.setHtmlContent(template.getHtmlTemplate());
        pagina.setUltimaEdicaoPor(editor);

        return paginaLivroRepository.save(pagina);
    }

    // ==================== ESTATÍSTICAS ====================

    public long contarLivrosPorColecao(Long colecaoId) {
        return livroRepository.countByColecaoId(colecaoId);
    }

    public long contarVolumesPorLivro(Long livroId) {
        return volumeRepository.countByLivroId(livroId);
    }

    public long contarPaginasPorVolume(Long volumeId) {
        return paginaLivroRepository.countByVolumeId(volumeId);
    }
}

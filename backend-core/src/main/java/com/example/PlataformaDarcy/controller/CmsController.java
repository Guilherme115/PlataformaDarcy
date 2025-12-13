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

    @Autowired private BlocoRepository blocoRepo;
    @Autowired private QuestaoRepository questaoRepo;
    @Autowired private ProvaRepository provaRepo;
    @Autowired private ImageService imageService;
    @Autowired private TaxonomyService taxonomyService;

    // TELA PRINCIPAL
    @GetMapping
    public String dashboard(@RequestParam(defaultValue = "1") Integer etapa,
                            @RequestParam(required = false) Integer ano,
                            @RequestParam(required = false) Long blocoId,
                            Model model) {

        model.addAttribute("etapaAtiva", etapa);
        model.addAttribute("anoAtivo", ano);

        try {
            String taxonomyJson = new ObjectMapper().writeValueAsString(taxonomyService.getTaxonomy());
            model.addAttribute("taxonomyJson", taxonomyJson);
        } catch (Exception e) {
            model.addAttribute("taxonomyJson", "{}");
        }

        if (ano == null) {
            List<Integer> anosDisponiveis = provaRepo.findAnosByEtapa(etapa);
            model.addAttribute("anosDisponiveis", anosDisponiveis);
            return "cms-selecao-ano";
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
        return "cms";
    }

    @PostMapping("/salvar-bloco/{id}")
    @ResponseBody
    public String salvarBloco(@PathVariable Long id, @RequestParam String textoBase) {
        Bloco bloco = blocoRepo.findById(id).orElseThrow();
        bloco.setTextoBase(textoBase);
        blocoRepo.save(bloco);
        return "<span class='text-green-600 font-bold text-xs'>TEXTO SALVO!</span>";
    }

    // SALVAR QUESTÃO
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

        if (enunciado != null) q.setEnunciado(enunciado);
        if (tipo != null) q.setTipo(tipo);
        if (gabarito != null) q.setGabarito(gabarito.toUpperCase());
        if (tags != null) q.setTags(tags.toUpperCase());

        if ("REVISADO".equals(statusCheck)) {
            q.setStatus(StatusRevisao.REVISADO);
        } else {
            q.setStatus(StatusRevisao.PENDENTE);
        }

        if (q.getTipo() == TipoQuestao.C) {
            try {
                Map<String, String> map = new HashMap<>();
                map.put("A", altA != null ? altA : q.getAltTexto("A"));
                map.put("B", altB != null ? altB : q.getAltTexto("B"));
                map.put("C", altC != null ? altC : q.getAltTexto("C"));
                map.put("D", altD != null ? altD : q.getAltTexto("D"));
                q.setAlternativas(new ObjectMapper().writeValueAsString(map));
            } catch (Exception e) {}
        }

        questaoRepo.save(q);

        String cor = q.getStatus() == StatusRevisao.REVISADO ? "text-green-600" : "text-orange-500";
        String texto = q.getStatus() == StatusRevisao.REVISADO ? "REVISADO" : "PENDENTE";

        return "<span class='" + cor + " font-bold text-[10px] bg-white border border-black px-2 uppercase tracking-widest'>" + texto + "</span>";
    }

    // --- CARREGAR GALERIA ---
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

        return "cms-modal :: modal-galeria";
    }

    @PostMapping("/vincular-imagem")
    public String vincularImagem(@RequestParam Long qid,
                                 @RequestParam String caminho,
                                 @RequestParam String tag,
                                 Model model) {
        imageService.vincularImagemExistente(qid, caminho, tag);
        Questao q = questaoRepo.findById(qid).orElseThrow();
        model.addAttribute("q", q);
        return "cms :: lista-imagens-questao";
    }

    @PostMapping("/upload-tag")
    public String uploadTag(@RequestParam("file") MultipartFile file,
                            @RequestParam("qid") Long qid,
                            @RequestParam("tag") String tag, Model model) throws IOException {
        imageService.uploadImagemComTag(qid, file, tag);
        Questao q = questaoRepo.findById(qid).orElseThrow();
        model.addAttribute("q", q);
        return "cms :: lista-imagens-questao";
    }

    @DeleteMapping("/imagem/{id}")
    @ResponseBody
    public String deletarImagem(@PathVariable Long id) {
        imageService.excluirImagem(id);
        return "";
    }
}
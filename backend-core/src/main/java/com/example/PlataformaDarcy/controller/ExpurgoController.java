package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.repository.RegistroErroRepository;
import com.example.PlataformaDarcy.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/expurgo")
public class ExpurgoController {

    @Autowired
    private ExpurgoService expurgoService;
    @Autowired
    private AuthService authService;
    @Autowired
    private TaxonomyService taxonomyService;
    @Autowired
    private RegistroErroRepository erroRepo;

    @GetMapping("/central")
    public String dashboard(Model model) {
        Usuario usuario = authService.getUsuarioLogado();
        List<RegistroErro> inbox = expurgoService.getInbox(usuario);

        model.addAttribute("inbox", inbox);
        model.addAttribute("totalPendentes", inbox.size());
        model.addAttribute("criticos", inbox.stream().filter(e -> e.getTemperatura() >= 50).count());

        try {
            model.addAttribute("taxonomyJson", new ObjectMapper().writeValueAsString(taxonomyService.getTaxonomy()));
        } catch (Exception e) {
            model.addAttribute("taxonomyJson", "{}");
        }
        return "simulado/expurgo-central"; // Refatorado: agora dentro da pasta simulado
    }

    /**
     * NOVO: Dashboard principal do Protocolo de Expurgo
     * Mostra análise completa por matéria com sugestões
     */
    @GetMapping("/protocolo")
    public String protocoloDashboard(Model model) {
        Usuario usuario = authService.getUsuarioLogado();

        // Carrega todos os dados do dashboard
        Map<String, Object> dados = expurgoService.getDadosDashboard(usuario);
        model.addAllAttributes(dados);

        return "simulado/protocolo-dashboard";
    }

    @GetMapping("/detalhar/{id}")
    public String detalharErro(@PathVariable Long id, Model model) {
        model.addAttribute("erro", erroRepo.findById(id).orElseThrow());
        return "simulado/expurgo-central :: modal-revisao"; // Fragmento dentro do novo arquivo
    }

    @PostMapping("/confirmar/{id}")
    @ResponseBody
    public String confirmarExpurgo(@PathVariable Long id) {
        expurgoService.expurgarErro(id);
        return "";
    }

    /**
     * RENOMEADO: Agora é /emergencia para gerar simulado direto
     * O /protocolo mostra o dashboard primeiro
     */
    @PostMapping("/emergencia")
    public String iniciarEmergencia() {
        Usuario usuario = authService.getUsuarioLogado();
        Simulado s = expurgoService.gerarProtocoloEmergencia(usuario);

        if (s == null)
            return "redirect:/expurgo/protocolo?msg=sem_criticos";
        return "redirect:/simulado/" + s.getId();
    }

    @PostMapping("/criar-bateria")
    public String criarBateria(@RequestParam(required = false) String materia,
            @RequestParam(required = false) String topico,
            @RequestParam(required = false) Integer etapa,
            @RequestParam(defaultValue = "10") Integer quantidade,
            @RequestParam(defaultValue = "false") boolean usarIA) {

        Usuario usuario = authService.getUsuarioLogado();
        Simulado s = expurgoService.gerarBateriaPersonalizada(usuario, materia, topico, etapa, quantidade, usarIA);

        if (s == null)
            return "redirect:/expurgo/central?msg=sem_erros_filtro";
        return "redirect:/simulado/" + s.getId();
    }
}
package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.model.Comunicado;
import com.example.PlataformaDarcy.repository.ComunicadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/transmissoes")
public class AdminTransmissaoController {

    @Autowired private ComunicadoRepository comunicadoRepo;

    // 1. PÁGINA DE GESTÃO (Carrega a lista e o form)
    @GetMapping
    public String paginaGestao(Model model) {
        model.addAttribute("comunicados", comunicadoRepo.findAllByOrderByDataEnvioDesc());
        return "admin-transmissoes";
    }

    // 2. SALVAR NOVO AVISO
    @PostMapping("/salvar")
    public String salvar(@RequestParam String titulo,
                         @RequestParam String mensagem,
                         @RequestParam String tipo,
                         @RequestParam(required = false) boolean ativo) {

        Comunicado c = new Comunicado();
        c.setTitulo(titulo);
        c.setMensagem(mensagem);
        c.setTipo(tipo); // INFO, ALERTA, EVENTO
        c.setAtivo(ativo);
        c.setDataEnvio(LocalDateTime.now());

        comunicadoRepo.save(c);

        return "redirect:/admin/transmissoes";
    }

    // 3. LIGAR/DESLIGAR (HTMX) - Permite múltiplos ativos
    @PostMapping("/{id}/toggle")
    public String toggleStatus(@PathVariable Long id, Model model) {
        Comunicado c = comunicadoRepo.findById(id).orElseThrow();
        c.setAtivo(!c.isAtivo());
        comunicadoRepo.save(c);

        // Retorna apenas o botão atualizado para o HTMX
        model.addAttribute("c", c);
        return "admin-transmissoes :: botao-status";
    }

    // 4. EXCLUIR
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id) {
        comunicadoRepo.deleteById(id);
        return "redirect:/admin/transmissoes";
    }
}
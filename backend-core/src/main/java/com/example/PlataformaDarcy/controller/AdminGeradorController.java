package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.service.SimuladoGeradorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/gerador")
public class AdminGeradorController {

    @Autowired
    private SimuladoGeradorService geradorService;

    // Carrega a página HTML do Gerador
    @GetMapping
    public String paginaGerador(Model model) {
        return "admin-gerador"; // Vai procurar admin-gerador.html nos templates
    }

    // Ação do Botão (HTMX chama isso via AJAX)
    @PostMapping("/criar/{etapa}")
    @ResponseBody // Retorna HTML parcial (fragmento) para o HTMX
    public String gerarSimuladoAction(@PathVariable int etapa) {
        try {
            long startTime = System.currentTimeMillis();

            // Chama o Serviço Pesado (que pode demorar uns 10-20 segundos)
            geradorService.gerarSimuladoOficial(etapa);

            long duration = (System.currentTimeMillis() - startTime) / 1000;

            // Retorna um CARD DE SUCESSO estilizado para aparecer na tela
            return """
                <div class="bg-green-100 border-2 border-green-600 p-4 mb-4 shadow-[4px_4px_0px_0px_#166534] animate-fade-in-down">
                    <div class="flex items-center gap-3">
                        <div class="bg-green-600 text-white p-2 font-bold font-mono">OK</div>
                        <div>
                            <h4 class="font-black uppercase text-green-800 text-sm">Sucesso Total!</h4>
                            <p class="font-mono text-xs text-green-700">
                                Simulado <strong>PAS %d</strong> gerado em %d segundos.
                                <br>Já está disponível no Banco de Dados.
                            </p>
                        </div>
                    </div>
                </div>
                """.formatted(etapa, duration);

        } catch (Exception e) {
            e.printStackTrace();
            // Retorna um CARD DE ERRO estilizado
            return """
                <div class="bg-red-100 border-2 border-red-600 p-4 mb-4 shadow-[4px_4px_0px_0px_#991b1b]">
                    <div class="flex items-center gap-3">
                        <div class="bg-red-600 text-white p-2 font-bold font-mono">ERRO</div>
                        <div>
                            <h4 class="font-black uppercase text-red-800 text-sm">Falha na Geração</h4>
                            <p class="font-mono text-xs text-red-700">%s</p>
                        </div>
                    </div>
                </div>
                """.formatted(e.getMessage());
        }
    }
}
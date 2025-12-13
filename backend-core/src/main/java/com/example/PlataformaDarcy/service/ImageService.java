package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.ImagemQuestao;
import com.example.PlataformaDarcy.model.Questao;
import com.example.PlataformaDarcy.repository.ImagemQuestaoRepository;
import com.example.PlataformaDarcy.repository.QuestaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImageService {

    @Value("${app.images.path}")
    private String rootPath;

    @Autowired private ImagemQuestaoRepository imgRepo;
    @Autowired private QuestaoRepository questaoRepo;

    public void excluirImagem(Long id) {
        imgRepo.deleteById(id);
    }

    public List<String> listarImagensDisponiveis(Integer ano, Integer etapa) {
        String pastaNome = ano + "_" + etapa;

        String cleanRoot = rootPath.endsWith("/") ? rootPath.substring(0, rootPath.length() - 1) : rootPath;
        File diretorio = new File(cleanRoot, pastaNome);

        System.out.println("==================================================");
        System.out.println("🔍 [GALERIA DEBUG]");
        System.out.println("📂 Pasta Alvo: " + diretorio.getAbsolutePath());

        if (!diretorio.exists()) {
            System.out.println("❌ ERRO: A pasta não existe!");
            return Collections.emptyList();
        }

        File[] arquivos = diretorio.listFiles((dir, name) -> {
            String n = name.toLowerCase();
            return n.endsWith(".png") ||
                    n.endsWith(".jpg") ||
                    n.endsWith(".jpeg") || // <--- O PULO DO GATO
                    n.endsWith(".gif") ||
                    n.endsWith(".bmp") ||
                    n.endsWith(".webp");
        });

        if (arquivos == null || arquivos.length == 0) {
            System.out.println("⚠️ AVISO: Pasta vazia ou arquivos com extensão desconhecida.");
            String[] todosArquivos = diretorio.list();
            if (todosArquivos != null) {
                System.out.println("   👉 Arquivos ignorados (extensão errada?):");
                for (String f : todosArquivos) System.out.println("      - " + f);
            }
            return Collections.emptyList();
        }

        System.out.println("🚀 ENCONTRADOS " + arquivos.length + " IMAGENS VÁLIDAS.");
        System.out.println("==================================================");

        return Arrays.stream(arquivos)
                .map(f -> pastaNome + "/" + f.getName())
                .sorted()
                .collect(Collectors.toList());
    }

    public void vincularImagemExistente(Long questaoId, String caminhoRelativo, String tag) {
        Questao questao = questaoRepo.findById(questaoId).orElseThrow();
        ImagemQuestao novaImg = new ImagemQuestao();
        novaImg.setCaminhoArquivo(caminhoRelativo);
        novaImg.setQuestao(questao);
        novaImg.setTag(tag);
        imgRepo.save(novaImg);
    }

    public void uploadImagemComTag(Long questaoId, MultipartFile file, String tag) throws IOException {
        if (file.isEmpty()) return;

        Questao questao = questaoRepo.findById(questaoId).orElseThrow();
        String originalName = file.getOriginalFilename();
        String extensao = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".")) : ".png";

        String nomeArquivo = "upload_" + tag + "_" + System.currentTimeMillis() + extensao;

        String subPasta = "";
        if(questao.getProva() != null) {
            subPasta = questao.getProva().getAno() + "_" + questao.getProva().getEtapa();
        } else {
            subPasta = "uploads_gerais";
        }

        File destino = new File(rootPath + "/" + subPasta, nomeArquivo);
        destino.getParentFile().mkdirs();
        file.transferTo(destino);

        ImagemQuestao novaImg = new ImagemQuestao();
        novaImg.setCaminhoArquivo(subPasta + "/" + nomeArquivo);
        novaImg.setQuestao(questao);
        novaImg.setTag(tag);
        imgRepo.save(novaImg);
    }
}
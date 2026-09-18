package com.medic.ETL.service.anvisa;

import com.medic.ETL.model.processamento.Processamento;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;

@Service
public class AtualizarAnvisaService {

    private final AnvisaDownloadService downloadService;
    private final AnvisaCargaService cargaService;

    public AtualizarAnvisaService(AnvisaDownloadService downloadService,
                                  AnvisaCargaService cargaService) {
        this.downloadService = downloadService;
        this.cargaService = cargaService;
    }

    public void atualizar(Processamento processamento) throws IOException, InterruptedException {

        AnvisaDownloadService.ArquivosAnvisa arquivos = downloadService.baixar();

        try {
            cargaService.promover(processamento, arquivos.produtos(), arquivos.modelos());

        } finally {

            excluirTemporario(arquivos.produtos());
            excluirTemporario(arquivos.modelos());
        }
    }

    private void excluirTemporario(java.nio.file.Path arquivo) {

        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException exception) {
            // A carga ja foi promovida ou falhou por sua propria causa; o arquivo temporario nao altera esse resultado.
        }
    }
}

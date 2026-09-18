package com.medic.ETL.service.anvisa;

import com.medic.ETL.model.processamento.Processamento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

class AtualizarAnvisaServiceTest {

    @Test
    void shouldPromoteTheDownloadAndDeleteBothTemporaryFiles(@TempDir Path tempDir) throws Exception {

        AnvisaDownloadService downloadService = mock(AnvisaDownloadService.class);
        AnvisaCargaService cargaService = mock(AnvisaCargaService.class);
        Path produtos = Files.createFile(tempDir.resolve("produtos.csv"));
        Path modelos = Files.createFile(tempDir.resolve("modelos.csv"));
        when(downloadService.baixar()).thenReturn(new AnvisaDownloadService.ArquivosAnvisa(produtos, modelos));

        Processamento processamento = processamento();
        new AtualizarAnvisaService(downloadService, cargaService).atualizar(processamento);

        verify(cargaService).promover(processamento, produtos, modelos);
        assertFalse(Files.exists(produtos));
        assertFalse(Files.exists(modelos));
    }

    @Test
    void shouldDeleteTemporaryFilesWhenPromotionFails(@TempDir Path tempDir) throws Exception {

        AnvisaDownloadService downloadService = mock(AnvisaDownloadService.class);
        AnvisaCargaService cargaService = mock(AnvisaCargaService.class);
        Path produtos = Files.createFile(tempDir.resolve("produtos.csv"));
        Path modelos = Files.createFile(tempDir.resolve("modelos.csv"));
        when(downloadService.baixar()).thenReturn(new AnvisaDownloadService.ArquivosAnvisa(produtos, modelos));
        doThrow(new IOException("carga falhou")).when(cargaService).promover(anyProcessamento(), eq(produtos), eq(modelos));

        assertThrows(IOException.class, () -> new AtualizarAnvisaService(downloadService, cargaService)
                .atualizar(processamento()));
        assertFalse(Files.exists(produtos));
        assertFalse(Files.exists(modelos));
    }

    @Test
    void shouldIgnoreFailuresWhileDeletingNonEmptyTemporaryDirectories(@TempDir Path tempDir) throws Exception {

        AnvisaDownloadService downloadService = mock(AnvisaDownloadService.class);
        AnvisaCargaService cargaService = mock(AnvisaCargaService.class);
        Path produtos = Files.createDirectory(tempDir.resolve("produtos"));
        Path modelos = Files.createDirectory(tempDir.resolve("modelos"));
        Files.createFile(produtos.resolve("keep"));
        Files.createFile(modelos.resolve("keep"));
        when(downloadService.baixar()).thenReturn(new AnvisaDownloadService.ArquivosAnvisa(produtos, modelos));

        new AtualizarAnvisaService(downloadService, cargaService).atualizar(processamento());
    }

    @Test
    void shouldPropagateDownloadFailures() throws Exception {

        AnvisaDownloadService downloadService = mock(AnvisaDownloadService.class);
        AnvisaCargaService cargaService = mock(AnvisaCargaService.class);
        when(downloadService.baixar()).thenThrow(new IOException("download falhou"));

        assertThrows(IOException.class, () -> new AtualizarAnvisaService(downloadService, cargaService)
                .atualizar(processamento()));
    }

    private Processamento processamento() {

        Processamento processamento = new Processamento();
        processamento.setId(UUID.randomUUID());
        return processamento;
    }

    private Processamento anyProcessamento() {

        return org.mockito.ArgumentMatchers.any(Processamento.class);
    }
}

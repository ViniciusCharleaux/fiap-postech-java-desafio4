package org.acme.function;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.dto.RelatorioSemanalDTO;
import org.acme.service.RelatorioService;

import java.time.LocalDateTime;

@Path("/relatorio")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class WeeklyReportFunction {

    @Inject
    RelatorioService relatorioService;

    @Scheduled(cron = "{feedback.relatorio.cron}")
    void executarRelatorioAgendado() {
        relatorioService.gerarEEnviarRelatorioSemanal();
    }

    @POST
    @Path("/gerar")
    public Response gerarRelatorioManual() {
        relatorioService.gerarEEnviarRelatorioSemanal();
        return Response.ok("Relatório gerado e enviado com sucesso").build();
    }

    @GET
    @Path("/preview")
    public Response previewRelatorio() {
        LocalDateTime fim = LocalDateTime.now();
        LocalDateTime inicio = fim.minusDays(7);
        RelatorioSemanalDTO relatorio = relatorioService.gerarRelatorio(inicio, fim);
        return Response.ok(relatorio).build();
    }
}
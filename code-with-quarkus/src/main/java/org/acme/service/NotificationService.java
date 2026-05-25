package org.acme.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.RelatorioSemanalDTO;
import org.acme.entity.Feedback;
import org.acme.enums.Urgencia;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;

@ApplicationScoped
public class NotificationService {

    @Inject
    Mailer mailer;

    @ConfigProperty(name = "feedback.admin.email", defaultValue = "admin@tc4feedback.com")
    String adminEmail;

    public void enviarNotificacaoCritica(Feedback feedback) {
        String assunto = "[URGENTE] Feedback crítico recebido";
        String corpo = construirCorpoNotificacaoCritica(feedback);

        mailer.send(Mail.withText(adminEmail, assunto, corpo));
    }

    public void enviarRelatorioSemanal(RelatorioSemanalDTO relatorio) {
        String assunto = "[RELATÓRIO SEMANAL] Feedbacks - " + relatorio.periodoInicio + " a " + relatorio.periodoFim;
        String corpo = construirCorpoRelatorio(relatorio);

        mailer.send(Mail.withText(adminEmail, assunto, corpo));
    }

    private String construirCorpoNotificacaoCritica(Feedback feedback) {
        return """
                ⚠️ FEEDBACK CRÍTICO RECEBIDO
                
                Descrição: %s
                Urgência: %s
                Data de envio: %s
                Nota: %d
                
                Por favor, tome as medidas necessárias.
                """.formatted(
                feedback.descricao,
                feedback.urgencia.name(),
                feedback.dataEnvio.toString(),
                feedback.nota
        );
    }

    private String construirCorpoRelatorio(RelatorioSemanalDTO relatorio) {
        StringBuilder sb = new StringBuilder();

        sb.append("📊 RELATÓRIO SEMANAL DE FEEDBACKS\n");
        sb.append("=".repeat(40)).append("\n\n");
        sb.append("Período: ").append(relatorio.periodoInicio).append(" a ").append(relatorio.periodoFim).append("\n");
        sb.append("Total de avaliações: ").append(relatorio.totalAvaliacoes).append("\n");
        sb.append("Média das notas: ").append(String.format("%.2f", relatorio.mediaNotas)).append("\n\n");

        sb.append("📅 Avaliações por dia:\n");
        for (Map.Entry<String, Long> entry : relatorio.avaliacoesPorDia.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        sb.append("\n🚨 Avaliações por urgência:\n");
        for (Map.Entry<Urgencia, Long> entry : relatorio.avaliacoesPorUrgencia.entrySet()) {
            sb.append("  ").append(entry.getKey().name()).append(": ").append(entry.getValue()).append("\n");
        }

        return sb.toString();
    }
}
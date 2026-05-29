package org.acme.service;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailAddress;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.core.util.polling.SyncPoller;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.RelatorioSemanalDTO;
import org.acme.entity.Feedback;
import org.acme.enums.Urgencia;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@ApplicationScoped
public class NotificationService {

    private static final Logger LOG = Logger.getLogger(NotificationService.class.getName());

    @Inject
    Mailer mailer;

    @ConfigProperty(name = "feedback.admin.email", defaultValue = "admin@tc4feedback.com")
    String adminEmail;

    @ConfigProperty(name = "azure.communication.connection-string")
    Optional<String> azureCommunicationConnectionString;

    @ConfigProperty(name = "quarkus.mailer.mock", defaultValue = "true")
    boolean mailerMock;

    public void enviarNotificacaoCritica(Feedback feedback) {
        String assunto = "[URGENTE] Feedback crítico recebido - Nota: " + feedback.nota;
        String corpo = construirCorpoNotificacaoCritica(feedback);

        if (deveUsarAzureCommunication()) {
            enviarViaAzureCommunication(assunto, corpo);
        } else {
            enviarViaMailer(assunto, corpo);
        }

        LOG.info("Notificação crítica enviada para: " + adminEmail + " | FeedbackId: " + feedback.id);
    }

    public void enviarRelatorioSemanal(RelatorioSemanalDTO relatorio) {
        String assunto = "[RELATÓRIO SEMANAL] Feedbacks - " + relatorio.periodoInicio + " a " + relatorio.periodoFim;
        String corpo = construirCorpoRelatorio(relatorio);

        if (deveUsarAzureCommunication()) {
            enviarViaAzureCommunication(assunto, corpo);
        } else {
            enviarViaMailer(assunto, corpo);
        }

        LOG.info("Relatório semanal enviado para: " + adminEmail);
    }

    private boolean deveUsarAzureCommunication() {
        return azureCommunicationConnectionString.isPresent()
                && !azureCommunicationConnectionString.get().isBlank()
                && !mailerMock;
    }

    private void enviarViaAzureCommunication(String assunto, String corpo) {
        try {
            EmailClient emailClient = new EmailClientBuilder()
                    .connectionString(azureCommunicationConnectionString.get())
                    .buildClient();

            EmailMessage message = new EmailMessage()
                    .setSenderAddress("DoNotReply@tc4feedback.azurecomm.net")
                    .setToRecipients(new EmailAddress(adminEmail))
                    .setSubject(assunto)
                    .setBodyPlainText(corpo);

            SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message);
            poller.waitForCompletion();

            LOG.info("E-mail enviado via Azure Communication Services.");
        } catch (Exception e) {
            LOG.severe("Erro ao enviar e-mail via Azure Communication Services: " + e.getMessage());
            enviarViaMailer(assunto, corpo);
        }
    }

    private void enviarViaMailer(String assunto, String corpo) {
        mailer.send(Mail.withText(adminEmail, assunto, corpo));
        LOG.info("E-mail enviado via Quarkus Mailer (mock=" + mailerMock + ").");
    }

    private String construirCorpoNotificacaoCritica(Feedback feedback) {
        return """
                ⚠️  FEEDBACK CRÍTICO RECEBIDO
                ==============================

                Descrição : %s
                Urgência  : %s
                Nota      : %d / 10
                Data      : %s

                Por favor, tome as medidas necessárias o mais breve possível.

                -- Sistema de Feedback TC4
                """.formatted(
                feedback.descricao,
                feedback.urgencia.name(),
                feedback.nota,
                feedback.dataEnvio.toString()
        );
    }

    private String construirCorpoRelatorio(RelatorioSemanalDTO relatorio) {
        StringBuilder sb = new StringBuilder();

        sb.append("📊 RELATÓRIO SEMANAL DE FEEDBACKS\n");
        sb.append("=".repeat(40)).append("\n\n");
        sb.append("Período        : ").append(relatorio.periodoInicio).append(" a ").append(relatorio.periodoFim).append("\n");
        sb.append("Total          : ").append(relatorio.totalAvaliacoes).append(" avaliações\n");
        sb.append("Média das notas: ").append(String.format("%.2f", relatorio.mediaNotas)).append(" / 10\n\n");

        sb.append("📅 Avaliações por dia:\n");
        for (Map.Entry<String, Long> entry : relatorio.avaliacoesPorDia.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        sb.append("\n🚨 Avaliações por urgência:\n");
        for (Map.Entry<Urgencia, Long> entry : relatorio.avaliacoesPorUrgencia.entrySet()) {
            sb.append("  ").append(entry.getKey().name()).append(": ").append(entry.getValue()).append("\n");
        }

        sb.append("\n-- Sistema de Feedback TC4");
        return sb.toString();
    }
}
package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.RelatorioSemanalDTO;
import org.acme.entity.Feedback;
import org.acme.enums.Urgencia;
import org.acme.repository.FeedbackRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class RelatorioService {

    @Inject
    FeedbackRepository feedbackRepository;

    @Inject
    NotificationService notificationService;

    public void gerarEEnviarRelatorioSemanal() {
        LocalDateTime fim = LocalDateTime.now();
        LocalDateTime inicio = fim.minusDays(7);

        RelatorioSemanalDTO relatorio = gerarRelatorio(inicio, fim);
        notificationService.enviarRelatorioSemanal(relatorio);
    }

    public RelatorioSemanalDTO gerarRelatorio(LocalDateTime inicio, LocalDateTime fim) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<Feedback> feedbacks = feedbackRepository.findByPeriodo(inicio, fim);

        RelatorioSemanalDTO relatorio = new RelatorioSemanalDTO();
        relatorio.periodoInicio = inicio.format(formatter);
        relatorio.periodoFim = fim.format(formatter);
        relatorio.totalAvaliacoes = feedbacks.size();
        relatorio.mediaNotas = feedbacks.stream()
                .mapToInt(f -> f.nota)
                .average()
                .orElse(0.0);

        relatorio.avaliacoesPorDia = feedbacks.stream()
                .collect(Collectors.groupingBy(
                        f -> f.dataEnvio.toLocalDate().format(formatter),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        relatorio.avaliacoesPorUrgencia = feedbacks.stream()
                .collect(Collectors.groupingBy(
                        f -> f.urgencia,
                        Collectors.counting()
                ));

        for (Urgencia u : Urgencia.values()) {
            relatorio.avaliacoesPorUrgencia.putIfAbsent(u, 0L);
        }

        return relatorio;
    }
}
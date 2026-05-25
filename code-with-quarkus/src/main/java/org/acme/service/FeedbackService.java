package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.dto.FeedbackRequest;
import org.acme.dto.FeedbackResponse;
import org.acme.entity.Feedback;
import org.acme.enums.Urgencia;
import org.acme.repository.FeedbackRepository;

@ApplicationScoped
public class FeedbackService {

    @Inject
    FeedbackRepository feedbackRepository;

    @Inject
    UrgenciaService urgenciaService;

    @Inject
    NotificationService notificationService;

    @Transactional
    public FeedbackResponse receberFeedback(FeedbackRequest request) {
        Urgencia urgencia = urgenciaService.classificar(request.nota);

        Feedback feedback = new Feedback();
        feedback.descricao = request.descricao;
        feedback.nota = request.nota;
        feedback.urgencia = urgencia;

        feedbackRepository.persist(feedback);

        if (urgenciaService.isCritica(urgencia)) {
            notificationService.enviarNotificacaoCritica(feedback);
        }

        return FeedbackResponse.from(feedback);
    }
}
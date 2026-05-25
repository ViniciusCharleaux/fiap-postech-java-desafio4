package org.acme.dto;

import org.acme.entity.Feedback;
import org.acme.enums.Urgencia;

import java.time.LocalDateTime;

public class FeedbackResponse {

    public Long id;
    public String descricao;
    public Integer nota;
    public Urgencia urgencia;
    public LocalDateTime dataEnvio;

    public static FeedbackResponse from(Feedback feedback) {
        FeedbackResponse response = new FeedbackResponse();
        response.id = feedback.id;
        response.descricao = feedback.descricao;
        response.nota = feedback.nota;
        response.urgencia = feedback.urgencia;
        response.dataEnvio = feedback.dataEnvio;
        return response;
    }
}
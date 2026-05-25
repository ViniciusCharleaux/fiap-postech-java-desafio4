package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.enums.Urgencia;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class UrgenciaService {

    @ConfigProperty(name = "feedback.urgencia.critica.nota-maxima", defaultValue = "4")
    int notaMaximaCritica;

    public Urgencia classificar(int nota) {
        if (nota <= notaMaximaCritica) {
            return Urgencia.CRITICA;
        } else if (nota <= 6) {
            return Urgencia.ALTA;
        } else if (nota <= 8) {
            return Urgencia.MEDIA;
        } else {
            return Urgencia.BAIXA;
        }
    }

    public boolean isCritica(Urgencia urgencia) {
        return Urgencia.CRITICA.equals(urgencia);
    }
}
package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.Feedback;
import org.acme.enums.Urgencia;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class FeedbackRepository implements PanacheRepository<Feedback> {

    public List<Feedback> findByPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return list("dataEnvio >= ?1 and dataEnvio <= ?2", inicio, fim);
    }

    public long countByUrgenciaEPeriodo(Urgencia urgencia, LocalDateTime inicio, LocalDateTime fim) {
        return count("urgencia = ?1 and dataEnvio >= ?2 and dataEnvio <= ?3", urgencia, inicio, fim);
    }

    public Double calcularMediaNotasPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return getEntityManager()
                .createQuery(
                        "SELECT AVG(f.nota) FROM Feedback f WHERE f.dataEnvio >= :inicio AND f.dataEnvio <= :fim",
                        Double.class)
                .setParameter("inicio", inicio)
                .setParameter("fim", fim)
                .getSingleResult();
    }
}
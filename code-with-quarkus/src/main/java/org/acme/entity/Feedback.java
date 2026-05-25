package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.acme.enums.Urgencia;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
public class Feedback extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String descricao;

    @Column(nullable = false)
    public Integer nota;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Urgencia urgencia;

    @Column(name = "data_envio", nullable = false)
    public LocalDateTime dataEnvio;

    @PrePersist
    public void prePersist() {
        this.dataEnvio = LocalDateTime.now();
    }
}
package org.acme.dto;

import org.acme.enums.Urgencia;

import java.util.Map;

public class RelatorioSemanalDTO {

    public String periodoInicio;
    public String periodoFim;
    public long totalAvaliacoes;
    public double mediaNotas;
    public Map<String, Long> avaliacoesPorDia;
    public Map<Urgencia, Long> avaliacoesPorUrgencia;
}
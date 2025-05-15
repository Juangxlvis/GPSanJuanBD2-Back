package org.uniquindio.edu.co.gpsanjuan_backend.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PreguntaExamen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long preguntaExamenId;

    @Column(nullable=false)
    @EqualsAndHashCode.Include
    private Double porcentajeExamen;

    @Column(nullable=false)
    @EqualsAndHashCode.Include
    private Integer tiempoPregunta;

    @Column(nullable=false)
    @EqualsAndHashCode.Include
    private boolean tieneTiempoMaximo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta")
    private Pregunta pregunta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examen_id")
    private Examen examen;
}

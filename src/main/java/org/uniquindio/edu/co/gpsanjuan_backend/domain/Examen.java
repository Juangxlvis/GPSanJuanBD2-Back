package org.uniquindio.edu.co.gpsanjuan_backend.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Examen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer examenId;

    @Column(nullable=false)
    @EqualsAndHashCode.Include
    private Integer tiempoMaximo;

    @Column(nullable=false)
    @EqualsAndHashCode.Include
    private Integer numeroPreguntas;

    @Column(nullable=false)
    @EqualsAndHashCode.Include
    private Float porcentajeCurso;

    @Column(nullable=false)
    @EqualsAndHashCode.Include
    private String nombre;

    @Column(nullable=false)
    @EqualsAndHashCode.Include
    private String descripcion;

    @Column(nullable=false)
    @EqualsAndHashCode.Include
    private Float porcentajeAprobatorio;

    @Column(nullable=false)
    @EqualsAndHashCode.Include
    private LocalDateTime fechaHoraInicio;

    @Column(nullable=false)
    @EqualsAndHashCode.Include
    private LocalDateTime fechaHoraLimite;

    @Column(nullable=false)
    @EqualsAndHashCode.Include
    private Integer numeroPreguntasAleatorias;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id")
    private Docente docente;

    @OneToMany(mappedBy = "examen", fetch = FetchType.LAZY)
    private List<PresentacionExamen> preguntas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tema_id")
    private Tema tema;

    @OneToMany(mappedBy = "examen", fetch = FetchType.LAZY)
    private List<PreguntaExamen> preguntasExamen;


}


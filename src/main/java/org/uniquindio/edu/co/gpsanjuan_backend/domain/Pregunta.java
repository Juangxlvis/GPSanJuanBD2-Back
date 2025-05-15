package org.uniquindio.edu.co.gpsanjuan_backend.domain;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long idPregunta;

    @Column(nullable = false, length = 63)
    @EqualsAndHashCode.Include
    private String enunciado;

    @Column(nullable = false)
    private boolean esPublica;

    @Column(nullable = false, length = 63)
    private String tipoPregunta;

    // Relación para subpreguntas
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregunta_padre_id") // Nombre de la columna que referencia a la pregunta padre
    private Pregunta preguntaPadre;

    @OneToMany(mappedBy = "preguntaPadre", fetch = FetchType.LAZY)
    private List<Pregunta> subPreguntas;

    @OneToMany(mappedBy = "pregunta", fetch = FetchType.LAZY)
    private List<Respuesta> respuestas;

    @OneToMany(mappedBy = "pregunta", fetch = FetchType.LAZY)
    private List<PresentacionPregunta> presentacionesPregunta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id")
    private Docente docente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tema_id")
    private Tema tema;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_respuesta")
    private Respuesta respuesta;

    @OneToMany(mappedBy = "pregunta", fetch = FetchType.LAZY)
    private List<PreguntaExamen> preguntasExamen;
}


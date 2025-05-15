package org.uniquindio.edu.co.gpsanjuan_backend.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Docente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer docenteId;

    @Column(nullable = false, length = 63)
    @EqualsAndHashCode.Include
    private String nombre;

    @Column(nullable = false, length = 63)
    @EqualsAndHashCode.Include
    private String apellido;

    @Column(nullable = false, length = 63)
    @EqualsAndHashCode.Include
    private String identificacion;

    @Column(nullable = false, length = 63)
    @EqualsAndHashCode.Include
    private String correo;

    @Column(nullable = false, length = 63)
    @EqualsAndHashCode.Include
    private String contrasenia;

    @OneToMany(mappedBy = "docente", fetch = FetchType.LAZY)
    private List<Grupo> grupos;

    @OneToMany(mappedBy = "docente", fetch = FetchType.LAZY)
    private List<Examen> examenes;

    @OneToMany(mappedBy = "docente", fetch = FetchType.LAZY)
    private List<Pregunta> preguntas;
}

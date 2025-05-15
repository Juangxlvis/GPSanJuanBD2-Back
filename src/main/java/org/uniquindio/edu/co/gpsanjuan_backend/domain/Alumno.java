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
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer alumnoId;

    @Column(nullable = false, length = 63)
    @EqualsAndHashCode.Include
    private String nombre;

    @Column(nullable = false, length = 63)
    @EqualsAndHashCode.Include
    private String apellido;

    @Column(nullable = false, length = 63)
    @EqualsAndHashCode.Include
    private String correo;

    @Column(nullable = false, length = 63)
    @EqualsAndHashCode.Include
    private String contrasenia;


    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "alumnos")
    private List<Grupo> grupos;

    @OneToMany(mappedBy = "alumno", fetch = FetchType.LAZY)
    private List<Grupo> notas;

    @OneToMany(mappedBy = "alumno", fetch = FetchType.LAZY)
    private List<PresentacionExamen> examenes;
}

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
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer cursoId;

    @EqualsAndHashCode.Include
    @Column(nullable = false, length = 63)
    private String nombre;

    @EqualsAndHashCode.Include
    @Column(nullable = false, length = 63)
    private String descripcion;

    @OneToMany(mappedBy = "curso", fetch = FetchType.LAZY)
    private List<Grupo> grupos;

    @OneToMany(mappedBy = "curso", fetch = FetchType.LAZY)
    private List<Unidad> unidades;


}

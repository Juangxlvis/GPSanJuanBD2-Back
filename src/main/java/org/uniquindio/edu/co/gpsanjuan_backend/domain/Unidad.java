package org.uniquindio.edu.co.gpsanjuan_backend.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Unidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long unidadId;

    @EqualsAndHashCode.Include
    @Column(nullable = false, length = 63)
    private String titulo;

    @EqualsAndHashCode.Include
    @Column(nullable = false, length = 63)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id")
    @EqualsAndHashCode.Include
    private Curso curso;

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Tema> temas = new ArrayList<>();
}


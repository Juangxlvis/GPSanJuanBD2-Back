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
public class Nota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long notaId;

    @EqualsAndHashCode.Include
    @Column(nullable = false, length = 63)
    private Double valor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id")
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY)
    private Grupo grupo;
}


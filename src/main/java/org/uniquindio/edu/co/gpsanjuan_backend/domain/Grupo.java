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
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long grupoId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "horario",
            joinColumns = @JoinColumn(name = "grupo_id"),
            inverseJoinColumns = @JoinColumn(name = "bloque_id"))
    private List<BloqueHorario> horarios;

    @Column(nullable = false, length = 63)
    @EqualsAndHashCode.Include
    private String jornada;

    @Column(nullable = false, length = 63)
    @EqualsAndHashCode.Include
    private String nombre;

    @Column(nullable = false, length = 63)
    @EqualsAndHashCode.Include
    private String periodo;

    @OneToMany(mappedBy = "grupo", fetch = FetchType.LAZY)
    private List<Nota> notas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id")
    private Curso curso;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "alumno_grupo",
            joinColumns = @JoinColumn(name = "grupo_id"),
            inverseJoinColumns = @JoinColumn(name = "alumno_id"))
    private List<Alumno> alumnos;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id")
    private Docente docente;


}


package org.uniquindio.edu.co.gpsanjuan_backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Examen {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "examen_seq_gen")
    @SequenceGenerator(name = "examen_seq_gen", sequenceName = "examen_seq", allocationSize = 1)
    private Integer id_examen;
    private  Integer tiempo_maximo;
    private Integer numero_preguntas;
    private Float porcentaje_curso;
    private String nombre;
    private String descripcion;
    private Integer porcentaje_aprobatorio;
    private String fecha_hora_inicio;
    private String fecha_hora_limite;
    private Integer numero_preguntas_aleatorias;
    private Integer tema_id;
    private Integer docente_id;
    private Integer grupo_id;
}

package org.uniquindio.edu.co.gpsanjuan_backend.controllers;


import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uniquindio.edu.co.gpsanjuan_backend.DTO.*;
import org.uniquindio.edu.co.gpsanjuan_backend.services.interfaces.DocenteService;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/api/docente")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class DocenteController {

    private final DocenteService docenteService;

    @PostMapping("/listarBancoPreguntas")
    public ResponseEntity<MensajeDTO<List<PreguntaBancoDTO>>> listarBancoPreguntas(@RequestBody Integer id_tema) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", docenteService.obtenerBancoPreguntas(id_tema)));
    }


    @PostMapping("/crearRespuesta")
    public ResponseEntity<MensajeDTO<String>> crearRespuesta(@RequestBody String descripcion, Character esVerdadera, Integer id_pregunta) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", docenteService.crearRespuesta(descripcion, esVerdadera, id_pregunta)));
    }

    @PostMapping("/crearExamen")
    public ResponseEntity<MensajeDTO<String>> crearExamen(@RequestBody CrearExamenDTO examenDTO) throws ParseException {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", docenteService.crearExamen(examenDTO)));
    }

    @PostMapping("/crearPregunta")
    public ResponseEntity<MensajeDTO<String>> crearPregunta(@RequestBody String enunciado, Character es_publica, String tipoPregunta,
                                                            Integer id_tema, Integer id_docente) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", docenteService.crearPregunta(enunciado,es_publica,tipoPregunta,id_tema,id_docente)));
    }

    @PostMapping("/calificarExamen")
    public ResponseEntity<MensajeDTO<String>> calificarExamen(@RequestBody   Integer id_presentacion_examen) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", docenteService.calificarExamen(id_presentacion_examen)));
    }


    @PostMapping("/obtenerPreguntasDocente")
    public ResponseEntity<MensajeDTO<List<PreguntaBancoDTO>>> obtenerPreguntasDocente (@RequestBody  Integer id_docente) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", docenteService.obtenerPreguntasDocente(id_docente)));
    }

    @PostMapping("/obtenerExamenesDocente")
    public ResponseEntity<MensajeDTO<List<ExamenDTO>>> obtenerExamenesDocente (@RequestBody  Integer id_docente) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", docenteService.obtenerExamenesDocente(id_docente)));
    }

    @GetMapping("/nombre/{id}/{rol}")
    public ResponseEntity<MensajeDTO<String>> obtenerNombre(@PathVariable String id, @PathVariable String rol) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", docenteService.obtenerNombre(id, rol)));
    }
    @GetMapping("/cursos/{id}/{rol}")
    public ResponseEntity<MensajeDTO<List<CursoDTO>>> obtenerCursos(@PathVariable String id, @PathVariable String rol) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", docenteService.obtenerCursos(id, rol)));
    }

    @GetMapping("/temasCurso/{id_curso}")
    public ResponseEntity<MensajeDTO<List<TemasCursoDTO>>> obtenerTemasCurso(@PathVariable Integer id_curso) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", docenteService.obtenerTemasCurso(id_curso)));
    }

    @GetMapping("/{id_curso}/grupos") // Endpoint para obtener grupos por ID de curso
    public ResponseEntity<MensajeDTO<List<GrupoDTO>>> obtenerGruposPorCurso(@PathVariable("id_curso") Integer idCurso) {
        try {
            List<GrupoDTO> grupos = docenteService.obtenerGruposPorCurso(idCurso);
            if (grupos.isEmpty()) {
                // Puedes devolver un mensaje específico si no se encuentran grupos o simplemente una lista vacía
                return ResponseEntity.ok().body(new MensajeDTO<>(false, "No se encontraron grupos para el curso especificado.", grupos));
            }
            return ResponseEntity.ok().body(new MensajeDTO<>(false, "", grupos));
        } catch (Exception e) {
            // Manejo de errores generales, puedes ser más específico
            return ResponseEntity.internalServerError().body(new MensajeDTO<>(true, "Error al obtener los grupos del curso: " + e.getMessage(), null));
        }
    }

    @GetMapping("/allTemas")
    public ResponseEntity<MensajeDTO<List<TemasCursoDTO>>> obtenerTemasDocente() {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", docenteService.obtenerTemasDocente()));
    }
}

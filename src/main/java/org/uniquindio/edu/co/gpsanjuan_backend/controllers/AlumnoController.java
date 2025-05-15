package org.uniquindio.edu.co.gpsanjuan_backend.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uniquindio.edu.co.gpsanjuan_backend.DTO.*;
import org.uniquindio.edu.co.gpsanjuan_backend.services.interfaces.AlumnoService;

import java.util.List;

@RestController
@RequestMapping("/api/estudiante")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class AlumnoController {
    private final AlumnoService alumnoService;

    @PostMapping("/guardar-pregunta")
    public ResponseEntity<MensajeDTO<String>> guardarPregunta(@RequestBody PreguntaDTO preguntaDTO) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", alumnoService.guardarPregunta(preguntaDTO)));
    }

    // FUNCIONA
    @PostMapping("/obtener-nota")
    public ResponseEntity<MensajeDTO<Float>> obtenerNota(@RequestBody Integer id_presentacion_examen) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", alumnoService.obtenerNotaPresentacionExamen(id_presentacion_examen)));
    }

    @PostMapping("/presentar-examen")
    public ResponseEntity<MensajeDTO<String>> presentarExamen(@RequestBody PresentacionExamenDTO p) {


        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", alumnoService.crearPresentacionExamen(p.tiempo(),
                p.presentado(),p.ipSource(),p.fechaHoraPresentacion(),p.id_examen(),p.id_alumno())));
    }

    @GetMapping("/nombre/{id}/{rol}")
    public ResponseEntity<MensajeDTO<String>> obtenerNombre(@PathVariable String id, @PathVariable String rol) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", alumnoService.obtenerNombre(id, rol)));
    }

    @GetMapping("/cursos/{id}/{rol}")
    public ResponseEntity<MensajeDTO<List<CursoDTO>>> obtenerCursos(@PathVariable String id, @PathVariable String rol) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", alumnoService.obtenerCursos(id, rol)));
    }


    @GetMapping("/examenes-pendientes/{id}/{id_grupo}")
    public ResponseEntity<MensajeDTO<List<ExamenPendienteDTO>>> obtenerExamenesPendientes(@PathVariable String id, @PathVariable Integer id_grupo) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", alumnoService.obtenerExamenesPendiente(id, id_grupo)));
    }

    @GetMapping("/examenes-hechos/{id}/{id_grupo}")
    public ResponseEntity<MensajeDTO<List<ExamenHechoDTO>>> obtenerExamenesHechos(@PathVariable String id, @PathVariable Integer id_grupo) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", alumnoService.obtenerExamenesHechos(id, id_grupo)));
    }
}

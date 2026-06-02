package ufps.edu.co.controllers.cases.estudiante;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import ufps.edu.co.processor.crud.EstudiantesProcessor;
import ufps.edu.co.records.input.entity.EstudiantesInput.ESTUDIANTE_CREATE;
import ufps.edu.co.records.input.entity.EstudiantesInput.ESTUDIANTE_UPDATE;
import ufps.edu.co.records.output.entity.EstudiantesOutput;

@RestController
@RequestMapping("/estudiantes")
public class EstudiantesCase {

    private static final Logger logger = LoggerFactory.getLogger(EstudiantesCase.class);

    @Autowired
    private EstudiantesProcessor estudiantesProcessor;

    @GetMapping
    public ResponseEntity<List<EstudiantesOutput>> findAll() {
        try {
            return ResponseEntity.ok(estudiantesProcessor.findAll());
        } catch (Exception e) {
            logger.error("Error obteniendo todos los estudiantes", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstudiantesOutput> findById(@PathVariable Integer id) {
        try {
            EstudiantesOutput output = estudiantesProcessor.findById(id);
            if (output == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(output);
        } catch (Exception e) {
            logger.error("Error obteniendo estudiante con id {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/programa/{programaId}")
    public ResponseEntity<List<EstudiantesOutput>> findByPrograma(@PathVariable Integer programaId) {
        try {
            return ResponseEntity.ok(estudiantesProcessor.findByProgramaId(programaId));
        } catch (Exception e) {
            logger.error("Error obteniendo estudiantes del programa {}", programaId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/cohorte/{cohorteId}")
    public ResponseEntity<List<EstudiantesOutput>> findByCohorte(@PathVariable Integer cohorteId) {
        try {
            return ResponseEntity.ok(estudiantesProcessor.findByCohorteId(cohorteId));
        } catch (Exception e) {
            logger.error("Error obteniendo estudiantes de la cohorte {}", cohorteId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/programa/{programaId}/cohorte/{cohorteId}")
    public ResponseEntity<List<EstudiantesOutput>> findByProgramaAndCohorte(
            @PathVariable Integer programaId,
            @PathVariable Integer cohorteId) {
        try {
            return ResponseEntity.ok(estudiantesProcessor.findByProgramaIdAndCohorteId(programaId, cohorteId));
        } catch (Exception e) {
            logger.error("Error obteniendo estudiantes del programa {} y cohorte {}", programaId, cohorteId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EstudiantesOutput> create(@Valid @RequestBody ESTUDIANTE_CREATE body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(estudiantesProcessor.create(body));
        } catch (IllegalArgumentException e) {
            logger.warn("Solicitud inválida al crear estudiante: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creando estudiante", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EstudiantesOutput> update(
            @PathVariable Integer id,
            @Valid @RequestBody ESTUDIANTE_UPDATE body) {
        try {
            return ResponseEntity.ok(estudiantesProcessor.update(id, body));
        } catch (IllegalArgumentException e) {
            logger.warn("Solicitud inválida al actualizar estudiante {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error actualizando estudiante {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            estudiantesProcessor.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error eliminando estudiante {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

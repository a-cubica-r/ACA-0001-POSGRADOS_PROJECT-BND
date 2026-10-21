package ufps.edu.co.controllers.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ufps.edu.co.services.core.crud.CalificacioncriterioCoreService;
import ufps.edu.co.records.input.entity.CalificacioncriterioInput.*;
import ufps.edu.co.records.output.entity.CalificacioncriterioOutput;

@RestController
@RequestMapping(value = "/calificacioncriterio", produces = MediaType.APPLICATION_JSON_VALUE)
public class CalificacioncriterioRestController {

    @Autowired
    private CalificacioncriterioCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<Page<CalificacioncriterioOutput>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(processor.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalificacioncriterioOutput> findById(@PathVariable Integer id) {
        CalificacioncriterioOutput output = processor.findById(new CALIFICACIONCRITERIO_FIND(id));
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/aspirante/{idAspirante}")
    public ResponseEntity<List<CalificacioncriterioOutput>> findByIdAspirante(@PathVariable Integer idAspirante) {
        List<CalificacioncriterioOutput> list = processor.findByIdAspirante(idAspirante);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/criterio/{idCriterio}")
    public ResponseEntity<List<CalificacioncriterioOutput>> findByIdCriterio(@PathVariable Integer idCriterio) {
        List<CalificacioncriterioOutput> list = processor.findByIdCriterio(idCriterio);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/create")
    public ResponseEntity<CalificacioncriterioOutput> create(@RequestBody CALIFICACIONCRITERIO_CREATE request) {
        CalificacioncriterioOutput output = processor.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<CalificacioncriterioOutput> update(@RequestBody CALIFICACIONCRITERIO_UPDATE request) {
        try {
            CalificacioncriterioOutput updated = processor.update(request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@RequestBody CALIFICACIONCRITERIO_DELETE request) {
        processor.deleteById(request);
        return ResponseEntity.noContent().build();
    }
}

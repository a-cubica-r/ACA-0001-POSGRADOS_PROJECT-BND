package ufps.edu.co.controllers.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ufps.edu.co.services.core.crud.CriterioevaluacionCoreService;
import ufps.edu.co.records.input.entity.CriterioevaluacionInput.*;
import ufps.edu.co.records.output.entity.CriterioevaluacionOutput;

@RestController
@RequestMapping(value = "/criterioevaluacion", produces = MediaType.APPLICATION_JSON_VALUE)
public class CriterioevaluacionRestController {

    @Autowired
    private CriterioevaluacionCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<List<CriterioevaluacionOutput>> findAll() {
        List<CriterioevaluacionOutput> list = processor.findAll();
        return ResponseEntity.ok(list);
    }

    @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CriterioevaluacionOutput> findById(@RequestBody CRITERIOEVALUACION_FIND request) {
        CriterioevaluacionOutput output = processor.findById(request);
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<CriterioevaluacionOutput> create(@RequestBody CRITERIOEVALUACION_CREATE request) {
        CriterioevaluacionOutput output = processor.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<CriterioevaluacionOutput> update(@RequestBody CRITERIOEVALUACION_UPDATE request) {
        try {
            CriterioevaluacionOutput updated = processor.update(request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@RequestBody CRITERIOEVALUACION_DELETE request) {
        try {
            processor.deleteById(request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

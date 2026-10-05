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
import ufps.edu.co.services.core.crud.CohorteCoreService;
import ufps.edu.co.records.input.entity.CohorteInput.*;
import ufps.edu.co.records.output.entity.CohorteOutput;

@RestController
@RequestMapping(value = "/cohortes", produces = MediaType.APPLICATION_JSON_VALUE)
public class CohorteRestController {

    @Autowired
    private CohorteCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<List<CohorteOutput>> findAll() {
        List<CohorteOutput> list = processor.findAll();
        return ResponseEntity.ok(list);
    }

    @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohorteOutput> findById(@RequestBody COHORTE_FIND request) {
        CohorteOutput output = processor.findById(request);
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<CohorteOutput> create(@RequestBody COHORTE_CREATE request) {
        CohorteOutput output = processor.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<CohorteOutput> update(@RequestBody COHORTE_UPDATE request) {
        try {
            CohorteOutput updated = processor.update(request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@RequestBody COHORTE_DELETE request) {
        try {
            processor.deleteById(request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

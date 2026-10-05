package ufps.edu.co.controllers.rest;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ufps.edu.co.services.core.crud.SedeCoreService;
import ufps.edu.co.records.input.entity.SedeInput.*;
import ufps.edu.co.records.output.entity.SedeOutput;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping(value = "/sedes", produces = MediaType.APPLICATION_JSON_VALUE)
public class SedeController {

    @Autowired
    private SedeCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<List<SedeOutput>> findAll() {
        List<SedeOutput> list = processor.findAll();
        return ResponseEntity.ok(list);
    }

    @PostMapping(value ="/list", consumes   = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SedeOutput> findById(@RequestBody SEDE_FIND request) {
        SedeOutput output = processor.findById(request);
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<SedeOutput> create(@RequestBody SEDE_CREATE request) {
        SedeOutput output = processor.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<SedeOutput> update(@RequestBody SEDE_UPDATE request) {
        try {
            SedeOutput updated = processor.update(request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@RequestBody SEDE_DELETE request) {
        try {
            processor.deleteById(request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}

package ufps.edu.co.controllers.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ufps.edu.co.services.core.crud.GeneroCoreService;
import ufps.edu.co.records.input.entity.GeneroInput.*;
import ufps.edu.co.records.output.entity.GeneroOutput;

@RestController
@RequestMapping(value = "/genero", produces = MediaType.APPLICATION_JSON_VALUE)
public class GeneroRestController {

    @Autowired
    private GeneroCoreService processor;

    @PostMapping(value = "/list", consumes   = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeneroOutput> findById(@RequestBody GENERO_FIND request) {
        GeneroOutput output = processor.findById(request);
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<GeneroOutput> create(@RequestBody GENERO_CREATE request) {
        GeneroOutput output = processor.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<GeneroOutput> update(@RequestBody GENERO_UPDATE request) {
        try {
            GeneroOutput updated = processor.update(request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@RequestBody GENERO_DELETE request) {
        try {
            processor.deleteById(request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
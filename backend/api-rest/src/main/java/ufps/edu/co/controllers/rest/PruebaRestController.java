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

import ufps.edu.co.services.core.crud.PruebaCoreService;
import ufps.edu.co.records.input.entity.PruebaInput.*;
import ufps.edu.co.records.output.entity.PruebaOutput;

@RestController
@RequestMapping(value = "/prueba", produces = MediaType.APPLICATION_JSON_VALUE)
public class PruebaRestController {

    @Autowired
    private PruebaCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<List<PruebaOutput>> findAll() {
        return ResponseEntity.ok(processor.findAll());
    }

    @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PruebaOutput> findById(@RequestBody PRUEBA_FIND request) {
        return ResponseEntity.ok(processor.findById(request));
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PruebaOutput> create(@RequestBody PRUEBA_CREATE request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(processor.create(request));
    }

    @PutMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PruebaOutput> update(@RequestBody PRUEBA_UPDATE request) {
        return ResponseEntity.ok(processor.update(request));
    }

    @DeleteMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteById(@RequestBody PRUEBA_DELETE request) {
        processor.deleteById(request);
        return ResponseEntity.noContent().build();
    }
}

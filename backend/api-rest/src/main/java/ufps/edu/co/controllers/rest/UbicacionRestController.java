package ufps.edu.co.controllers.rest;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ufps.edu.co.services.core.crud.UbicacionCoreService;
import ufps.edu.co.records.input.entity.UbicacionInput.*;
import ufps.edu.co.records.output.entity.UbicacionOutput;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping(value = "/ubicacion", produces = MediaType.APPLICATION_JSON_VALUE)
public class UbicacionRestController {

    @Autowired
    private UbicacionCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<List<UbicacionOutput>> findAll() {
        List<UbicacionOutput> list = processor.findAll();
        return ResponseEntity.ok(list);
    }

    @PostMapping(value = "/list", consumes   = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UbicacionOutput> findById(@RequestBody UBICACION_FIND request) {
        UbicacionOutput output = processor.findById(request);
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<UbicacionOutput> create(@RequestBody UBICACION_CREATE request) {
        UbicacionOutput output = processor.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<UbicacionOutput> update(@RequestBody UBICACION_UPDATE request) {
        try {
            UbicacionOutput updated = processor.update(request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@RequestBody UBICACION_DELETE request) {
        try {
            processor.deleteById(request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

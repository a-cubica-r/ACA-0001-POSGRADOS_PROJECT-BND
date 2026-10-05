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

import ufps.edu.co.services.core.crud.ModalidadCoreService;
import ufps.edu.co.records.input.entity.ModalidadInput.*;
import ufps.edu.co.records.output.entity.ModalidadOutput;

@RestController
@RequestMapping(value = "/modalidad", produces = MediaType.APPLICATION_JSON_VALUE)
public class ModalidadRestController {

    @Autowired
    private ModalidadCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<List<ModalidadOutput>> findAll() {
        List<ModalidadOutput> list = processor.findAll();
        return ResponseEntity.ok(list);
    }

    @PostMapping(value = "/list", consumes   = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ModalidadOutput> findById(@RequestBody MODALIDAD_FIND request) {
        ModalidadOutput output = processor.findById(request);
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ModalidadOutput> create(@RequestBody MODALIDAD_CREATE request) {
        ModalidadOutput output = processor.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<ModalidadOutput> update(@RequestBody MODALIDAD_UPDATE request) {
        try {
            ModalidadOutput updated = processor.update(request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@RequestBody MODALIDAD_DELETE request) {
        try {
            processor.deleteById(request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

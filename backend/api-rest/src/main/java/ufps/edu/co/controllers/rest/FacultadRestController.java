package ufps.edu.co.controllers.rest;

import java.util.List;

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

import ufps.edu.co.services.core.crud.FacultadCoreService;
import ufps.edu.co.records.input.entity.FacultadInput.*;
import ufps.edu.co.records.output.entity.FacultadOutput;

@RestController
@RequestMapping(value = "/facultad", produces = MediaType.APPLICATION_JSON_VALUE)
public class FacultadRestController {

    @Autowired
    private FacultadCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<List<FacultadOutput>> findAll() {
        List<FacultadOutput> list = processor.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacultadOutput> findById(@PathVariable Integer id) {
        FacultadOutput output = processor.findById(new FACULTAD_FIND(id));
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<FacultadOutput> create(@RequestBody FACULTAD_CREATE request) {
        FacultadOutput output = processor.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<FacultadOutput> update(@RequestBody FACULTAD_UPDATE request) {
        FacultadOutput updated = processor.update(request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@RequestBody FACULTAD_DELETE request) {
        processor.deleteById(request);
        return ResponseEntity.noContent().build();
    }
}

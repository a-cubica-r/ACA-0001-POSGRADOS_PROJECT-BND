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
import ufps.edu.co.services.core.crud.SemestreCoreService;
import ufps.edu.co.records.input.entity.SemestreInput.*;
import ufps.edu.co.records.output.entity.SemestreOutput;

@RestController
@RequestMapping(value = "/semestre", produces = MediaType.APPLICATION_JSON_VALUE)
public class SemestreRestController {

    @Autowired
    private SemestreCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<List<SemestreOutput>> findAll() {
        List<SemestreOutput> list = processor.findAll();
        return ResponseEntity.ok(list);
    }

    @PostMapping(value = "/list", consumes   = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SemestreOutput> findById(@RequestBody SEMESTRE_FIND request) {
        SemestreOutput output = processor.findById(request);
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<SemestreOutput> create(@RequestBody SEMESTRE_CREATE request) {
        SemestreOutput output = processor.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<SemestreOutput> update(@RequestBody SEMESTRE_UPDATE request) {
        try {
            SemestreOutput updated = processor.update(request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@RequestBody SEMESTRE_DELETE request) {
        try {
            processor.deleteById(request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
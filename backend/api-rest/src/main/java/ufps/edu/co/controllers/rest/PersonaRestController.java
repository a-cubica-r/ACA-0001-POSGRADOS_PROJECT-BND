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
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ufps.edu.co.services.core.crud.PersonaCoreService;
import ufps.edu.co.records.input.entity.PersonaInput.*;
import ufps.edu.co.records.output.entity.PersonaOutput;

@RestController
@RequestMapping(value = "/persona", produces = MediaType.APPLICATION_JSON_VALUE)
public class PersonaRestController {

    @Autowired
    private PersonaCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<List<PersonaOutput>> findAll() {
        List<PersonaOutput> list = processor.findAll();
        return ResponseEntity.ok(list);
    }

    @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PersonaOutput> findById(@Valid @RequestBody PERSONA_FIND request) {
        PersonaOutput output = processor.findById(request);
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<PersonaOutput> create(@Valid @RequestBody PERSONA_CREATE request) {
        PersonaOutput output = processor.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<PersonaOutput> update(@Valid @RequestBody PERSONA_UPDATE request) {
        try {
            PersonaOutput updated = processor.update(request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@Valid @RequestBody PERSONA_DELETE request) {
        try {
            processor.deleteById(request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

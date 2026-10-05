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

import ufps.edu.co.services.core.crud.DocumentosrequisitoprogramaCoreService;
import ufps.edu.co.records.input.entity.DocumentosrequisitoprogramaInput.*;
import ufps.edu.co.records.output.entity.DocumentosrequisitoprogramaOutput;

@RestController
@RequestMapping(value = "/documentosrequisitoprograma", produces = MediaType.APPLICATION_JSON_VALUE)
public class DocumentosrequisitoprogramaRestController {

    @Autowired
    private DocumentosrequisitoprogramaCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<List<DocumentosrequisitoprogramaOutput>> findAll() {
        return ResponseEntity.ok(processor.findAll());
    }

    @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentosrequisitoprogramaOutput> findById(@RequestBody DOCUMENTOSREQUISITOPROGRAMA_FIND request) {
        DocumentosrequisitoprogramaOutput output = processor.findById(request);
        return output != null ? ResponseEntity.ok(output) : ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentosrequisitoprogramaOutput> create(@RequestBody DOCUMENTOSREQUISITOPROGRAMA_CREATE request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(processor.create(request));
    }

    @PutMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentosrequisitoprogramaOutput> update(@RequestBody DOCUMENTOSREQUISITOPROGRAMA_UPDATE request) {
        try {
            return ResponseEntity.ok(processor.update(request));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteById(@RequestBody DOCUMENTOSREQUISITOPROGRAMA_DELETE request) {
        try {
            processor.deleteById(request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

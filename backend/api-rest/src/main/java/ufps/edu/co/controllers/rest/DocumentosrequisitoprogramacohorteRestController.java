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

import ufps.edu.co.services.core.crud.DocumentosrequisitoprogramacohorteCoreService;
import ufps.edu.co.records.input.entity.DocumentosrequisitoprogramacohorteInput.*;
import ufps.edu.co.records.output.entity.DocumentosrequisitoprogramacohorteOutput;

@RestController
@RequestMapping(value = "/documentosrequisitoprogramacohorte", produces = MediaType.APPLICATION_JSON_VALUE)
public class DocumentosrequisitoprogramacohorteRestController {

    @Autowired
    private DocumentosrequisitoprogramacohorteCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<List<DocumentosrequisitoprogramacohorteOutput>> findAll() {
        return ResponseEntity.ok(processor.findAll());
    }

    @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentosrequisitoprogramacohorteOutput> findById(@RequestBody DOCUMENTOSREQUISITOPROGRAMACOHORTE_FIND request) {
        DocumentosrequisitoprogramacohorteOutput output = processor.findById(request);
        return output != null ? ResponseEntity.ok(output) : ResponseEntity.notFound().build();
    }

    @GetMapping("/cohorte/{idCohorte}")
    public ResponseEntity<List<DocumentosrequisitoprogramacohorteOutput>> findByIdCohorte(@PathVariable Integer idCohorte) {
        return ResponseEntity.ok(processor.findByIdCohorte(idCohorte));
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentosrequisitoprogramacohorteOutput> create(@RequestBody DOCUMENTOSREQUISITOPROGRAMACOHORTE_CREATE request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(processor.create(request));
    }

    @PutMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentosrequisitoprogramacohorteOutput> update(@RequestBody DOCUMENTOSREQUISITOPROGRAMACOHORTE_UPDATE request) {
        try {
            return ResponseEntity.ok(processor.update(request));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteById(@RequestBody DOCUMENTOSREQUISITOPROGRAMACOHORTE_DELETE request) {
        try {
            processor.deleteById(request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

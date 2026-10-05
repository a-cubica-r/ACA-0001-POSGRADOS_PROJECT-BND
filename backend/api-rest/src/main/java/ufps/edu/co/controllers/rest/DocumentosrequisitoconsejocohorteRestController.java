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

import ufps.edu.co.services.core.crud.DocumentosrequisitoconsejocohorteCoreService;
import ufps.edu.co.records.input.entity.DocumentosrequisitoconsejocohorteInput.*;
import ufps.edu.co.records.output.entity.DocumentosrequisitoconsejocohorteOutput;

@RestController
@RequestMapping(value = "/documentosrequisitoconsejocohorte", produces = MediaType.APPLICATION_JSON_VALUE)
public class DocumentosrequisitoconsejocohorteRestController {

    @Autowired
    private DocumentosrequisitoconsejocohorteCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<List<DocumentosrequisitoconsejocohorteOutput>> findAll() {
        return ResponseEntity.ok(processor.findAll());
    }

    @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentosrequisitoconsejocohorteOutput> findById(@RequestBody DOCUMENTOSREQUISITOCONSEJOCOHORTE_FIND request) {
        DocumentosrequisitoconsejocohorteOutput output = processor.findById(request);
        return output != null ? ResponseEntity.ok(output) : ResponseEntity.notFound().build();
    }

    @GetMapping("/cohorte/{idCohorte}")
    public ResponseEntity<List<DocumentosrequisitoconsejocohorteOutput>> findByIdCohorte(@PathVariable Integer idCohorte) {
        return ResponseEntity.ok(processor.findByIdCohorte(idCohorte));
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentosrequisitoconsejocohorteOutput> create(@RequestBody DOCUMENTOSREQUISITOCONSEJOCOHORTE_CREATE request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(processor.create(request));
    }

    @PutMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentosrequisitoconsejocohorteOutput> update(@RequestBody DOCUMENTOSREQUISITOCONSEJOCOHORTE_UPDATE request) {
        try {
            return ResponseEntity.ok(processor.update(request));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteById(@RequestBody DOCUMENTOSREQUISITOCONSEJOCOHORTE_DELETE request) {
        try {
            processor.deleteById(request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

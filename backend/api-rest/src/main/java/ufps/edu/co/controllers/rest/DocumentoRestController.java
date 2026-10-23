package ufps.edu.co.controllers.rest;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ufps.edu.co.services.core.crud.DocumentoCoreService;
import ufps.edu.co.records.input.entity.DocumentoInput.*;
import ufps.edu.co.records.output.entity.DocumentoOutput;

@RestController
@RequestMapping(value = "/documento", produces = MediaType.APPLICATION_JSON_VALUE)
public class DocumentoRestController {

    @Autowired
    private DocumentoCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<Page<DocumentoOutput>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(processor.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentoOutput> findById(@PathVariable Integer id) {
        DocumentoOutput output = processor.findById(new DOCUMENTO_FIND(id));
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<DocumentoOutput> create(@RequestBody DOCUMENTO_CREATE request) {
        DocumentoOutput output = processor.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<DocumentoOutput> update(@RequestBody DOCUMENTO_UPDATE request) {
        try {
            DocumentoOutput updated = processor.update(request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@RequestBody DOCUMENTO_DELETE request) {
        processor.deleteById(request);
        return ResponseEntity.noContent().build();
    }
}

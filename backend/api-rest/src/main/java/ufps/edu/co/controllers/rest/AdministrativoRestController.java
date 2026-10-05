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

import ufps.edu.co.services.core.crud.AdministrativoCoreService;
import ufps.edu.co.records.input.entity.AdministrativoInput.*;
import ufps.edu.co.records.output.entity.AdministrativoOutput;

@RestController
@RequestMapping(value = "/administrativo", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdministrativoRestController {

    @Autowired
    private AdministrativoCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<List<AdministrativoOutput>> findAll() {
        List<AdministrativoOutput> list = processor.findAll();
        return ResponseEntity.ok(list);
    }

    @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdministrativoOutput> findById(@Valid @RequestBody ADMINISTRATIVO_FIND request) {
        AdministrativoOutput output = processor.findById(request);
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<AdministrativoOutput> create(@Valid @RequestBody ADMINISTRATIVO_CREATE request) {
        AdministrativoOutput output = processor.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<AdministrativoOutput> update(@Valid @RequestBody ADMINISTRATIVO_UPDATE request) {
        try {
            AdministrativoOutput updated = processor.update(request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@Valid @RequestBody ADMINISTRATIVO_DELETE request) {
        try {
            processor.delete(request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}

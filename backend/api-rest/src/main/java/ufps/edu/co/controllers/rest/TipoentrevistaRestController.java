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

import ufps.edu.co.services.core.crud.TipoentrevistaCoreService;
import ufps.edu.co.records.input.entity.TipoentrevistaInput.*;
import ufps.edu.co.records.output.entity.TipoentrevistaOutput;

@RestController
@RequestMapping(value = "/tipoentrevista", produces = MediaType.APPLICATION_JSON_VALUE)
public class TipoentrevistaRestController {

    @Autowired
    private TipoentrevistaCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<List<TipoentrevistaOutput>> findAll() {
        List<TipoentrevistaOutput> list = processor.findAll();
        return ResponseEntity.ok(list);
    }

    @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TipoentrevistaOutput> findById(@RequestBody TIPOENTREVISTA_FIND request) {
        TipoentrevistaOutput output = processor.findById(request);
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<TipoentrevistaOutput> create(@RequestBody TIPOENTREVISTA_CREATE request) {
        TipoentrevistaOutput output = processor.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<TipoentrevistaOutput> update(@RequestBody TIPOENTREVISTA_UPDATE request) {
        try {
            TipoentrevistaOutput updated = processor.update(request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@RequestBody TIPOENTREVISTA_DELETE request) {
        try {
            processor.deleteById(request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

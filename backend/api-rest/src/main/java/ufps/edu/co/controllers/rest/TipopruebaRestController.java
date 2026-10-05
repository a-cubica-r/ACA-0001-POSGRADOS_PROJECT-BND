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

import ufps.edu.co.services.core.crud.TipopruebaCoreService;
import ufps.edu.co.records.input.entity.TipopruebaInput.*;
import ufps.edu.co.records.output.entity.TipopruebaOutput;

@RestController
@RequestMapping(value = "/tipoprueba", produces = MediaType.APPLICATION_JSON_VALUE)
public class TipopruebaRestController {

    @Autowired
    private TipopruebaCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<List<TipopruebaOutput>> findAll() {
        List<TipopruebaOutput> list = processor.findAll();
        return ResponseEntity.ok(list);
    }

    @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TipopruebaOutput> findById(@RequestBody TIPOPRUEBA_FIND request) {
        TipopruebaOutput output = processor.findById(request);
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<TipopruebaOutput> create(@RequestBody TIPOPRUEBA_CREATE request) {
        TipopruebaOutput output = processor.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<TipopruebaOutput> update(@RequestBody TIPOPRUEBA_UPDATE request) {
        try {
            TipopruebaOutput updated = processor.update(request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@RequestBody TIPOPRUEBA_DELETE request) {
        try {
            processor.deleteById(request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

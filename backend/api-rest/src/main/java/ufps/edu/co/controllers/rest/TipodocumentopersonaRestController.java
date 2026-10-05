package ufps.edu.co.controllers.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ufps.edu.co.services.core.crud.TipodocumentopersonaCoreService;
import ufps.edu.co.records.input.entity.TipodocumentopersonaInput.*;
import ufps.edu.co.records.output.entity.TipodocumentopersonaOutput;

@RestController
@RequestMapping(value = "/tipodocumentopersona", produces = MediaType.APPLICATION_JSON_VALUE)
public class TipodocumentopersonaRestController {

    @Autowired
    private TipodocumentopersonaCoreService processor;

    @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TipodocumentopersonaOutput> findById(@RequestBody TIPODOCUMENTOPERSONA_FIND request) {
        TipodocumentopersonaOutput output = processor.findById(request);
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<TipodocumentopersonaOutput> create(@RequestBody TIPODOCUMENTOPERSONA_CREATE request) {
        TipodocumentopersonaOutput output = processor.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<TipodocumentopersonaOutput> update(@RequestBody TIPODOCUMENTOPERSONA_UPDATE request) {
        try {
            TipodocumentopersonaOutput updated = processor.update(request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@RequestBody TIPODOCUMENTOPERSONA_DELETE request) {
        try {
            processor.deleteById(request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

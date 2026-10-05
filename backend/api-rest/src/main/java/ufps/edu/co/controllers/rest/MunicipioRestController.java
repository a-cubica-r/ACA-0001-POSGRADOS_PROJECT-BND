package ufps.edu.co.controllers.rest;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ufps.edu.co.services.core.crud.MunicipioCoreService;
import ufps.edu.co.records.input.entity.MunicipioInput.*;
import ufps.edu.co.records.output.entity.MunicipioOutput;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping(value = "/municipio", produces = MediaType.APPLICATION_JSON_VALUE)
public class MunicipioRestController {

    @Autowired
    private MunicipioCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<List<MunicipioOutput>> findAll() {
        List<MunicipioOutput> list = processor.findAll();
        return ResponseEntity.ok(list);
    }

    @PostMapping(value = "/list", consumes   = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MunicipioOutput> findById(@RequestBody MUNICIPIO_FIND request) {
        MunicipioOutput output = processor.findById(request);
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<MunicipioOutput> create(@RequestBody MUNICIPIO_CREATE request) {
        MunicipioOutput output = processor.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<MunicipioOutput> update(@RequestBody MUNICIPIO_UPDATE request) {
        try {
            MunicipioOutput updated = processor.update(request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@RequestBody MUNICIPIO_DELETE request) {
        try {
            processor.deleteById(request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

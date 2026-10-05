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

import ufps.edu.co.services.core.crud.PlazoCoreService;
import ufps.edu.co.records.input.entity.PlazoInput.*;
import ufps.edu.co.records.output.entity.PlazoOutput;

@RestController
@RequestMapping(value = "/plazo", produces = MediaType.APPLICATION_JSON_VALUE)
public class PlazoRestController {

    @Autowired
    private PlazoCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<List<PlazoOutput>> findAll() {
        List<PlazoOutput> list = processor.findAll();
        return ResponseEntity.ok(list);
    }

    @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PlazoOutput> findById(@RequestBody PLAZO_FIND request) {
        PlazoOutput output = processor.findById(request);
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<PlazoOutput> create(@RequestBody PLAZO_CREATE request) {
        PlazoOutput output = processor.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<PlazoOutput> update(@RequestBody PLAZO_UPDATE request) {
        try {
            PlazoOutput updated = processor.update(request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@RequestBody PLAZO_DELETE request) {
        try {
            processor.deleteById(request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

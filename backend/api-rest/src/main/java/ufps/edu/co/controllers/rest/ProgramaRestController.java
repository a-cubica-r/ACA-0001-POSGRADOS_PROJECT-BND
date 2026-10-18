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

import ufps.edu.co.services.core.crud.ProgramaCoreService;
import ufps.edu.co.records.input.entity.ProgramaInput.*;
import ufps.edu.co.records.output.entity.ProgramaOutput;

@RestController
@RequestMapping(value = "/programa", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProgramaRestController {

    @Autowired
    private ProgramaCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<List<ProgramaOutput>> findAll() {
        List<ProgramaOutput> list = processor.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgramaOutput> findById(@PathVariable Integer id) {
        ProgramaOutput output = processor.findById(new PROGRAMA_FIND(id));
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ProgramaOutput> create(@RequestBody PROGRAMA_CREATE request) {
        ProgramaOutput output = processor.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<ProgramaOutput> update(@RequestBody PROGRAMA_UPDATE request) {
        ProgramaOutput updated = processor.update(request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@RequestBody PROGRAMA_DELETE request) {
        processor.deleteById(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/byfacultad/{id}")
    public ResponseEntity<List<ProgramaOutput>> findByIdFacultad(@PathVariable Integer id) {
        List<ProgramaOutput> list = processor.findByIdFacultad(id);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/count-en-proceso/{id}")
    public ResponseEntity<Long> countAspirantesEnProcesoEnCohorteAbierta(@PathVariable Integer id) {
        Long total = processor.countAspirantesEnProcesoEnCohorteAbierta(id);
        return ResponseEntity.ok(total);
    }
}

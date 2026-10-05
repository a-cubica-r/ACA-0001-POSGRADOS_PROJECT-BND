package ufps.edu.co.controllers.cases.command;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ufps.edu.co.services.core.crud.CohorteCoreService;
import ufps.edu.co.records.input.entity.CohorteInput.*;
import ufps.edu.co.records.output.entity.CohorteOutput;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping(value = "/cohorte", produces = MediaType.APPLICATION_JSON_VALUE)
public class CohorteCommand {

    @Autowired
    private CohorteCoreService processor;

    @PostMapping("/createWithPlazo")
    public ResponseEntity<CohorteOutput> createWithPlazo(
            @RequestBody COHORTE_WITHPLAZO_CREATE request) {
        CohorteOutput output = processor.createWithPlazo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

}

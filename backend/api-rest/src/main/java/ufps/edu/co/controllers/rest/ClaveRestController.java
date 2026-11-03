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

import ufps.edu.co.auth.contract.PasswordHashService;
import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.ClaveErrorCode;
import ufps.edu.co.services.core.crud.ClaveCoreService;
import ufps.edu.co.records.input.entity.ClaveInput.*;
import ufps.edu.co.records.output.entity.ClaveOutput;

@RestController
@RequestMapping(value = "/clave", produces = MediaType.APPLICATION_JSON_VALUE)
public class ClaveRestController {

    @Autowired
    private ClaveCoreService processor;

    @Autowired
    private PasswordHashService passwordHashService;

    @GetMapping("/listall")
    public ResponseEntity<List<ClaveOutput>> findAll() {
        List<ClaveOutput> list = processor.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClaveOutput> findById(@PathVariable Integer id) {
        ClaveOutput output = processor.findById(new CLAVE_FIND(id));
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ClaveOutput> create(@RequestBody CLAVE_CREATE request) {
        ClaveOutput output = processor.create(new CLAVE_CREATE(cifrar(request.valor())));
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<ClaveOutput> update(@RequestBody CLAVE_UPDATE request) {
        CLAVE_UPDATE cifrada = new CLAVE_UPDATE(request.id(), cifrar(request.valor()));
        try {
            ClaveOutput updated = processor.update(cifrada);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@RequestBody CLAVE_DELETE request) {
        processor.deleteById(request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Toda clave recibida por HTTP se cifra aquí. Un valor ya hasheado por el
     * cliente no se acepta como tal: se vuelve a cifrar.
     */
    private String cifrar(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new DomainException(ClaveErrorCode.CLAVE_VALOR_OBLIGATORIO, "valor");
        }
        return passwordHashService.encode(valor);
    }
}

package ufps.edu.co.controllers.cases.integracion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ufps.edu.co.records.output.cases.SituacionAcademicaOutput;
import ufps.edu.co.services.core.integracion.IntegracionTramitesCoreService;

/**
 * SC-03 - Consulta que hace el sistema de Trámites con su cuenta técnica
 * (rol SERVICIO_TRAMITES). Errores: 400 CEDULA_INVALIDA y 404
 * ESTUDIANTE_NOT_FOUND, con el cuerpo {code, message, param} de
 * GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/integracion/v1/estudiantes")
public class IntegracionTramitesCase {

    @Autowired
    private IntegracionTramitesCoreService integracionTramitesService;

    @GetMapping("/{cedula}")
    public ResponseEntity<SituacionAcademicaOutput> consultar(@PathVariable String cedula) {
        return ResponseEntity.ok(integracionTramitesService.consultar(cedula));
    }
}

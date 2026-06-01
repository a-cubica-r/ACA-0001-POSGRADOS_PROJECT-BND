package ufps.edu.co.controllers.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ufps.edu.co.rest.dto.AdministrativoDTO;
import ufps.edu.co.rest.services.AdministrativoService;

@RestController
@RequestMapping(value = "/superadmin/administrativo", produces = MediaType.APPLICATION_JSON_VALUE)
public class SuperAdminAdministrativoAsignarProgramaRestController {

    @Autowired
    private AdministrativoService administrativoService;

    @PatchMapping(value = "/asignar-programa", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> asignarPrograma(@RequestBody AsignarProgramaRequest request) {
        AdministrativoDTO dto = administrativoService.findById(request.id());
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }

        dto.setIdCargo(request.id_cargo());
        administrativoService.update(request.id(), dto);

        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/crear-director", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdministrativoDTO> crearDirector(@RequestBody CrearDirectorRequest request) {
        AdministrativoDTO created = administrativoService.createDirectorPrograma(request.id_persona(), request.id_cargo());
        return ResponseEntity.status(HttpStatus.OK).body(created);
    }

    public static record AsignarProgramaRequest(Integer id, Integer id_cargo) {
    }

    public static record CrearDirectorRequest(Integer id_persona, Integer id_cargo) {
    }
}

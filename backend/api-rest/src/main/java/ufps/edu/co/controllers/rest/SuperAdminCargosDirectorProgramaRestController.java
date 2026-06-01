package ufps.edu.co.controllers.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ufps.edu.co.rest.dto.CargoDTO;
import ufps.edu.co.rest.services.CargoService;

@RestController
@RequestMapping(value = "/superadmin/cargos-director-programa", produces = MediaType.APPLICATION_JSON_VALUE)
public class SuperAdminCargosDirectorProgramaRestController {

    @Autowired
    private CargoService cargoService;

    @GetMapping("/listall")
    public ResponseEntity<List<CargoSimpleResponse>> listAllDirectorProgramaCargos() {
        List<CargoDTO> cargos = cargoService.findAll();

        List<CargoSimpleResponse> response = cargos.stream()
                .filter(c -> c.getIdPrograma() != null)
                .map(c -> new CargoSimpleResponse(
                        c.getId(),
                        c.getNombre(),
                        c.getDescripcion(),
                        c.getIdFacultad(),
                        c.getIdPrograma()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    public static record CargoSimpleResponse(
            Integer id,
            String nombre,
            String descripcion,
            Integer idFacultad,
            Integer idPrograma) {
    }
}

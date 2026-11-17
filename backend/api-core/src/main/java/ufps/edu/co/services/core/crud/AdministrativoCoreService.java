package ufps.edu.co.services.core.crud;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ufps.edu.co.domain.utilities.PrinterObjects;
import ufps.edu.co.maps.specific.AdministrativoMap;
import ufps.edu.co.usecase.CrudUseCase;
import ufps.edu.co.records.input.entity.AdministrativoInput.*;
import ufps.edu.co.records.input.entity.ProgramaInput.PROGRAMA_CREATE_WITH_RELATIONS;
import ufps.edu.co.records.input.entity.ProgramaInput.PROGRAMA_UPDATE_WITH_RELATIONS;
import ufps.edu.co.records.output.entity.AdministrativoOutput;
import ufps.edu.co.records.output.entity.CohorteInscritosOutput;
import ufps.edu.co.records.output.entity.CohorteOutput;
import ufps.edu.co.records.output.entity.ProgramaOutput;
import ufps.edu.co.rest.dto.AdministrativoDTO;
import ufps.edu.co.rest.dto.UsuarioDTO;
import ufps.edu.co.rest.services.AdministrativoService;
import ufps.edu.co.rest.services.UsuarioService;

@Service
public class AdministrativoCoreService implements
        CrudUseCase<ADMINISTRATIVO_CREATE, ADMINISTRATIVO_UPDATE, ADMINISTRATIVO_DELETE, ADMINISTRATIVO_PATCH, ADMINISTRATIVO_FIND, AdministrativoOutput> {

    @Autowired
    private AdministrativoService service;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AdministrativoMap map;

    @Autowired
    private ProgramaCoreService programaProcessor;

    @Autowired
    private CohorteCoreService cohorteProcessor;

    @Override
    public AdministrativoOutput create(ADMINISTRATIVO_CREATE input) {
        AdministrativoDTO dto = map.toDto(input);
        AdministrativoDTO created = service.create(dto);
        return map.toOutput(created);

    }

    @Override
    public AdministrativoOutput update(ADMINISTRATIVO_UPDATE input) {
        AdministrativoDTO dto = map.toDto(input);
        AdministrativoDTO updated = service.update(input.id(), dto);
        return map.toOutput(updated);

    }

    @Override
    public AdministrativoOutput patch(ADMINISTRATIVO_PATCH input) {
        throw new UnsupportedOperationException("Patch not supported for Administrativo");
    }

    @Override
    @Transactional(readOnly = true)
    public AdministrativoOutput findById(ADMINISTRATIVO_FIND input) {
        return map.toOutput(service.findById(input.id()));

    }

    @Override
    public List<AdministrativoOutput> findAll() {
        return service.findAll().stream().map(map::toOutput).toList();

    }

    @Override
    public void delete(ADMINISTRATIVO_DELETE input) {
        service.deleteById(input.id());

    }

    // #region PERSONALIZADOS

    public List<AdministrativoOutput> findPosiblesDirectores() {
        return service.findPosiblesDirectores().stream().map(map::toOutput).toList();

    }

    public List<ProgramaOutput> findProgramasFacultad(ADMINISTRATIVO_FIND input) {
        if (input != null && input.id() != null) {
            AdministrativoDTO admin = service.findById(input.id());
            PrinterObjects.printNotorious(admin);
            if (admin != null && admin.getCargo() != null && admin.getCargo().getIdFacultad() != null) {
                return programaProcessor.findByIdFacultad(admin.getCargo().getIdFacultad());
            }
            throw new RuntimeException("Administrativo no tiene cargo asignado o cargo no tiene facultad asociada");
        }
        throw new RuntimeException("Input inválido: id de administrativo es requerido");

    }

    public List<CohorteOutput> findCohortesActivasFacultad(ADMINISTRATIVO_FIND input) {
        List<ProgramaOutput> programas = findProgramasFacultad(input);
        return programas.stream()
                .flatMap(programa -> cohorteProcessor.findActivasByIdPrograma(programa.id()).stream())
                .toList();

    }

    public List<CohorteInscritosOutput> findCohortesActivasFacultadConInscritos(ADMINISTRATIVO_FIND input) {
        List<CohorteOutput> cohortes = findCohortesActivasFacultad(input);
        return cohortes.stream()
                .map(cohorte -> CohorteInscritosOutput.builder()
                        .cohorte(cohorte)
                        .inscritosEnProceso(
                                cohorteProcessor.countAspirantesEnProcesoByCohorteId(cohorte.id()))
                        .build())
                .toList();

    }

    public ProgramaOutput createProgramaFacultad(ADMINISTRATIVO_FIND input, PROGRAMA_CREATE_WITH_RELATIONS request) {
        Integer idFacultad = resolveFacultadId(input);
        return programaProcessor.createWithRelations(request, idFacultad);

    }

    public ProgramaOutput updateProgramaFacultad(ADMINISTRATIVO_FIND input, PROGRAMA_UPDATE_WITH_RELATIONS request) {
        Integer idFacultad = resolveFacultadId(input);
        return programaProcessor.updateWithRelations(request, idFacultad);

    }

    public Integer findProgramaDirectorByUsuarioId(Integer idUsuario) {
        if (idUsuario == null) {
            throw new RuntimeException("Id de usuario es requerido");
        }

        UsuarioDTO usuario = usuarioService.findById(idUsuario);
        if (usuario == null || usuario.getIdPersona() == null) {
            throw new RuntimeException("No existe un usuario asociado al id enviado");
        }

        Integer adminId = service.findIdByIdPersona(usuario.getIdPersona());
        if (adminId == null) {
            throw new RuntimeException("El usuario no pertenece a un administrativo");
        }
        Integer idPrograma = service.findIdProgramaByIdPersona(usuario.getIdPersona());
        if (idPrograma == null) {
            throw new RuntimeException("El administrativo no tiene cargo de programa asignado");
        }
        String cargoNombre = service.findCargoNombreByIdPersona(usuario.getIdPersona());
        if (cargoNombre == null || !"DIRECTOR DE PROGRAMA".equalsIgnoreCase(cargoNombre.trim())) {
            throw new RuntimeException("El usuario no pertenece a un director de programa");
        }

        return idPrograma;

    }

    private Integer resolveFacultadId(ADMINISTRATIVO_FIND input) {
        if (input == null || input.id() == null) {
            throw new RuntimeException("Input invalido: id de administrativo es requerido");
        }
        AdministrativoDTO admin = service.findById(input.id());
        if (admin == null || admin.getCargo() == null || admin.getCargo().getIdFacultad() == null) {
            throw new RuntimeException("Administrativo no tiene facultad asociada");
        }
        return admin.getCargo().getIdFacultad();
    }

    // #endregion

}

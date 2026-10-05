package ufps.edu.co.services.core.crud;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.CriterioevaluacionErrorCode;
import ufps.edu.co.domain.exceptions.errorcodes.CriteriocohorteErrorCode;
import ufps.edu.co.maps.specific.CriteriocohorteMap;
import ufps.edu.co.records.input.entity.CriteriocohorteInput.*;
import ufps.edu.co.records.output.entity.CriteriocohorteOutput;
import ufps.edu.co.rest.dto.AspiranteDTO;
import ufps.edu.co.rest.dto.CohorteDTO;
import ufps.edu.co.rest.dto.CriteriocohorteDTO;
import ufps.edu.co.rest.dto.CriterioevaluacionDTO;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.CalificacioncriterioService;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.CriteriocohorteService;
import ufps.edu.co.rest.services.CriterioevaluacionService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class CriteriocohorteCoreService implements
        GlobalUseCase<CRITERIOCOHORTE_CREATE, CRITERIOCOHORTE_UPDATE, CRITERIOCOHORTE_DELETE, CRITERIOCOHORTE_PATCH, CRITERIOCOHORTE_FIND, CriteriocohorteOutput> {

    @Autowired
    private CriteriocohorteService service;

    @Autowired
    private CriteriocohorteMap map;

    @Autowired
    private CriterioevaluacionService criterioevaluacionService;

    @Autowired
    private CohorteService cohorteService;

    @Autowired
    private AspiranteService aspiranteService;

    @Autowired
    private CalificacioncriterioCoreService calificacioncriterioProcessor;

    @Autowired
    private CalificacioncriterioService calificacioncriterioService;

    @Override
    public CriteriocohorteOutput create(CRITERIOCOHORTE_CREATE input) {
        try {
            CriteriocohorteDTO dto = map.toDto(input);
            return map.toOutput(service.create(dto));
        } catch (Exception e) {
            throw new DomainException(CriteriocohorteErrorCode.CRITERIOCOHORTE_DUPLICADO, input);
        }
    }

    @Override
    public CriteriocohorteOutput update(CRITERIOCOHORTE_UPDATE input) {
        try {
            assertSinCalificaciones(input.id());
            CriteriocohorteDTO dto = map.toDto(input);
            return map.toOutput(service.update(input.id(), dto));
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            throw new DomainException(CriteriocohorteErrorCode.CRITERIOCOHORTE_NOT_FOUND, input.id());
        }
    }

    @Override
    public CriteriocohorteOutput patch(CRITERIOCOHORTE_PATCH input) {
        return null;
    }

    @Override
    public CriteriocohorteOutput findById(CRITERIOCOHORTE_FIND input) {
        try {
            return map.toOutput(service.findById(input.id()));
        } catch (Exception e) {
            throw new DomainException(CriteriocohorteErrorCode.CRITERIOCOHORTE_NOT_FOUND, input.id());
        }
    }

    @Override
    public List<CriteriocohorteOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new DomainException(CriteriocohorteErrorCode.CRITERIOCOHORTE_NOT_FOUND, "findAll");
        }
    }

    @Override
    public void deleteById(CRITERIOCOHORTE_DELETE input) {
        try {
            assertSinCalificaciones(input.id());
            service.deleteById(input.id());
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            throw new DomainException(CriteriocohorteErrorCode.CRITERIOCOHORTE_NOT_FOUND, input.id());
        }
    }

    public List<CriteriocohorteOutput> findByIdCohorte(Integer idCohorte) {
        return service.findByIdCohorte(idCohorte).stream().map(map::toOutput).toList();
    }

    public CriteriocohorteOutput assign(Integer idCohorte, Integer idCriterio, BigDecimal pesoSnapshot) {
        CohorteDTO cohorte = cohorteService.findById(idCohorte);
        if (cohorte == null) {
            throw new DomainException(CriteriocohorteErrorCode.CRITERIOCOHORTE_NOT_FOUND, idCohorte);
        }

        CriterioevaluacionDTO criterio = criterioevaluacionService.findById(idCriterio);
        if (criterio == null) {
            throw new DomainException(CriteriocohorteErrorCode.CRITERIOCOHORTE_NOT_FOUND, idCriterio);
        }

        if (!Objects.equals(criterio.getIdprograma(), cohorte.getIdPrograma())) {
            throw new DomainException(CriteriocohorteErrorCode.CRITERIO_NO_PERTENECE_AL_PROGRAMA, idCriterio);
        }

        if (Boolean.FALSE.equals(criterio.getActivo())) {
            throw new DomainException(CriteriocohorteErrorCode.CRITERIO_INACTIVO, idCriterio);
        }

        boolean yaAsignado = service.findByIdCohorte(idCohorte).stream()
                .anyMatch(cc -> Objects.equals(cc.getIdCriterio(), idCriterio));
        if (yaAsignado) {
            throw new DomainException(CriteriocohorteErrorCode.CRITERIO_YA_ASIGNADO_A_COHORTE, idCriterio);
        }

        BigDecimal pesoEfectivo = pesoSnapshot != null ? pesoSnapshot : criterio.getPeso();
        if (pesoEfectivo == null) pesoEfectivo = BigDecimal.ZERO;

        BigDecimal sumaActual = service.findByIdCohorte(idCohorte).stream()
                .map(CriteriocohorteDTO::getPesoSnapshot)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sumaActual.add(pesoEfectivo).compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new DomainException(CriteriocohorteErrorCode.PESO_SNAPSHOT_EXCEDE_LIMITE, idCohorte);
        }

        return create(CRITERIOCOHORTE_CREATE.builder()
                .idCohorte(idCohorte)
                .idCriterio(idCriterio)
                .pesoSnapshot(pesoEfectivo)
                .build());
    }

    public CriteriocohorteOutput updatePeso(Integer id, BigDecimal pesoSnapshot) {
        CriteriocohorteOutput existing = findById(new CRITERIOCOHORTE_FIND(id));
        assertSinCalificaciones(id);

        BigDecimal pesoEfectivo = pesoSnapshot != null ? pesoSnapshot : BigDecimal.ZERO;
        BigDecimal sumaActual = service.findByIdCohorte(existing.idCohorte()).stream()
                .filter(cc -> !Objects.equals(cc.getId(), id))
                .map(CriteriocohorteDTO::getPesoSnapshot)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sumaActual.add(pesoEfectivo).compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new DomainException(CriteriocohorteErrorCode.PESO_SNAPSHOT_EXCEDE_LIMITE, existing.idCohorte());
        }

        CriteriocohorteOutput updated = update(CRITERIOCOHORTE_UPDATE.builder()
                .id(id)
                .idCohorte(existing.idCohorte())
                .idCriterio(existing.idCriterio())
                .pesoSnapshot(pesoSnapshot)
                .build());

        aspiranteService.findByCohorte(existing.idCohorte())
                .stream()
                .map(AspiranteDTO::getId)
                .filter(Objects::nonNull)
                .forEach(calificacioncriterioProcessor::recalcularPuntuacionAspirantePublic);

        return updated;
    }

    private void assertSinCalificaciones(Integer idCriteriocohorte) {
        if (calificacioncriterioService.existsByCriterio(idCriteriocohorte)) {
            throw new DomainException(CriterioevaluacionErrorCode.CRITERIO_CON_CALIFICACIONES_BLOQUEADO, idCriteriocohorte);
        }
    }
}
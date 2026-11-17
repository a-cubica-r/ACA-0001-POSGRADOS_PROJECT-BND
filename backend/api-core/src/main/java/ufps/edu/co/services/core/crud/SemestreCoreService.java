package ufps.edu.co.services.core.crud;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.SemestreErrorCode;
import ufps.edu.co.maps.specific.SemestreMap;
import ufps.edu.co.records.input.entity.SemestreInput.*;
import ufps.edu.co.records.output.entity.SemestreOutput;
import ufps.edu.co.rest.dto.SemestreDTO;
import ufps.edu.co.rest.services.SemestreService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class SemestreCoreService implements
        GlobalUseCase<SEMESTRE_CREATE, SEMESTRE_UPDATE, SEMESTRE_DELETE, SEMESTRE_PATCH, SEMESTRE_FIND, SemestreOutput> {

    @Autowired
    private SemestreService service;

    @Autowired
    private SemestreMap map;

    @Override
    public SemestreOutput create(SEMESTRE_CREATE input) {
        SemestreDTO dto = map.toDto(input);
        prepararYValidar(dto, null);
        SemestreDTO created = service.create(dto);
        return map.toOutput(created);

    }

    @Override
    public SemestreOutput update(SEMESTRE_UPDATE input) {
        if (input.id() == null) {
            throw new DomainException(SemestreErrorCode.SEMESTRE_PARAMETRO_REQUERIDO, "id");
        }
        SemestreDTO dto = map.toDto(input);
        prepararYValidar(dto, input.id());
        SemestreDTO updated = service.update(input.id(), dto);
        return map.toOutput(updated);

    }

    @Override
    public SemestreOutput patch(SEMESTRE_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Semestre");
    }

    @Override
    public SemestreOutput findById(SEMESTRE_FIND input) {
        SemestreDTO dto = service.findById(input.id());
        return map.toOutput(dto);

    }

    @Override
    public List<SemestreOutput> findAll() {
        return service.findAll().stream().map(map::toOutput).toList();

    }

    @Override
    public void deleteById(SEMESTRE_DELETE input) {
        if (input.id() == null) {
            throw new DomainException(SemestreErrorCode.SEMESTRE_PARAMETRO_REQUERIDO, "id");
        }
        service.deleteById(input.id());
    }

    /**
     * Normaliza el nombre y valida, en este orden: fechas coherentes, periodo
     * (año + enero-junio/julio-diciembre) y nombre único. Se ejecuta antes de
     * guardar.
     */
    private void prepararYValidar(SemestreDTO dto, Integer idQueSeEdita) {
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new DomainException(SemestreErrorCode.SEMESTRE_PARAMETRO_REQUERIDO, "nombre");
        }
        dto.setNombre(dto.getNombre().trim());
        if (dto.getFechaInicio() == null || dto.getFechaFin() == null
                || !dto.getFechaInicio().isBefore(dto.getFechaFin())) {
            throw new DomainException(SemestreErrorCode.SEMESTRE_FECHAS_INVALIDAS, dto.getNombre());
        }

        SemestrePeriodo periodo = SemestrePeriodo.de(dto.getFechaInicio());
        if (service.existsByPeriodo(periodo.anio(), periodo.periodo(), idQueSeEdita)) {
            throw new DomainException(SemestreErrorCode.SEMESTRE_PERIODO_DUPLICADO,
                    periodo.anio() + "-" + periodo.periodo());
        }

        if (dto.getNombre() != null && !dto.getNombre().isEmpty()
                && service.existsByNombre(dto.getNombre(), idQueSeEdita)) {
            throw new DomainException(SemestreErrorCode.SEMESTRE_NOMBRE_DUPLICADO, dto.getNombre());
        }
    }
}


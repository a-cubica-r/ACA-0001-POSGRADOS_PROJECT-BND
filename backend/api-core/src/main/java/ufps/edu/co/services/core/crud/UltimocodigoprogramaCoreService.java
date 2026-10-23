package ufps.edu.co.services.core.crud;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.UltimocodigoprogramaErrorCode;
import ufps.edu.co.records.output.entity.UltimocodigoprogramaOutput;
import ufps.edu.co.rest.dto.UltimocodigoprogramaDTO;
import ufps.edu.co.rest.services.EstudiantesService;
import ufps.edu.co.rest.services.UltimocodigoprogramaService;

/**
 * Reglas del consecutivo del código de estudiante.
 *
 * <p>El consecutivo vive en {@code ultimocodigoprograma} y es la fuente de la
 * verdad: {@code reservarSiguiente} lo bloquea, lo avanza y devuelve el número
 * que le toca al estudiante. La consulta de la pantalla no bloquea.</p>
 *
 * <p>El mínimo nunca queda por debajo del siguiente al último estudiante del
 * programa: así, si el consecutivo se atrasó (programas sembrados a mano, filas
 * restauradas), no se repiten códigos.</p>
 */
@Service
public class UltimocodigoprogramaCoreService {

    private static final Logger log = LoggerFactory.getLogger(UltimocodigoprogramaCoreService.class);

    private static final int CODIGO_MINIMO = 0;
    private static final int CODIGO_MAXIMO = 9999;

    @Autowired
    private UltimocodigoprogramaService service;

    @Autowired
    private EstudiantesService estudiantesService;

    /** Una entrada por programa, ordenada por nombre. */
    @Transactional(readOnly = true)
    public List<UltimocodigoprogramaOutput> findAll() {
        return service.findAllConNombrePrograma().stream()
                .map(this::toOutput)
                .toList();
    }

    /**
     * Fija el consecutivo a mano. Bloquea la fila para que la edición no se
     * cruce con una reserva y rechaza valores que generarían códigos repetidos.
     */
    @Transactional
    public UltimocodigoprogramaOutput updateCodigoPorPrograma(Integer idPrograma, Integer nuevoCodigo) {
        if (nuevoCodigo == null || nuevoCodigo < CODIGO_MINIMO || nuevoCodigo > CODIGO_MAXIMO) {
            throw new DomainException(UltimocodigoprogramaErrorCode.ULTIMOCODIGOPROGRAMA_CODIGO_INVALIDO,
                    nuevoCodigo);
        }

        UltimocodigoprogramaDTO dto = idPrograma == null ? null : service.findByIdProgramaForUpdate(idPrograma);
        if (dto == null) {
            throw new DomainException(UltimocodigoprogramaErrorCode.ULTIMOCODIGOPROGRAMA_NOT_FOUND, idPrograma);
        }

        int minimo = SecuencialCodigoEstudiante.siguiente(
                estudiantesService.findUltimoCodigoPorProgramaSinBloqueo(idPrograma));
        if (nuevoCodigo < minimo) {
            throw new DomainException(
                    UltimocodigoprogramaErrorCode.ULTIMOCODIGOPROGRAMA_CODIGO_MENOR_QUE_ESTUDIANTES_CONFLICT,
                    Map.of("idPrograma", idPrograma, "codigoMinimo", minimo));
        }

        dto.setCodigo(nuevoCodigo);
        return toOutput(service.update(dto.getId(), dto));
    }

    /**
     * Reserva el consecutivo que le toca al siguiente estudiante del programa y
     * deja guardado el siguiente a ese.
     *
     * <p>Debe llamarse dentro de una transacción de escritura: el bloqueo de la
     * fila dura hasta el commit, así que dos aprobaciones simultáneas del mismo
     * programa no obtienen el mismo número.</p>
     *
     * @return el secuencial a asignar (0..9999)
     * @throws DomainException {@code ULTIMOCODIGOPROGRAMA_AGOTADO_CONFLICT} si el
     *                         programa ya no tiene códigos disponibles (10000)
     */
    @Transactional
    public int reservarSiguiente(Integer idPrograma) {
        if (idPrograma == null) {
            throw new DomainException(UltimocodigoprogramaErrorCode.ULTIMOCODIGOPROGRAMA_NOT_FOUND, null);
        }

        UltimocodigoprogramaDTO dto = service.findByIdProgramaForUpdate(idPrograma);

        // Se conserva la lectura con bloqueo que ya usaba la generación: el
        // número no puede quedar por debajo del último estudiante registrado.
        int minimo = SecuencialCodigoEstudiante.siguiente(
                estudiantesService.findUltimoCodigoPorPrograma(idPrograma));

        int siguiente;
        if (dto == null) {
            siguiente = minimo;
            dto = UltimocodigoprogramaDTO.builder()
                    .idPrograma(idPrograma)
                    .build();
            log.warn("El programa {} no tenía consecutivo; se crea en {}", idPrograma, siguiente + 1);
        } else {
            int actual = dto.getCodigo() != null ? dto.getCodigo() : minimo;
            siguiente = Math.max(actual, minimo);
            if (siguiente != actual) {
                log.warn("El consecutivo del programa {} estaba atrasado ({}); se usa {}",
                        idPrograma, actual, siguiente);
            }
        }

        if (siguiente > CODIGO_MAXIMO) {
            throw new DomainException(UltimocodigoprogramaErrorCode.ULTIMOCODIGOPROGRAMA_AGOTADO_CONFLICT,
                    Map.of("idPrograma", idPrograma, "siguiente", siguiente));
        }

        dto.setCodigo(siguiente + 1);
        if (dto.getId() == null) {
            service.create(dto);
        } else {
            service.update(dto.getId(), dto);
        }
        return siguiente;
    }

    private UltimocodigoprogramaOutput toOutput(UltimocodigoprogramaDTO dto) {
        if (dto == null) {
            return null;
        }
        return UltimocodigoprogramaOutput.builder()
                .id(dto.getId() != null ? dto.getId() : dto.getIdPrograma())
                .idPrograma(dto.getIdPrograma())
                .nombrePrograma(dto.getNombrePrograma())
                .codigo(dto.getCodigo() != null ? dto.getCodigo() : 0)
                .build();
    }
}

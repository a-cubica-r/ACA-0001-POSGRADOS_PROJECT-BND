package ufps.edu.co.processor.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;

import java.sql.SQLException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.SedeErrorCode;
import ufps.edu.co.maps.specific.PlazoMap;
import ufps.edu.co.maps.specific.SedeMap;
import ufps.edu.co.records.input.entity.PlazoInput.PLAZO_DELETE;
import ufps.edu.co.records.input.entity.SedeInput.SEDE_DELETE;
import ufps.edu.co.rest.exceptions.RegistroNoEncontradoException;
import ufps.edu.co.rest.services.PlazoService;
import ufps.edu.co.rest.services.SedeService;
import ufps.edu.co.services.core.crud.PlazoCoreService;
import ufps.edu.co.services.core.crud.SedeCoreService;
import ufps.edu.co.services.core.crud.UbicacionCoreService;

/**
 * D-09 T1 — El deleteById de los CoreService deja de convertir cualquier fallo
 * en "no encontrado": solo el registro ausente se traduce a un código
 * {@code *_NOT_FOUND}; una violación de integridad sale sin envolver para que
 * el handler global la responda como 409.
 */
@ExtendWith(MockitoExtension.class)
class DeleteByIdNoEncontradoTest {

    private static final Integer ID = 7;

    @Mock private SedeService sedeService;
    @Mock private SedeMap sedeMap;
    @Mock private UbicacionCoreService ubicacionProcessor;
    @InjectMocks private SedeCoreService sedeProcessor;

    @Mock private PlazoService plazoService;
    @Mock private PlazoMap plazoMap;
    @InjectMocks private PlazoCoreService plazoProcessor;

    private static DataIntegrityViolationException enUso() {
        return new DataIntegrityViolationException("fallo de integridad",
                new SQLException("Cannot delete or update a parent row: a foreign key constraint fails "
                        + "(`railway`.`plazo`, CONSTRAINT `plazo_ibfk_1` FOREIGN KEY (`id_tipoplazo`) "
                        + "REFERENCES `tipoplazo` (`id`) ON DELETE RESTRICT)", "23000", 1451));
    }

    // ─── Entidad con código propio: Sede ─────────────────────────────────────

    @Test
    @DisplayName("Sede inexistente → DomainException SEDE_NOT_FOUND")
    void sedeNoEncontrada() {
        doThrow(new RegistroNoEncontradoException("Sede no encontrado con id: " + ID))
                .when(sedeService).deleteById(ID);

        DomainException ex = assertThrows(DomainException.class,
                () -> sedeProcessor.deleteById(new SEDE_DELETE(ID)));

        assertEquals(SedeErrorCode.SEDE_NOT_FOUND, ex.getCode());
        assertEquals(ID, ex.getParam());
    }

    @Test
    @DisplayName("Sede en uso → DataIntegrityViolationException sale sin envolver")
    void sedeEnUsoNoSeConvierteEnNotFound() {
        DataIntegrityViolationException original = enUso();
        doThrow(original).when(sedeService).deleteById(ID);

        DataIntegrityViolationException ex = assertThrows(DataIntegrityViolationException.class,
                () -> sedeProcessor.deleteById(new SEDE_DELETE(ID)));

        assertSame(original, ex);
    }

    // ─── Entidad sin *_NOT_FOUND propio: Plazo ──────────────────────────────

    @Test
    @DisplayName("Plazo inexistente → RegistroNoEncontradoException (la resuelve el handler como 404)")
    void plazoNoEncontrado() {
        doThrow(new RegistroNoEncontradoException("Plazo no encontrado con id: " + ID))
                .when(plazoService).deleteById(ID);

        assertThrows(RegistroNoEncontradoException.class,
                () -> plazoProcessor.deleteById(new PLAZO_DELETE(ID)));
    }

    @Test
    @DisplayName("Plazo en uso → DataIntegrityViolationException sale sin envolver")
    void plazoEnUsoNoSeConvierteEnNotFound() {
        DataIntegrityViolationException original = enUso();
        doThrow(original).when(plazoService).deleteById(ID);

        DataIntegrityViolationException ex = assertThrows(DataIntegrityViolationException.class,
                () -> plazoProcessor.deleteById(new PLAZO_DELETE(ID)));

        assertSame(original, ex);
    }
}

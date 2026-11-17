package ufps.edu.co.controllers.cases.aspirante;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.AspiranteErrorCode;
import ufps.edu.co.rest.services.PersonaService;

@ExtendWith(MockitoExtension.class)
class InscripcionPromedioTest {

    @Mock
    private PersonaService personaService;

    @InjectMocks
    private InscripcionCase inscripcionCase;

    @ParameterizedTest
    @ValueSource(strings = { "5.01", "6", "-0.1", "4.567" })
    void rechazaPromediosFueraDeRangoConDomainException(String valor) {
        DomainException ex = assertThrows(DomainException.class,
                () -> inscripcionCase.registrarNuevoUsuario(request(new BigDecimal(valor))));

        assertEquals(AspiranteErrorCode.PROMEDIO_INSCRIPCION_FUERA_DE_RANGO, ex.getCode());
        verifyNoInteractions(personaService);
    }

    private InscripcionCase.RegistrarNuevoUsuarioRequest request(BigDecimal promedio) {
        return new InscripcionCase.RegistrarNuevoUsuarioRequest(
                "Ana", "Pérez", 1, "123456", 1, 1, null, null, null, 1,
                null, "Ingeniería", "ana@example.com", null, "3001234567", promedio,
                null, null, null, false, null, null,
                new InscripcionCase.UbicacionNacimientoRequest(1, 1), null,
                new InscripcionCase.UbicacionResidenciaRequest("URBANA", 1, 1, "Calle 1"),
                1, 1, "ana", "Clave123*");
    }
}

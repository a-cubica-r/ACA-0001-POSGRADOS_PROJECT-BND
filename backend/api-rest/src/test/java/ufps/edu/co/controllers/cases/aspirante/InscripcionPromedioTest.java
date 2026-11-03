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
                () -> inscripcionCase.registrarFormulario(request(new BigDecimal(valor))));

        assertEquals(AspiranteErrorCode.PROMEDIO_INSCRIPCION_FUERA_DE_RANGO, ex.getCode());
        verifyNoInteractions(personaService);
    }

    private InscripcionCase.FormularioInscripcionRequest request(BigDecimal promedio) {
        return new InscripcionCase.FormularioInscripcionRequest(
                "Ana", "Pérez", null, null, null, null, null, null, null, null,
                null, null, "ANA@EXAMPLE.COM", null, "3001234567", promedio,
                null, null, null, null, null, null, null, null, null, 1, null, "ana");
    }
}

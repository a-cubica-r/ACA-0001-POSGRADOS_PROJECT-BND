package ufps.edu.co.services.core.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SemestrePeriodoTest {

    @Test
    @DisplayName("enero a junio es periodo 1")
    void primerSemestre() {
        assertEquals(new SemestrePeriodo(2028, 1), SemestrePeriodo.de(LocalDate.of(2028, 1, 1)));
        assertEquals(new SemestrePeriodo(2028, 1), SemestrePeriodo.de(LocalDate.of(2028, 6, 30)));
        assertEquals(new SemestrePeriodo(2026, 1), SemestrePeriodo.de(LocalDate.of(2026, 1, 31)));
    }

    @Test
    @DisplayName("julio a diciembre es periodo 2")
    void segundoSemestre() {
        assertEquals(new SemestrePeriodo(2028, 2), SemestrePeriodo.de(LocalDate.of(2028, 7, 1)));
        assertEquals(new SemestrePeriodo(2028, 2), SemestrePeriodo.de(LocalDate.of(2028, 12, 31)));
    }
}

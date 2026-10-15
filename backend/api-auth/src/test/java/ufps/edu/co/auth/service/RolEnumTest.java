package ufps.edu.co.auth.service;

import org.junit.jupiter.api.Test;

import ufps.edu.co.enums.Rol;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SC-05 - SecurityConfig usa {@code Rol.X.getCodigo()} en {@code hasRole}/{@code hasAnyRole}.
 * El JWT lleva el nombre del rol de la BD normalizado por {@link RoleNameNormalizer};
 * ambos deben coincidir para que la autorización no cambie.
 */
class RolEnumTest {

    @Test
    void codigoDeCadaRol_esElNombreDeBdNormalizado() {
        for (Rol rol : Rol.values()) {
            assertEquals(RoleNameNormalizer.normalize(rol.getValor()), rol.getCodigo(),
                    "El código del rol no coincide con el nombre normalizado: " + rol.getValor());
        }
    }

    @Test
    void codigos_sonLosQueUsabaSecurityConfig() {
        assertEquals("SUPER_ADMINISTRADOR", Rol.SUPER_ADMINISTRADOR.getCodigo());
        assertEquals("POSGRADOS", Rol.POSGRADOS.getCodigo());
        assertEquals("DIRECTOR_DE_PROGRAMA", Rol.DIRECTOR_DE_PROGRAMA.getCodigo());
        assertEquals("ASPIRANTE", Rol.ASPIRANTE.getCodigo());
    }
}

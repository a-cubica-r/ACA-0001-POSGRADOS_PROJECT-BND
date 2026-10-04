package ufps.edu.co.mapping;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Configuracion comun de todos los mappers MapStruct (reemplaza ModelMapperConfig).
 * <ul>
 * <li>Los mappers son beans de Spring y reciben sus dependencias por constructor.</li>
 * <li>{@code unmappedTargetPolicy = ERROR}: cualquier propiedad destino que no tenga una regla
 * explicita (mapeo o {@code ignore}) rompe la compilacion, de modo que ningun campo queda
 * sin decision al agregar o cambiar atributos en Entities/DTOs.</li>
 * </ul>
 */
@MapperConfig(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MapStructConfig {
}

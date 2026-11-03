package ufps.edu.co.records.input.entity;

import java.time.LocalDate;
import java.math.BigDecimal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import ufps.edu.co.records.contracts.CreateType;
import ufps.edu.co.records.contracts.DeleteType;
import ufps.edu.co.records.contracts.FindType;
import ufps.edu.co.records.contracts.PatchType;
import ufps.edu.co.records.contracts.UpdateType;

public enum PersonaInput {
        ;

        public record PERSONA_CREATE(
                        String nombres,
                        String apellidos,
                        String correo,
                        LocalDate fechanacimiento,
                        String celular,
                        String telefono,
                        Integer idUbicacionvivienda,
                        Integer idUbicacionnacimiento,
                        Integer idUbicaciontrabajo,
                        Integer idGenero,
                        Integer idEstadocivil,
                        Integer idGrupoetnico,
                        Integer idPoblacionindigena,
                        Integer idDiscapacidad,
                        Integer idCapacidadexepcional,
                        Integer idDocumentopersona,
                        BigDecimal promediopregrado,
                        String titulopregrado,
                        String titulosposgrados,
                        String empresa,
                        String experiencialaboral,
                        Boolean egresadoufps) implements CreateType {
        }

        public record PERSONA_UPDATE(
                        Integer id,
                        String nombres,
                        String apellidos,
                        String correo,
                        LocalDate fechanacimiento,
                        String celular,
                        String telefono,
                        Integer idUbicacionvivienda,
                        Integer idUbicacionnacimiento,
                        Integer idUbicaciontrabajo,
                        Integer idGenero,
                        Integer idEstadocivil,
                        Integer idGrupoetnico,
                        Integer idPoblacionindigena,
                        Integer idDiscapacidad,
                        Integer idCapacidadexepcional,
                        Integer idDocumentopersona,
                        BigDecimal promediopregrado,
                        String titulopregrado,
                        String titulosposgrados,
                        String empresa,
                        String experiencialaboral,
                        Boolean egresadoufps) implements UpdateType {
        }

        public record PERSONA_PATCH(
                        Integer id,
                        String nombres,
                        String apellidos,
                        String correo,
                        LocalDate fechanacimiento,
                        String celular,
                        String telefono,
                        Integer idUbicacionvivienda,
                        Integer idUbicacionnacimiento,
                        Integer idUbicaciontrabajo,
                        Integer idGenero,
                        Integer idEstadocivil,
                        Integer idGrupoetnico,
                        Integer idPoblacionindigena,
                        Integer idDiscapacidad,
                        Integer idCapacidadexepcional,
                        Integer idDocumentopersona,
                        BigDecimal promediopregrado,
                        String titulopregrado,
                        String titulosposgrados,
                        String empresa,
                        String experiencialaboral,
                        Boolean egresadoufps) implements PatchType {
        }

        public record PERSONA_DELETE(
                        Integer id) implements DeleteType {
        }

        public record PERSONA_FIND(
                        Integer id) implements FindType {
        }

        /**
         * Ubicación opcional dentro del alta completa: se valida en el servicio
         * (dirección y municipio deben venir juntos) y no con anotaciones, porque
         * el front puede enviar objetos con un solo campo.
         */
        @Builder
        public record UBICACION_OPCIONAL(
                        String direccion,
                        Integer idMunicipio) {
        }

        @Builder
        public record PERSONA_FULL_CREATE(
                        @NotBlank @Size(max = 100) String nombres,
                        @NotBlank @Size(max = 100) String apellidos,
                        @NotBlank @Email @Size(max = 100) String correo,
                        LocalDate fechanacimiento,
                        @NotBlank @Pattern(regexp = "\\d{10,12}") String celular,
                        @Size(max = 12) String telefono,
                        Integer idGenero,
                        Integer idEstadocivil,
                        Integer idGrupoetnico,
                        Integer idPoblacionindigena,
                        Integer idDiscapacidad,
                        Integer idCapacidadexepcional,
                        BigDecimal promediopregrado,
                        @Size(max = 100) String titulopregrado,
                        String titulosposgrados,
                        @Size(max = 100) String empresa,
                        String experiencialaboral,
                        Boolean egresadoufps,
                        UBICACION_OPCIONAL ubicacionvivienda,
                        UBICACION_OPCIONAL ubicacionnacimiento,
                        UBICACION_OPCIONAL ubicaciontrabajo,
                        String numerodocumento,
                        Integer idTipodocumento,
                        UBICACION_OPCIONAL lugarexpedicion) implements CreateType {
        }
}

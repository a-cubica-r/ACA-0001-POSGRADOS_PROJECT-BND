package ufps.edu.co.records.input.entity;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ufps.edu.co.records.contracts.*;

public enum CohorteInput {
        ;

        public record COHORTE_CREATE(
                        String nombre,
                        @NotBlank Integer cupos,
                        @NotNull Integer idEstado,
                        @NotNull Integer idSemestre,
                        @NotNull Integer idModalidad,
                        @NotNull Integer idPlazodocumentacion,
                        @NotNull Integer idPlazoinscripcion,
                        @NotNull Integer idPlazopago,
                        @NotNull Integer idPrograma) implements CreateType {
        }

        public record COHORTE_UPDATE(
                        @NotNull Integer id,
                        @NotBlank String nombre,
                        @NotBlank Integer cupos,
                        @NotNull Integer idEstado,
                        @NotNull Integer idSemestre,
                        @NotNull Integer idModalidad,
                        @NotNull Integer idPlazodocumentacion,
                        @NotNull Integer idPlazoinscripcion,
                        @NotNull Integer idPlazopago,
                        @NotNull Integer idPrograma) implements UpdateType {
        }

        public record COHORTE_PATCH(
                        @NotNull Integer id,
                        @NotBlank String nombre,
                        @NotBlank Integer cupos,
                        @NotNull Integer idEstado,
                        @NotNull Integer idSemestre,
                        @NotNull Integer idModalidad,
                        @NotNull Integer idPlazodocumentacion,
                        @NotNull Integer idPlazoinscripcion,
                        @NotNull Integer idPlazopago,
                        @NotNull Integer idPrograma) implements PatchType {
        }

        public record COHORTE_DELETE(@NotNull Integer id) implements DeleteType {
        }

        public record COHORTE_FIND(@NotNull Integer id) implements FindType {
        }

        public record COHORTE_DIRECTOR_CREATE(
                        @NotBlank String nombre,
                        @NotNull Integer cupos,
                        @NotNull Integer idSemestre,
                        @NotNull Integer idModalidad,
                        @NotNull LocalDate fechaInicioDocumentacion,
                        @NotNull LocalDate fechaFinDocumentacion,
                        @NotNull LocalDate fechaInicioInscripcion,
                        @NotNull LocalDate fechaFinInscripcion,
                        @NotNull LocalDate fechaInicioPago,
                        @NotNull LocalDate fechaFinPago,
                        List<DOCUMENTO_ASIGNADO_CREATE> documentosConsejo,
                        List<DOCUMENTO_ASIGNADO_CREATE> documentosPrograma,
                        List<CriteriocohorteInput.CRITERIOCOHORTE_CREATE> criteriosCohorte) implements CreateType {
        }

        public record CRITERIOCOHORTE_DIRECTOR_UPDATE(
                        @NotNull Integer id,
                        @NotNull Integer idCriterio,
                        java.math.BigDecimal pesoSnapshot) {
        }

        public record DOCUMENTO_ASIGNADO_CREATE(
                        Integer id,
                        Integer idDocrequisito,
                        Integer idCohorte,
                        String nombre) {
        }

        public record COHORTE_DIRECTOR_UPDATE(
                        @NotNull Integer id,
                        @NotBlank String nombre,
                        @NotNull Integer cupos,
                        @NotNull Integer idSemestre,
                        @NotNull Integer idModalidad,
                        // Deprecado: alias por compatibilidad con el frontend anterior.
                        LocalDate fechaInicio,
                        // Deprecado: alias por compatibilidad con el frontend anterior.
                        LocalDate fechaLimiteDocumentos,
                        // Deprecado: alias por compatibilidad con el frontend anterior.
                        LocalDate fechaLimitePago,
                        LocalDate fechaInicioDocumentacion,
                        LocalDate fechaFinDocumentacion,
                        LocalDate fechaInicioInscripcion,
                        LocalDate fechaFinInscripcion,
                        LocalDate fechaInicioPago,
                        LocalDate fechaFinPago,
                        List<DOCUMENTO_ASIGNADO_CREATE> documentosConsejo,
                        List<DOCUMENTO_ASIGNADO_CREATE> documentosPrograma,
                        List<CRITERIOCOHORTE_DIRECTOR_UPDATE> criteriosCohorte) implements UpdateType {
        }

        public record COHORTE_WITHPLAZO_CREATE(
                        String nombre,
                        @NotBlank Integer cupos,
                        @NotNull Integer idEstado,
                        @NotNull Integer idSemestre,
                        @NotNull Integer idModalidad,
                        @NotNull Integer idPlazodocumentacion,
                        @NotNull Integer idPlazoinscripcion,
                        @NotNull Integer idPlazopago,
                        @NotNull Integer idPrograma,
                        @NotNull PlazoInput.PLAZO_CREATE plazocreate) implements CreateType {
        }
}

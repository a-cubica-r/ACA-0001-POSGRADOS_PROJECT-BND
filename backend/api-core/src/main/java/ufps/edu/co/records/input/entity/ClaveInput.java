package ufps.edu.co.records.input.entity;

import jakarta.validation.constraints.NotNull;
import ufps.edu.co.records.contracts.*;

public enum ClaveInput {
    ;

    public record CLAVE_CREATE(
            @NotNull String valor) implements CreateType {
        @Override
        public String toString() {
            return "CLAVE_CREATE[valor=***]";
        }
    }

    public record CLAVE_UPDATE(
            @NotNull Integer id,
            @NotNull String valor) implements UpdateType {
        @Override
        public String toString() {
            return "CLAVE_UPDATE[id=" + id + ", valor=***]";
        }
    }

    public record CLAVE_PATCH(
            @NotNull Integer id,
            @NotNull String valor) implements PatchType {
        @Override
        public String toString() {
            return "CLAVE_PATCH[id=" + id + ", valor=***]";
        }
    }

    public record CLAVE_DELETE(
            @NotNull Integer id) implements DeleteType {
    }

    public record CLAVE_FIND(
            @NotNull Integer id) implements FindType {
    }
}
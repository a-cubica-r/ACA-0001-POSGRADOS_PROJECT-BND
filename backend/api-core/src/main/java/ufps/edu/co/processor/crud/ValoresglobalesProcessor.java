package ufps.edu.co.processor.crud;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.PagoErrorCode;
import ufps.edu.co.domain.exceptions.errorcodes.VariablesglobalesErrorCode;
import ufps.edu.co.maps.specific.ValoresglobalesMap;
import ufps.edu.co.rest.dto.ValoresglobalesDTO;
import ufps.edu.co.rest.services.ValoresglobalesService;

@Service
public class ValoresglobalesProcessor {

    private static final Pattern GLOBAL_KEY_PATTERN = Pattern.compile("^(?<prefix>[A-Z_]+)_(?<year>\\d{4})_(?<version>\\d+)$");

    @Autowired
    private ValoresglobalesService valoresglobalesService;

    @Autowired
    private ValoresglobalesMap valoresglobalesMap;

    public ValoresglobalesDTO findCurrentByPrefix(String prefix) {
        String normalizedPrefix = normalizePrefix(prefix);
        int currentYear = LocalDate.now().getYear();

        return findAll().stream()
            .filter(item -> item.getClave() != null)
            .filter(item -> matchesCurrentFamily(item.getClave(), normalizedPrefix, currentYear))
            .max(Comparator.comparingInt(item -> extractVersionForAliases(item.getClave(), normalizedPrefix, currentYear)))
            .orElseThrow(() -> new DomainException(
                VariablesglobalesErrorCode.VALOR_GLOBAL_NO_CONFIGURADO_NOT_FOUND_TAMANO_MAXIMO,
                normalizedPrefix + "_" + currentYear));
    }

    public List<ValoresglobalesDTO> findAllByPrefix(String prefix) {
        String normalizedPrefix = normalizePrefix(prefix);
        return findAll().stream()
                .filter(item -> item.getClave() != null)
            .filter(item -> matchesFamily(item.getClave(), normalizedPrefix))
                .sorted((left, right) -> right.getClave().compareToIgnoreCase(left.getClave()))
                .collect(Collectors.toList());
    }

    public Long getCurrentSalaryMinimumPesos() {
        ValoresglobalesDTO dto = findCurrentByPrefix("SALARIO_MINIMO");
        Long salarioMinimo = valoresglobalesMap.parsePesos(dto.getValor());
        if (salarioMinimo == null) {
            throw new DomainException(PagoErrorCode.VALOR_GLOBAL_FORMATO_INVALIDO, dto.getClave());
        }
        return salarioMinimo;
    }

    public BigDecimal getCurrentValorInscripcionMultiplier() {
        ValoresglobalesDTO dto = findCurrentByPrefix("VALOR_INSCRIPCION");
        BigDecimal multiplier = valoresglobalesMap.parseSmmlvMultiplier(dto.getValor());
        if (multiplier == null) {
            throw new DomainException(PagoErrorCode.VALOR_GLOBAL_FORMATO_INVALIDO, dto.getClave());
        }
        return multiplier;
    }

    public BigDecimal calcularValorInscripcionPesos() {
        BigDecimal salarioMinimo = BigDecimal.valueOf(getCurrentSalaryMinimumPesos());
        BigDecimal multiplier = getCurrentValorInscripcionMultiplier();
        return salarioMinimo.multiply(multiplier);
    }

    public long calcularValorInscripcionCentavos() {
        return calcularValorInscripcionPesos().movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private List<ValoresglobalesDTO> findAll() {
        return valoresglobalesService.findAll();
    }

    private Integer extractVersion(String clave, String prefix, int year) {
        Matcher matcher = GLOBAL_KEY_PATTERN.matcher(clave);
        if (!matcher.matches()) {
            return 0;
        }
        if (!prefix.equalsIgnoreCase(matcher.group("prefix"))) {
            return 0;
        }
        if (Integer.parseInt(matcher.group("year")) != year) {
            return 0;
        }
        return Integer.parseInt(matcher.group("version"));
    }

    private Boolean matchesCurrentFamily(String clave, String prefix, int year) {
        for (String candidatePrefix : resolvePrefixAliases(prefix)) {
            if (clave.startsWith(candidatePrefix + "_" + year + "_")) {
                return true;
            }
        }
        return false;
    }

    private Boolean matchesFamily(String clave, String prefix) {
        for (String candidatePrefix : resolvePrefixAliases(prefix)) {
            if (clave.startsWith(candidatePrefix + "_")) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    @Deprecated(since = "2024-06", forRemoval = true)
    private String familyPrefixFor(String clave, String requestedPrefix) {
        for (String candidatePrefix : resolvePrefixAliases(requestedPrefix)) {
            if (clave.startsWith(candidatePrefix + "_")) {
                return candidatePrefix;
            }
        }
        return requestedPrefix;
    }

    private List<String> resolvePrefixAliases(String prefix) {
        String normalizedPrefix = normalizePrefix(prefix);
        if ("TAMANO_MAXIMO".equals(normalizedPrefix) || "TAMANO_MAXIMO_ARCHIVOS".equals(normalizedPrefix)) {
            return List.of("TAMANO_MAXIMO", "TAMANO_MAXIMO_ARCHIVOS");
        }
        return List.of(normalizedPrefix);
    }

    public ValoresglobalesDTO createTamanoMaximoArchivos(ValoresglobalesDTO dto) {
        return createForPrefix("TAMANO_MAXIMO_ARCHIVOS", dto, "MB");
    }

    public ValoresglobalesDTO createSalarioMinimo(ValoresglobalesDTO dto) {
        return createForPrefix("SALARIO_MINIMO", dto);
    }

    public ValoresglobalesDTO createValorInscripcion(ValoresglobalesDTO dto) {
        return createForPrefix("VALOR_INSCRIPCION", dto, "SMMLV");
    }

    private ValoresglobalesDTO createForPrefix(String prefix, ValoresglobalesDTO dto) {
        return createForPrefix(prefix, dto, null);
    }

    private ValoresglobalesDTO createForPrefix(String prefix, ValoresglobalesDTO dto, String requiredSuffix) {
        if (dto == null || dto.getClave() == null || dto.getValor() == null) {
            throw new DomainException(PagoErrorCode.VALOR_GLOBAL_FORMATO_INVALIDO, dto == null ? null : dto.getClave());
        }

        String incoming = dto.getClave().trim();
        // Reject if already contains suffix (year_version)
        if (GLOBAL_KEY_PATTERN.matcher(incoming).matches()) {
            throw new DomainException(PagoErrorCode.VALOR_GLOBAL_FORMATO_INVALIDO, incoming);
        }

        if (!prefix.equalsIgnoreCase(incoming)) {
            throw new DomainException(PagoErrorCode.VALOR_GLOBAL_FORMATO_INVALIDO, incoming);
        }

        String normalizedValue = normalizeValueWithSuffix(dto.getValor(), requiredSuffix);

        int year = LocalDate.now().getYear();
        List<ValoresglobalesDTO> existing = findAll();
        int maxVersion = existing.stream()
                .filter(item -> item.getClave() != null && item.getClave().startsWith(prefix + "_" + year + "_"))
                .mapToInt(item -> extractVersion(item.getClave(), prefix, year))
                .max()
                .orElse(0);

        int nextVersion = maxVersion + 1;
        String newKey = String.format("%s_%d_%d", prefix, year, nextVersion);

        ValoresglobalesDTO toCreate = ValoresglobalesDTO.builder()
                .clave(newKey)
                .valor(normalizedValue)
                .build();

        return valoresglobalesService.create(toCreate);
    }

    private String normalizeValueWithSuffix(String value, String requiredSuffix) {
        if (value == null || value.isBlank() || requiredSuffix == null || requiredSuffix.isBlank()) {
            return value;
        }

        String trimmedValue = value.trim();
        String upperValue = trimmedValue.toUpperCase();
        String upperSuffix = requiredSuffix.trim().toUpperCase();

        if (upperValue.endsWith(upperSuffix)) {
            return trimmedValue;
        }

        if (upperSuffix.equals("MB") && upperValue.matches("\\d+(\\.\\d+)?")) {
            return trimmedValue + "MB";
        }

        if (upperSuffix.equals("SMMLV") && upperValue.matches("\\d+(\\.\\d+)?")) {
            return trimmedValue + "SMMLV";
        }

        return trimmedValue;
    }

    private Integer extractVersionForAliases(String clave, String requestedPrefix, int year) {
        for (String candidatePrefix : resolvePrefixAliases(requestedPrefix)) {
            int version = extractVersion(clave, candidatePrefix, year);
            if (version > 0) {
                return version;
            }
        }
        return 0;
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            throw new DomainException(PagoErrorCode.VALOR_GLOBAL_FORMATO_INVALIDO, prefix);
        }
        return prefix.trim().toUpperCase();
    }
}
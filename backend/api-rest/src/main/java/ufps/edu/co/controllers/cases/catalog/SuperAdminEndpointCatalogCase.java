package ufps.edu.co.controllers.cases.catalog;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import ufps.edu.co.enums.Rol;

@RestController
@RequestMapping(value = "/super-admin/endpoints", produces = MediaType.APPLICATION_JSON_VALUE)
public class SuperAdminEndpointCatalogCase {

    private final SuperAdminEndpointCatalogService catalogService;

    public SuperAdminEndpointCatalogCase(SuperAdminEndpointCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public ResponseEntity<EndpointCatalogResponse> listAuthorizedEndpoints() {
        return ResponseEntity.ok(catalogService.buildCatalog());
    }

    @Service
    static class SuperAdminEndpointCatalogService {

        private static final String REST_PACKAGE = "ufps.edu.co.controllers.rest";
        private static final String CASES_PACKAGE = "ufps.edu.co.controllers.cases";
        private static final String AUTH_PACKAGE = "ufps.edu.co.controllers.auth";
        private static final String CATALOG_CONTROLLER = SuperAdminEndpointCatalogCase.class.getName();

        private final RequestMappingHandlerMapping handlerMapping;

        SuperAdminEndpointCatalogService(
                @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
            this.handlerMapping = handlerMapping;
        }

        EndpointCatalogResponse buildCatalog() {
            List<EndpointTemplate> endpoints = handlerMapping.getHandlerMethods().entrySet().stream()
                    .filter(entry -> isVisibleToSuperAdmin(entry.getValue()))
                    .map(entry -> toTemplate(entry.getKey(), entry.getValue()))
                    .sorted(Comparator
                            .comparing((EndpointTemplate endpoint) -> endpoint.path())
                            .thenComparing(endpoint -> String.join(",", endpoint.methods())))
                    .toList();

            return new EndpointCatalogResponse(
                    Rol.SUPER_ADMINISTRADOR.getCodigo(),
                    "Catálogo de endpoints autorizados para Super Administrador. El token Bearer se envía desde el frontend y no se incluye en las plantillas.",
                    endpoints.size(),
                    endpoints);
        }

        private Boolean isVisibleToSuperAdmin(HandlerMethod handlerMethod) {
            String className = handlerMethod.getBeanType().getName();
            if (CATALOG_CONTROLLER.equals(className) || className.startsWith(AUTH_PACKAGE)) {
                return false;
            }
            return className.startsWith(REST_PACKAGE) || className.startsWith(CASES_PACKAGE);
        }

        private EndpointTemplate toTemplate(RequestMappingInfo info, HandlerMethod handlerMethod) {
            return new EndpointTemplate(
                    firstPath(info),
                    methods(info),
                    values(info.getConsumesCondition().getConsumableMediaTypes()),
                    values(info.getProducesCondition().getProducibleMediaTypes()),
                    handlerMethod.getBeanType().getSimpleName(),
                    handlerMethod.getMethod().getName(),
                    bodyTemplate(handlerMethod),
                    requestParameters(handlerMethod, RequestParam.class, "query"),
                    requestParameters(handlerMethod, PathVariable.class, "path"));
        }

        private String firstPath(RequestMappingInfo info) {
            Set<String> pathPatterns = info.getPathPatternsCondition() == null
                    ? Set.of()
                    : info.getPathPatternsCondition().getPatternValues();
            if (!pathPatterns.isEmpty()) {
                return pathPatterns.stream().sorted().findFirst().orElse("");
            }
            return "";
        }

        private List<String> methods(RequestMappingInfo info) {
            Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
            if (!methods.isEmpty()) {
                return methods.stream().map(RequestMethod::name).sorted().toList();
            }
            return List.of("GET", "POST", "PUT", "PATCH", "DELETE");
        }

        private List<String> values(Set<?> values) {
            if (values == null || values.isEmpty()) {
                return List.of();
            }
            return values.stream()
                    .map(Object::toString)
                    .sorted()
                    .toList();
        }

        private RequestBodyTemplate bodyTemplate(HandlerMethod handlerMethod) {
            for (Parameter parameter : handlerMethod.getMethod().getParameters()) {
                RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
                if (requestBody != null) {
                    return new RequestBodyTemplate(
                            parameter.getType().getName(),
                            requestBody.required(),
                            buildExample(parameter.getType(), 0),
                            fields(parameter.getType(), 0));
                }
            }
            return null;
        }

        private List<ParameterTemplate> requestParameters(
                HandlerMethod handlerMethod,
                Class<? extends Annotation> annotationType,
                String source) {

            List<ParameterTemplate> parameters = new ArrayList<>();
            for (Parameter parameter : handlerMethod.getMethod().getParameters()) {
                Annotation annotation = parameter.getAnnotation(annotationType);
                if (annotation != null) {
                    parameters.add(new ParameterTemplate(
                            parameterName(parameter, annotation),
                            source,
                            parameter.getType().getSimpleName(),
                            required(annotation),
                            exampleValue(parameter.getType())));
                }
            }
            return parameters;
        }

        private String parameterName(Parameter parameter, Annotation annotation) {
            if (annotation instanceof RequestParam requestParam && !requestParam.name().isBlank()) {
                return requestParam.name();
            }
            if (annotation instanceof RequestParam requestParam && !requestParam.value().isBlank()) {
                return requestParam.value();
            }
            if (annotation instanceof PathVariable pathVariable && !pathVariable.name().isBlank()) {
                return pathVariable.name();
            }
            if (annotation instanceof PathVariable pathVariable && !pathVariable.value().isBlank()) {
                return pathVariable.value();
            }
            return parameter.getName();
        }

        private Boolean required(Annotation annotation) {
            if (annotation instanceof RequestParam requestParam) {
                return requestParam.required();
            }
            if (annotation instanceof PathVariable pathVariable) {
                return pathVariable.required();
            }
            return true;
        }

        private Object buildExample(Class<?> type, int depth) {
            if (depth > 4) {
                return null;
            }
            if (type.isRecord()) {
                Map<String, Object> template = new LinkedHashMap<>();
                for (RecordComponent component : type.getRecordComponents()) {
                    template.put(component.getName(), buildExample(component.getType(), depth + 1));
                }
                return template;
            }
            if (isSimple(type)) {
                return exampleValue(type);
            }
            if (List.class.isAssignableFrom(type) || Set.class.isAssignableFrom(type)) {
                return List.of();
            }
            if (Map.class.isAssignableFrom(type)) {
                return Map.of();
            }

            Map<String, Object> template = new LinkedHashMap<>();
            for (Field field : type.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic()) {
                    template.put(field.getName(), buildExample(field.getType(), depth + 1));
                }
            }
            return template;
        }

        private List<FieldTemplate> fields(Class<?> type, int depth) {
            if (depth > 4) {
                return List.of();
            }
            if (type.isRecord()) {
                List<FieldTemplate> fields = new ArrayList<>();
                for (RecordComponent component : type.getRecordComponents()) {
                    fields.add(new FieldTemplate(
                            component.getName(),
                            readableType(component.getGenericType()),
                            isRequired(component.getAnnotations()),
                            buildExample(component.getType(), depth + 1),
                            fields(component.getType(), depth + 1)));
                }
                return fields;
            }
            if (isSimple(type)) {
                return List.of();
            }

            List<FieldTemplate> fields = new ArrayList<>();
            for (Field field : type.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic()) {
                    fields.add(new FieldTemplate(
                            field.getName(),
                            readableType(field.getGenericType()),
                            isRequired(field.getAnnotations()),
                            buildExample(field.getType(), depth + 1),
                            fields(field.getType(), depth + 1)));
                }
            }
            return fields;
        }

        private Boolean isRequired(Annotation[] annotations) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof NotNull || annotation instanceof NotBlank || annotation instanceof NotEmpty) {
                    return true;
                }
            }
            return false;
        }

        private Boolean isSimple(Class<?> type) {
            return type.isPrimitive()
                    || type.isEnum()
                    || String.class.equals(type)
                    || Number.class.isAssignableFrom(type)
                    || Boolean.class.equals(type)
                    || Character.class.equals(type)
                    || BigInteger.class.equals(type)
                    || BigDecimal.class.equals(type)
                    || LocalDate.class.equals(type)
                    || LocalDateTime.class.equals(type)
                    || OffsetDateTime.class.equals(type);
        }

        private Object exampleValue(Class<?> type) {
            if (String.class.equals(type) || Character.class.equals(type)) {
                return "";
            }
            if (Integer.class.equals(type) || int.class.equals(type)
                    || Long.class.equals(type) || long.class.equals(type)
                    || Short.class.equals(type) || short.class.equals(type)
                    || Byte.class.equals(type) || byte.class.equals(type)
                    || BigInteger.class.equals(type)) {
                return 0;
            }
            if (Double.class.equals(type) || double.class.equals(type)
                    || Float.class.equals(type) || float.class.equals(type)
                    || BigDecimal.class.equals(type)) {
                return 0.0;
            }
            if (Boolean.class.equals(type) || boolean.class.equals(type)) {
                return false;
            }
            if (LocalDate.class.equals(type)) {
                return "2026-01-01";
            }
            if (LocalDateTime.class.equals(type) || OffsetDateTime.class.equals(type)) {
                return "2026-01-01T00:00:00";
            }
            if (type.isEnum()) {
                Object[] constants = type.getEnumConstants();
                return constants.length == 0 ? "" : constants[0].toString();
            }
            return null;
        }

        private String readableType(Type type) {
            return type.getTypeName();
        }
    }

    public record EndpointCatalogResponse(
            String role,
            String description,
            int total,
            List<EndpointTemplate> endpoints) {
    }

    public record EndpointTemplate(
            String path,
            List<String> methods,
            List<String> consumes,
            List<String> produces,
            String controller,
            String handler,
            RequestBodyTemplate requestBody,
            List<ParameterTemplate> queryParameters,
            List<ParameterTemplate> pathVariables) {
    }

    public record RequestBodyTemplate(
            String type,
            boolean required,
            Object template,
            List<FieldTemplate> fields) {
    }

    public record FieldTemplate(
            String name,
            String type,
            boolean required,
            Object example,
            List<FieldTemplate> fields) {
    }

    public record ParameterTemplate(
            String name,
            String source,
            String type,
            boolean required,
            Object example) {
    }
}

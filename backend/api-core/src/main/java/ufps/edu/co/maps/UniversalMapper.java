package ufps.edu.co.maps;

import java.beans.Introspector;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ufps.edu.co.domain.annotations.UniversalMapping;
import ufps.edu.co.records.InputRequest;
import ufps.edu.co.records.OutputResponse;
import ufps.edu.co.records.contracts.CreateType;
import ufps.edu.co.records.contracts.DeleteType;
import ufps.edu.co.records.contracts.FindType;
import ufps.edu.co.records.contracts.PatchType;
import ufps.edu.co.records.contracts.UpdateType;

public abstract class UniversalMapper<O extends OutputResponse, DTO> {

    private final Class<? extends CreateType> createClass;
    private final Class<? extends UpdateType> updateClass;
    private final Class<? extends DeleteType> deleteClass;
    private final Class<? extends PatchType> patchClass;
    private final Class<? extends FindType> findClass;
    private final Class<?> dtoClass;
    private final SetterIndex setterIndex;

    protected UniversalMapper() {
        UniversalMapping mapping = getClass().getAnnotation(UniversalMapping.class);
        if (mapping == null) {
            throw new IllegalStateException(
                    "Missing @UniversalMapping on " + getClass().getSimpleName());
        }
        this.createClass = mapping.create();
        this.updateClass = mapping.update();
        this.deleteClass = mapping.delete();
        this.patchClass = mapping.patch();
        this.findClass = mapping.find();
        this.dtoClass = resolveDtoClass();
        if (this.dtoClass == null) {
            throw new IllegalStateException(
                    "Cannot resolve DTO type for " + getClass().getSimpleName());
        }
        this.setterIndex = SetterIndex.from(dtoClass);
    }

    public final DTO toDto(InputRequest input) {
        if (input == null) {
            return null;
        }
        if (createClass.isInstance(input)
                || updateClass.isInstance(input)
                || deleteClass.isInstance(input)
                || patchClass.isInstance(input)
                || findClass.isInstance(input)) {
            return autoMap(input);
        }
        throw new IllegalArgumentException(
                "Unsupported input type for " + getClass().getSimpleName() + ": " + input.getClass().getName());
    }

    private DTO autoMap(Object input) {
        DTO dto = newDtoInstance();
        int mappedFields = mapInputToDto(input, dto);
        return mappedFields == 0 ? null : dto;
    }

    @SuppressWarnings("unchecked")
    private DTO newDtoInstance() {
        try {
            Constructor<?> constructor = dtoClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (DTO) constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "DTO type " + dtoClass.getSimpleName() + " must have a no-arg constructor",
                    e);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(
                    "Error creating DTO instance for " + dtoClass.getSimpleName(), e);
        }
    }

    private Integer mapInputToDto(Object input, DTO dto) {
        if (input.getClass().isRecord()) {
            return mapRecord(input, dto);
        }
        return mapBean(input, dto);
    }

    private Integer mapRecord(Object input, DTO dto) {
        int mapped = 0;
        RecordComponent[] components = input.getClass().getRecordComponents();
        for (RecordComponent component : components) {
            Object value = readRecordComponent(input, component);
            mapped += applyValue(component.getName(), value, dto);
        }
        return mapped;
    }

    private Object readRecordComponent(Object input, RecordComponent component) {
        try {
            return component.getAccessor().invoke(input);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(
                    "Error reading input component " + component.getName() + " for "
                            + getClass().getSimpleName(),
                    e);
        }
    }

    private Integer mapBean(Object input, DTO dto) {
        int mapped = 0;
        for (Method method : input.getClass().getMethods()) {
            if (method.getParameterCount() != 0) {
                continue;
            }
            String name = method.getName();
            if ("getClass".equals(name)) {
                continue;
            }
            String propertyName = null;
            if (name.startsWith("get") && name.length() > 3) {
                propertyName = Introspector.decapitalize(name.substring(3));
            } else if (name.startsWith("is")
                    && name.length() > 2
                    && (method.getReturnType() == boolean.class
                            || method.getReturnType() == Boolean.class)) {
                propertyName = Introspector.decapitalize(name.substring(2));
            }
            if (propertyName == null) {
                continue;
            }
            Object value = readGetterValue(input, method, propertyName);
            mapped += applyValue(propertyName, value, dto);
        }
        return mapped;
    }

    private Object readGetterValue(Object input, Method method, String propertyName) {
        try {
            return method.invoke(input);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(
                    "Error reading input property " + propertyName + " for "
                            + getClass().getSimpleName(),
                    e);
        }
    }

    private Integer applyValue(String propertyName, Object value, DTO dto) {
        if (value == null) {
            return 0;
        }
        Method setter = setterIndex.find(propertyName);
        if (setter == null) {
            return 0;
        }
        if (!isCompatible(setter.getParameterTypes()[0], value.getClass())) {
            return 0;
        }
        try {
            setter.invoke(dto, value);
            return 1;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(
                    "Error mapping property " + propertyName + " in " + getClass().getSimpleName(),
                    e);
        }
    }

    private Boolean isCompatible(Class<?> parameterType, Class<?> valueType) {
        if (parameterType.isPrimitive()) {
            parameterType = primitiveToWrapper(parameterType);
        }
        return parameterType.isAssignableFrom(valueType);
    }

    private Class<?> primitiveToWrapper(Class<?> type) {
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return type;
    }

    private Class<?> resolveDtoClass() {
        Class<?> current = getClass();
        while (current != null) {
            Type superType = current.getGenericSuperclass();
            if (superType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) superType;
                Type rawType = parameterizedType.getRawType();
                if (rawType instanceof Class && UniversalMapper.class.isAssignableFrom((Class<?>) rawType)) {
                    return toClass(parameterizedType.getActualTypeArguments()[1]);
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private Class<?> toClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        return null;
    }

    private static String normalize(String name) {
        StringBuilder builder = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (ch == '_') {
                continue;
            }
            builder.append(Character.toLowerCase(ch));
        }
        return builder.toString();
    }

    private static final class SetterIndex {

        private final Map<String, Method> exact;
        private final Map<String, Method> normalized;
        private final Set<String> ambiguousNormalized;

        private SetterIndex(Map<String, Method> exact, Map<String, Method> normalized,
                Set<String> ambiguousNormalized) {
            this.exact = exact;
            this.normalized = normalized;
            this.ambiguousNormalized = ambiguousNormalized;
        }

        static SetterIndex from(Class<?> dtoClass) {
            Map<String, Method> exact = new HashMap<>();
            Map<String, Method> normalized = new HashMap<>();
            Set<String> ambiguousNormalized = new HashSet<>();
            for (Method method : dtoClass.getMethods()) {
                if (!method.getName().startsWith("set") || method.getParameterCount() != 1) {
                    continue;
                }
                String propertyName = Introspector.decapitalize(method.getName().substring(3));
                method.setAccessible(true);
                exact.put(propertyName, method);

                String normalizedName = normalize(propertyName);
                Method existing = normalized.get(normalizedName);
                if (existing != null && !existing.equals(method)) {
                    ambiguousNormalized.add(normalizedName);
                } else if (!ambiguousNormalized.contains(normalizedName)) {
                    normalized.put(normalizedName, method);
                }
            }
            return new SetterIndex(exact, normalized, ambiguousNormalized);
        }

        Method find(String propertyName) {
            Method exactMatch = exact.get(propertyName);
            if (exactMatch != null) {
                return exactMatch;
            }
            String normalizedName = normalize(propertyName);
            if (ambiguousNormalized.contains(normalizedName)) {
                return null;
            }
            return normalized.get(normalizedName);
        }
    }

    public abstract O toOutput(DTO dto);
}

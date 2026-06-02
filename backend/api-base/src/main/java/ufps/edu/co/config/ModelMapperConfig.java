package ufps.edu.co.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.modelmapper.Condition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ufps.edu.co.persistence.entities.*;
import ufps.edu.co.rest.dto.*;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
            mapper.getConfiguration().setAmbiguityIgnored(true);

        // Global condition: when mapping originates from a JPA entity (persistence.entities package),
        // avoid mapping collection properties (OneToMany) to prevent infinite recursion cycles
        // caused by bidirectional relationships (entity -> dto -> collection -> entity ...).
        Condition<Object, Object> skipCollectionsFromEntities = new Condition<Object, Object>() {
            @Override
            public boolean applies(MappingContext<Object, Object> context) {
                // If there's no source, nothing to map
                if (context.getSource() == null) {
                    return false;
                }
                // Find root source type
                MappingContext<?, ?> root = context;
                while (root.getParent() != null) {
                    root = root.getParent();
                }
                Class<?> rootSourceType = root.getSourceType();
                if (rootSourceType != null) {
                    Package pkg = rootSourceType.getPackage();
                    if (pkg != null && pkg.getName().contains("persistence.entities")) {
                        // If destination is a collection, skip mapping
                        Class<?> destType = context.getDestinationType();
                        if (destType != null && java.util.Collection.class.isAssignableFrom(destType)) {
                            return false;
                        }
                        // If source itself is a collection, skip mapping
                        if (context.getSource() instanceof java.util.Collection) {
                            return false;
                        }
                    }
                }
                return true;
            }
        };

        mapper.getConfiguration().setPropertyCondition(skipCollectionsFromEntities);

        // Persona ↔ Aspirante / Administrativo / Usuario
        mapper.createTypeMap(PersonaEntity.class, PersonaDTO.class)
              .addMappings(m -> {
                  m.skip(PersonaDTO::setAspiranteList);
                  m.skip(PersonaDTO::setAdministrativoList);
                  m.skip(PersonaDTO::setUsuarioList);
              });

        // Persona DTO -> Entity (avoid ambiguous ubicacion and deep graphs)
            mapper.emptyTypeMap(PersonaDTO.class, PersonaEntity.class)
              .addMappings(m -> {
                  m.skip(PersonaEntity::setAdministrativoList);
                  m.skip(PersonaEntity::setAspiranteList);
                  m.skip(PersonaEntity::setUsuarioList);
                  m.skip(PersonaEntity::setGenero);
                  m.skip(PersonaEntity::setUbicacion);
                  m.skip(PersonaEntity::setGrupoetnico);
                  m.skip(PersonaEntity::setPoblacionindigena);
                  m.skip(PersonaEntity::setDiscapacidad);
                  m.skip(PersonaEntity::setDocumentopersona);
                  m.skip(PersonaEntity::setCapacidadexepcional);
                  m.skip(PersonaEntity::setEstadocivil);
                    })
                    .implicitMappings();

        // Cohorte ↔ Aspirante / ListaAdmitidos / Prueba / Criterio
        mapper.createTypeMap(CohorteEntity.class, CohorteDTO.class)
              .addMappings(m -> {
                  m.skip(CohorteDTO::setAspiranteList);
                  m.skip(CohorteDTO::setAdmitidoList);
                  m.skip(CohorteDTO::setCriterioevaluacionList);
                  m.skip(CohorteDTO::setPruebaList);
              });

        // Cohorte DTO -> Entity (avoid deep mapping of relations)
            mapper.emptyTypeMap(CohorteDTO.class, CohorteEntity.class)
              .addMappings(m -> {
                  m.skip(CohorteEntity::setAdmitidoList);
                  m.skip(CohorteEntity::setAspiranteList);
                  m.skip(CohorteEntity::setEstado);
                  m.skip(CohorteEntity::setSemestre);
                  m.skip(CohorteEntity::setModalidad);
                  m.skip(CohorteEntity::setPlazo);
                  m.skip(CohorteEntity::setPlazo2);
                  m.skip(CohorteEntity::setPlazo3);
                  m.skip(CohorteEntity::setPrograma);
                  m.skip(CohorteEntity::setPruebaList);
                    })
                    .implicitMappings();

        // Estado → listas bidireccionales (evita referencias circulares)
        mapper.createTypeMap(EstadoEntity.class, EstadoDTO.class)
              .addMappings(m -> {
                  m.skip(EstadoDTO::setAdministrativoList);
                  m.skip(EstadoDTO::setAspiranteList);
                  m.skip(EstadoDTO::setCohorteList);
                  m.skip(EstadoDTO::setEntrevistaList);
                  m.skip(EstadoDTO::setPagoList);
                  m.skip(EstadoDTO::setSemestreList);
              });

        // Aspirante → listas bidireccionales
        mapper.createTypeMap(AspiranteEntity.class, AspiranteDTO.class)
              .addMappings(m -> {
                  m.skip(AspiranteDTO::setCalificacioncriterioList);
                  m.skip(AspiranteDTO::setDocumentoList);
                  m.skip(AspiranteDTO::setEntrevistaList);
                  m.skip(AspiranteDTO::setAdmitidoList);
                  m.skip(AspiranteDTO::setPagoList);
              });

        // Tipovinculacion → evita referencia circular con aspiranteList
        mapper.createTypeMap(TipovinculacionEntity.class, TipovinculacionDTO.class)
              .addMappings(m -> m.skip(TipovinculacionDTO::setAspiranteList));

        // Administrativo ↔ Documento
        mapper.createTypeMap(AdministrativoEntity.class, AdministrativoDTO.class)
              .addMappings(m -> m.skip(AdministrativoDTO::setDocumentoList));

        // Administrativo DTO -> Entity (avoid deep persona mapping)
        mapper.emptyTypeMap(AdministrativoDTO.class, AdministrativoEntity.class)
              .addMappings(m -> {
                  m.skip(AdministrativoEntity::setDocumentoList);
                  m.skip(AdministrativoEntity::setCargo);
                  m.skip(AdministrativoEntity::setEstado);
                  m.skip(AdministrativoEntity::setPersona);
              })
              .implicitMappings();

        // Ubicacion ↔ Entrevista
        mapper.createTypeMap(UbicacionEntity.class, UbicacionDTO.class)
              .addMappings(m -> m.skip(UbicacionDTO::setEntrevistaList));

        // Ubicacion DTO -> Entity (avoid mapping list relations)
        mapper.emptyTypeMap(UbicacionDTO.class, UbicacionEntity.class)
              .addMappings(m -> m.skip(UbicacionEntity::setMunicipio))
              .implicitMappings();

        // Semestre DTO -> Entity (avoid mapping cohorte list)
        mapper.emptyTypeMap(SemestreDTO.class, SemestreEntity.class)
              .addMappings(m -> {
                  m.skip(SemestreEntity::setCohorteList);
                  m.skip(SemestreEntity::setEstado);
              })
              .implicitMappings();

        return mapper;
    }
}

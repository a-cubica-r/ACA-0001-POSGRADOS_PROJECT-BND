package ufps.edu.co.rest.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ufps.edu.co.persistence.entities.UltimocodigoprogramaEntity;
import ufps.edu.co.persistence.repositories.UltimocodigoprogramaRepository;
import ufps.edu.co.rest.dto.UltimocodigoprogramaDTO;
import ufps.edu.co.rest.exceptions.RegistroNoEncontradoException;
import ufps.edu.co.rest.services.commons.GenericService;

/**
 * Acceso a los consecutivos del código de estudiante por programa.
 *
 * <p>El consecutivo lo reserva la generación del código; la consulta de la
 * pantalla de «Últimos códigos» no bloquea nada.</p>
 */
@Service
@Transactional
public class UltimocodigoprogramaService
        extends GenericService<UltimocodigoprogramaEntity, UltimocodigoprogramaDTO> {

    @Autowired
    private UltimocodigoprogramaRepository repository;

    public UltimocodigoprogramaService() {
        super(UltimocodigoprogramaEntity.class, UltimocodigoprogramaDTO.class);
    }

    /** Una entrada por programa, aunque todavía no tenga fila de consecutivo. */
    @Transactional(readOnly = true)
    public List<UltimocodigoprogramaDTO> findAllConNombrePrograma() {
        return repository.findAllConNombrePrograma().stream()
                .map(row -> UltimocodigoprogramaDTO.builder()
                        .id((Integer) row[0])
                        .idPrograma((Integer) row[1])
                        .codigo((Integer) row[2])
                        .nombrePrograma((String) row[3])
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public UltimocodigoprogramaDTO findById(Integer id) {
        return entityToDto(repository.findById(id));
    }

    @Transactional(readOnly = true)
    public UltimocodigoprogramaDTO findByIdPrograma(Integer idPrograma) {
        return entityToDto(repository.findByIdPrograma(idPrograma));
    }

    /**
     * Versión con bloqueo de fila. Solo para usar dentro de una transacción de
     * escritura (reserva del consecutivo y edición manual).
     */
    public UltimocodigoprogramaDTO findByIdProgramaForUpdate(Integer idPrograma) {
        return entityToDto(repository.findByIdProgramaForUpdate(idPrograma));
    }

    @Transactional(readOnly = true)
    public boolean existsByIdPrograma(Integer idPrograma) {
        return repository.existsByIdPrograma(idPrograma);
    }

    public UltimocodigoprogramaDTO create(UltimocodigoprogramaDTO dto) {
        return entityToDto(repository.save(dtoToEntity(dto)));
    }

    public UltimocodigoprogramaDTO update(Integer id, UltimocodigoprogramaDTO dto) {
        UltimocodigoprogramaEntity entity = repository.findById(id)
                .orElseThrow(() -> new RegistroNoEncontradoException(
                        "Consecutivo no encontrado con id: " + id));
        // Se actualiza la entidad administrada en vez de guardar una copia: así
        // la relación con el programa se conserva y la respuesta trae su nombre.
        if (dto.getCodigo() != null) {
            entity.setCodigo(dto.getCodigo());
        }
        if (dto.getIdPrograma() != null) {
            entity.setIdPrograma(dto.getIdPrograma());
        }
        return entityToDto(entity);
    }

    /** Borra la fila del programa. No falla si el programa no tiene consecutivo. */
    public void deleteByIdPrograma(Integer idPrograma) {
        repository.findByIdPrograma(idPrograma).ifPresent(repository::delete);
    }
}

package ufps.edu.co.rest.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ufps.edu.co.persistence.entities.EstudiantesEntity;
import ufps.edu.co.persistence.repositories.EstudiantesRepository;
import ufps.edu.co.rest.dto.EstudiantesDTO;
import ufps.edu.co.rest.services.commons.GenericService;

@Service
@Transactional
public class EstudiantesService extends GenericService<EstudiantesEntity, EstudiantesDTO> {

    @Autowired
    private EstudiantesRepository repository;

    public EstudiantesService() {
        super(EstudiantesEntity.class, EstudiantesDTO.class);
    }

    @Transactional(readOnly = true)
    public List<EstudiantesDTO> findAll() {
        return entityListToDtoList(repository.findAll());
    }

    @Transactional(readOnly = true)
    public EstudiantesDTO findById(Integer id) {
        return entityToDto(repository.findById(id));
    }

    @Transactional(readOnly = true)
    public List<EstudiantesDTO> findByProgramaId(Integer programaId) {
        return entityListToDtoList(repository.findByProgramaId(programaId));
    }

    @Transactional(readOnly = true)
    public List<EstudiantesDTO> findByCohorteId(Integer cohorteId) {
        return entityListToDtoList(repository.findByCohorteId(cohorteId));
    }

    @Transactional(readOnly = true)
    public List<EstudiantesDTO> findByProgramaIdAndCohorteId(Integer programaId, Integer cohorteId) {
        return entityListToDtoList(repository.findByProgramaIdAndCohorteId(programaId, cohorteId));
    }

    public EstudiantesDTO create(EstudiantesDTO dto) {
        return entityToDto(repository.save(dtoToEntity(dto)));
    }

    public EstudiantesDTO update(Integer id, EstudiantesDTO dto) {
        repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con id: " + id));
        dto.setId(id);
        return entityToDto(repository.save(dtoToEntity(dto)));
    }

    public void deleteById(Integer id) {
        repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con id: " + id));
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByCedula(String cedula) {
        return repository.existsByCedula(cedula);
    }

    @Transactional(readOnly = true)
    public boolean existsByCedulaAndIdNot(String cedula, Integer id) {
        return repository.existsByCedulaAndIdNot(cedula, id);
    }

    @Transactional
    public String findUltimoCodigoPorPrograma(Integer programaId) {
        return repository.findFirstByProgramaIdOrderByCodigoDesc(programaId)
                .map(EstudiantesEntity::getCodigo)
                .orElse(null);
    }
}

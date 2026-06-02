package ufps.edu.co.processor.events;

public class AspiranteLegalizadoEvent {

    private final Integer idAspirante;

    public AspiranteLegalizadoEvent(Integer idAspirante) {
        this.idAspirante = idAspirante;
    }

    public Integer getIdAspirante() {
        return idAspirante;
    }
}

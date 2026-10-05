package ufps.edu.co.services.core.events;

public class AspiranteLegalizadoEvent {

    private final Integer idAspirante;

    public AspiranteLegalizadoEvent(Integer idAspirante) {
        this.idAspirante = idAspirante;
    }

    public Integer getIdAspirante() {
        return idAspirante;
    }
}

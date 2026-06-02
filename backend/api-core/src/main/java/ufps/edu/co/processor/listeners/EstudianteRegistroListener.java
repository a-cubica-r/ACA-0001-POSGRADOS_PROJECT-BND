package ufps.edu.co.processor.listeners;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ufps.edu.co.processor.crud.EstudiantesProcessor;
import ufps.edu.co.processor.events.AspiranteLegalizadoEvent;

@Component
public class EstudianteRegistroListener {

    private static final Logger log = LoggerFactory.getLogger(EstudianteRegistroListener.class);

    @Autowired
    private EstudiantesProcessor estudiantesProcessor;

    @EventListener
    public void onAspiranteLegalizado(AspiranteLegalizadoEvent event) {
        log.info("Evento AspiranteLegalizado recibido para aspirante {}", event.getIdAspirante());
        estudiantesProcessor.registrarAspiranteComoEstudiante(event.getIdAspirante());
    }
}

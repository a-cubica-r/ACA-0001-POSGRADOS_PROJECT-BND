package ufps.edu.co.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

@Component
public class EmailTemplates {

  // ─── Asuntos ─────────────────────────────────────────────────────────────────

  public static final String ASUNTO_INSCRIPCION = "Confirmación de inscripción - Posgrados UFPS";

  public static final String ASUNTO_SUBIDA_DOCUMENTO = "Documento cargado exitosamente - Posgrados UFPS";

  public static final String ASUNTO_APROBACION_DOCUMENTO = "Documento aprobado - Posgrados UFPS";

  public static final String ASUNTO_RECHAZO_DOCUMENTO = "Documento rechazado - Posgrados UFPS";

  public static final String ASUNTO_AGENDAR_ENTREVISTA = "Entrevista agendada - Posgrados UFPS";

  public static final String ASUNTO_REAGENDAR_ENTREVISTA = "Entrevista reagendada - Posgrados UFPS";

  public static final String ASUNTO_AGENDAR_PRUEBA = "Prueba agendada - Posgrados UFPS";

  public static final String ASUNTO_REAGENDAR_PRUEBA = "Prueba reagendada - Posgrados UFPS";

  public static final String ASUNTO_CALIFICACION_CRITERIO = "Calificación de criterio registrada - Posgrados UFPS";

  public static final String ASUNTO_ADMITIDO = "¡Felicitaciones! Has sido admitido - Posgrados UFPS";

  public static final String ASUNTO_RECHAZADO = "Resultado del proceso de admisión - Posgrados UFPS";

  // ─── Cuerpos HTML ────────────────────────────────────────────────────────────

  /**
   * Correo de confirmación de inscripción.
   * Datos disponibles en InscripcionCase.registrarFormulario:
   * body.nombres(), body.apellidos(), body.tituloPregrado(),
   * cohorteService.findById(body.idCohorte()).getNombre()
   */
  public static String cuerpoInscripcion(
      String nombres,
      String apellidos,
      String nombreCohorte,
      String programaPosgrado) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          <h2 style="color: #1a5276;">Confirmación de inscripción</h2>
          <p>Estimado/a <strong>%s %s</strong>,</p>
          <p>Su inscripción al programa de posgrados ha sido registrada exitosamente
             en la cohorte <strong>%s</strong>.</p>
          <table style="border-collapse: collapse; width: 100%%;">
            <tr>
              <td style="padding: 6px 12px; font-weight: bold;">Programa de posgrado:</td>
              <td style="padding: 6px 12px;">%s</td>
            </tr>
          </table>
          <p>En los próximos días recibirá instrucciones para continuar con el proceso de admisión.</p>
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(nombres, apellidos, nombreCohorte, programaPosgrado);
  }

  /**
   * Correo de confirmación de carga de documento.
   * Datos disponibles en AspiranteCase.subirDocumentoRequerido:
   * idAspirante (se resuelve a nombre del aspirante en el servicio),
   * file.getOriginalFilename() o nombre del requisito, LocalDate.now()
   */
  public static String cuerpoSubidaDocumento(
      String nombreAspirante,
      String nombreDocumento,
      LocalDate fechaCargue) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          <h2 style="color: #1a5276;">Documento cargado exitosamente</h2>
          <p>Estimado/a <strong>%s</strong>,</p>
          <p>El documento <strong>%s</strong> fue cargado exitosamente el <strong>%s</strong>.</p>
          <p>Su documento está pendiente de revisión por parte del director de programa.
             Le notificaremos cuando haya sido evaluado.</p>
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(nombreAspirante, nombreDocumento, fechaCargue);
  }

  /**
   * Correo de aprobación de documento.
   * Datos disponibles en DocumentoProcessor.approveDocument, retorna
   * AprobarDocumentoOutput:
   * output.nombre() es el nombre del documento, el aspirante se resuelve antes de
   * enviar.
   */
  public static String cuerpoAprobacionDocumento(
      String nombreAspirante,
      String nombreDocumento) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          <h2 style="color: #1a7a4a;">Documento aprobado</h2>
          <p>Estimado/a <strong>%s</strong>,</p>
          <p>Nos complace informarle que el documento <strong>%s</strong> ha sido
             <strong style="color: #1a7a4a;">aprobado</strong> por el director de programa.</p>
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(nombreAspirante, nombreDocumento);
  }

  /**
   * Correo de rechazo de documento.
   * Datos disponibles en DirectorProgramaCase.rechazarDocumento:
   * motivoRechazo viene del body, DocumentoEstadoOutput.nombre() es el nombre del
   * documento,
   * el aspirante se resuelve antes de enviar.
   */
  public static String cuerpoRechazoDocumento(
      String nombreAspirante,
      String nombreDocumento,
      String motivoRechazo) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          <h2 style="color: #a93226;">Documento rechazado</h2>
          <p>Estimado/a <strong>%s</strong>,</p>
          <p>Lamentamos informarle que el documento <strong>%s</strong> ha sido
             <strong style="color: #a93226;">rechazado</strong> por el director de programa.</p>
          <table style="border-collapse: collapse; width: 100%%; border: 1px solid #e74c3c; border-radius: 4px;">
            <tr style="background-color: #fdf2f0;">
              <td style="padding: 10px 14px; font-weight: bold; color: #a93226;">Motivo del rechazo:</td>
              <td style="padding: 10px 14px;">%s</td>
            </tr>
          </table>
          <p>Por favor, corrija el documento y vuelva a cargarlo a través del sistema.</p>
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(nombreAspirante, nombreDocumento, motivoRechazo);
  }

  /**
   * Correo de agendamiento de entrevista.
   * Datos disponibles en DirectorProgramaCase.scheduleInterview:
   * ENTREVISTA_SCHEDULE_REQUEST: fecha, tiempo, idTipoentrevista, ubicacion.
   * EntrevistaResumenOutput: tipoentrevista, fecha, tiempo, ubicacion.
   */
  public static String cuerpoAgendarEntrevista(
      String nombreAspirante,
      LocalDate fecha,
      LocalTime hora,
      String tipoEntrevista,
      String ubicacion) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          <h2 style="color: #1a5276;">Entrevista agendada</h2>
          <p>Estimado/a <strong>%s</strong>,</p>
          <p>Se ha agendado una entrevista de tipo <strong>%s</strong> con los siguientes detalles:</p>
          <table style="border-collapse: collapse; width: 100%%;">
            <tr>
              <td style="padding: 6px 12px; font-weight: bold;">Fecha:</td>
              <td style="padding: 6px 12px;">%s</td>
            </tr>
            <tr style="background-color: #f4f6f7;">
              <td style="padding: 6px 12px; font-weight: bold;">Hora:</td>
              <td style="padding: 6px 12px;">%s</td>
            </tr>
            <tr>
              <td style="padding: 6px 12px; font-weight: bold;">Lugar:</td>
              <td style="padding: 6px 12px;">%s</td>
            </tr>
          </table>
          <p>Por favor, confirme su asistencia a través del sistema.</p>
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(nombreAspirante, tipoEntrevista, fecha, hora, ubicacion);
  }

  /**
   * Correo de reagendamiento de entrevista.
   * Datos disponibles en DirectorProgramaCase.rescheduleInterview:
   * ENTREVISTA_REAGENDAR_REQUEST: fecha, tiempo, idTipoentrevista, ubicacion.
   * EntrevistaResumenOutput: tipoentrevista, fecha, tiempo, ubicacion.
   */
  public static String cuerpoReagendarEntrevista(
      String nombreAspirante,
      LocalDate fecha,
      LocalTime hora,
      String tipoEntrevista,
      String ubicacion) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          <h2 style="color: #1a5276;">Entrevista reagendada</h2>
          <p>Estimado/a <strong>%s</strong>,</p>
          <p>Su entrevista de tipo <strong>%s</strong> ha sido reagendada con los siguientes nuevos detalles:</p>
          <table style="border-collapse: collapse; width: 100%%;">
            <tr>
              <td style="padding: 6px 12px; font-weight: bold;">Nueva fecha:</td>
              <td style="padding: 6px 12px;">%s</td>
            </tr>
            <tr style="background-color: #f4f6f7;">
              <td style="padding: 6px 12px; font-weight: bold;">Nueva hora:</td>
              <td style="padding: 6px 12px;">%s</td>
            </tr>
            <tr>
              <td style="padding: 6px 12px; font-weight: bold;">Lugar:</td>
              <td style="padding: 6px 12px;">%s</td>
            </tr>
          </table>
          <p>Por favor, confirme su disponibilidad a través del sistema.</p>
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(nombreAspirante, tipoEntrevista, fecha, hora, ubicacion);
  }

  /**
   * Correo de agendamiento de prueba.
   * Datos disponibles en DirectorProgramaCase.crearPrueba:
   * PRUEBA_CREAR_REQUEST: nombre, descripcion, fecha, tiempo, idTipoprueba,
   * ubicacion.
   * PruebaResumenOutput: nombre, tipoprueba, fecha, tiempo, ubicacion.
   */
  public static String cuerpoAgendarPrueba(
      String nombreAspirante,
      String nombrePrueba,
      LocalDate fecha,
      LocalTime hora,
      String tipoPrueba,
      String ubicacion) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          <h2 style="color: #1a5276;">Prueba agendada</h2>
          <p>Estimado/a <strong>%s</strong>,</p>
          <p>Se ha agendado una prueba con los siguientes detalles:</p>
          <table style="border-collapse: collapse; width: 100%%;">
            <tr>
              <td style="padding: 6px 12px; font-weight: bold;">Nombre:</td>
              <td style="padding: 6px 12px;">%s</td>
            </tr>
            <tr style="background-color: #f4f6f7;">
              <td style="padding: 6px 12px; font-weight: bold;">Tipo:</td>
              <td style="padding: 6px 12px;">%s</td>
            </tr>
            <tr>
              <td style="padding: 6px 12px; font-weight: bold;">Fecha:</td>
              <td style="padding: 6px 12px;">%s</td>
            </tr>
            <tr style="background-color: #f4f6f7;">
              <td style="padding: 6px 12px; font-weight: bold;">Hora:</td>
              <td style="padding: 6px 12px;">%s</td>
            </tr>
            <tr>
              <td style="padding: 6px 12px; font-weight: bold;">Lugar:</td>
              <td style="padding: 6px 12px;">%s</td>
            </tr>
          </table>
          <p>Por favor, confirme su asistencia a través del sistema.</p>
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(nombreAspirante, nombrePrueba, tipoPrueba, fecha, hora, ubicacion);
  }

  /**
   * Correo de reagendamiento de prueba.
   * Datos disponibles en DirectorProgramaCase.reagendarPrueba:
   * PRUEBA_REAGENDAR_REQUEST: fecha, tiempo, idTipoprueba, ubicacion.
   * PruebaResumenOutput: nombre, tipoprueba, fecha, tiempo, ubicacion.
   */
  public static String cuerpoReagendarPrueba(
      String nombreAspirante,
      String nombrePrueba,
      LocalDate fecha,
      LocalTime hora,
      String tipoPrueba,
      String ubicacion) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          <h2 style="color: #1a5276;">Prueba reagendada</h2>
          <p>Estimado/a <strong>%s</strong>,</p>
          <p>La prueba <strong>%s</strong> de tipo <strong>%s</strong> ha sido reagendada
             con los siguientes nuevos detalles:</p>
          <table style="border-collapse: collapse; width: 100%%;">
            <tr>
              <td style="padding: 6px 12px; font-weight: bold;">Nueva fecha:</td>
              <td style="padding: 6px 12px;">%s</td>
            </tr>
            <tr style="background-color: #f4f6f7;">
              <td style="padding: 6px 12px; font-weight: bold;">Nueva hora:</td>
              <td style="padding: 6px 12px;">%s</td>
            </tr>
            <tr>
              <td style="padding: 6px 12px; font-weight: bold;">Lugar:</td>
              <td style="padding: 6px 12px;">%s</td>
            </tr>
          </table>
          <p>Por favor, confirme su disponibilidad a través del sistema.</p>
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(nombreAspirante, nombrePrueba, tipoPrueba, fecha, hora, ubicacion);
  }

  /**
   * Correo de notificación de calificación de criterio.
   * Datos disponibles en DirectorProgramaCase.calificarCriterio, retorna
   * CalificacionCriterioSimpleOutput:
   * output.nombreCriterio(), output.puntajeObtenido(), output.puntajeTotal().
   */
  public static String cuerpoCalificacionCriterio(
      String nombreAspirante,
      String nombreCriterio,
      BigDecimal puntajeObtenido,
      BigDecimal puntajeTotal) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          <h2 style="color: #1a5276;">Calificación de criterio registrada</h2>
          <p>Estimado/a <strong>%s</strong>,</p>
          <p>Se ha registrado la calificación del criterio <strong>%s</strong>:</p>
          <table style="border-collapse: collapse; width: 100%%;">
            <tr>
              <td style="padding: 6px 12px; font-weight: bold;">Puntaje obtenido:</td>
              <td style="padding: 6px 12px;">%s</td>
            </tr>
            <tr style="background-color: #f4f6f7;">
              <td style="padding: 6px 12px; font-weight: bold;">Puntaje máximo:</td>
              <td style="padding: 6px 12px;">%s</td>
            </tr>
          </table>
          <p>Puede consultar el estado completo de su proceso a través del sistema.</p>
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(nombreAspirante, nombreCriterio, puntajeObtenido, puntajeTotal);
  }

  public static String cuerpoAdmitido(
      String nombres,
      String apellidos,
      String nombreCohorte) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          <h2 style="color: #1a7a4a;">¡Felicitaciones, has sido admitido!</h2>
          <p>Estimado/a <strong>%s %s</strong>,</p>
          <p>Nos complace informarle que ha sido <strong style="color: #1a7a4a;">admitido/a</strong>
             en el proceso de admisión de posgrados de la UFPS para la cohorte
             <strong>%s</strong>.</p>
          <p>En los próximos días recibirá las instrucciones sobre los pasos a seguir para
             formalizar su matrícula.</p>
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(nombres, apellidos, nombreCohorte);
  }

  public static String cuerpoRechazado(
      String nombres,
      String apellidos,
      String nombreCohorte) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          <h2 style="color: #a93226;">Resultado del proceso de admisión</h2>
          <p>Estimado/a <strong>%s %s</strong>,</p>
          <p>Lamentamos informarle que no ha sido seleccionado/a en el proceso de admisión
             de posgrados de la UFPS para la cohorte <strong>%s</strong>.</p>
          <p>Le animamos a revisar los requisitos y a considerar aplicar en futuras convocatorias.
             Agradecemos su interés en nuestra institución.</p>
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(nombres, apellidos, nombreCohorte);
  }
}

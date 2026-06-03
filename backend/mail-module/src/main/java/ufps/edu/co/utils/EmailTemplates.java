package ufps.edu.co.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

@Component
public class EmailTemplates {

  private static final String ENCABEZADO_URL =
      "https://bucket-posgrados.s3.us-east-2.amazonaws.com/EncabezadoPDF.png";

  private static String encabezado() {
    return "<div style=\"text-align:center;margin-bottom:16px;\">"
        + "<img src=\"" + ENCABEZADO_URL + "\""
        + " alt=\"UFPS Posgrados\" style=\"max-width:100%;height:auto;\"/>"
        + "</div>";
  }

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

  public static final String ASUNTO_CONFIRMACION_CORREO = "Confirma tu correo electrónico - Posgrados UFPS";

  public static final String ASUNTO_ASPIRANTE_CONFIRMA = "Confirmación de asistencia recibida - Posgrados UFPS";

  public static final String ASUNTO_ACTIVIDAD_COMPLETADA = "Actividad completada - Posgrados UFPS";

  public static final String ASUNTO_SOLICITUD_CAMBIO = "Solicitud de cambio recibida - Posgrados UFPS";

  public static final String ASUNTO_CANCELACION_POR_ASPIRANTE = "Cancelación de actividad por aspirante - Posgrados UFPS";

  public static final String ASUNTO_CANCELACION_POR_DIRECTOR = "Tu actividad ha sido cancelada - Posgrados UFPS";

  public static final String ASUNTO_ADMITIDO = "¡Felicitaciones! Has sido admitido - Posgrados UFPS";

  public static final String ASUNTO_RECHAZADO = "Resultado del proceso de admisión - Posgrados UFPS";

  public static final String ASUNTO_CODIGO_ESTUDIANTIL = "Tu código estudiantil ha sido generado - Posgrados UFPS";

  public static final String ASUNTO_APROBACION_PAGO_INSCRIPCION = "Pago de inscripción aprobado - Posgrados UFPS";

  public static final String ASUNTO_RECHAZO_PAGO_INSCRIPCION = "Pago de inscripción rechazado - Posgrados UFPS";

  public static final String ASUNTO_APROBACION_PAGO_MATRICULA = "Pago de matrícula aprobado - Posgrados UFPS";

  public static final String ASUNTO_RECHAZO_PAGO_MATRICULA = "Pago de matrícula rechazado - Posgrados UFPS";

  // ─── Cuerpos HTML ────────────────────────────────────────────────────────────

  public static String cuerpoInscripcion(
      String nombres,
      String apellidos,
      String nombreCohorte,
      String programaPosgrado) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          %s
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
        """.formatted(encabezado(), nombres, apellidos, nombreCohorte, programaPosgrado);
  }

  public static String cuerpoSubidaDocumento(
      String nombreAspirante,
      String nombreDocumento,
      LocalDate fechaCargue) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          %s
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
        """.formatted(encabezado(), nombreAspirante, nombreDocumento, fechaCargue);
  }

  public static String cuerpoAprobacionDocumento(
      String nombreAspirante,
      String nombreDocumento) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          %s
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
        """.formatted(encabezado(), nombreAspirante, nombreDocumento);
  }

  public static String cuerpoRechazoDocumento(
      String nombreAspirante,
      String nombreDocumento,
      String motivoRechazo) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          %s
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
        """.formatted(encabezado(), nombreAspirante, nombreDocumento, motivoRechazo);
  }

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
          %s
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
        """.formatted(encabezado(), nombreAspirante, tipoEntrevista, fecha, hora, ubicacion);
  }

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
          %s
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
        """.formatted(encabezado(), nombreAspirante, tipoEntrevista, fecha, hora, ubicacion);
  }

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
          %s
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
        """.formatted(encabezado(), nombreAspirante, nombrePrueba, tipoPrueba, fecha, hora, ubicacion);
  }

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
          %s
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
        """.formatted(encabezado(), nombreAspirante, nombrePrueba, tipoPrueba, fecha, hora, ubicacion);
  }

  public static String cuerpoCalificacionCriterio(
      String nombreAspirante,
      String nombreCriterio,
      BigDecimal puntajeObtenido,
      BigDecimal puntajeTotal) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          %s
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
        """.formatted(encabezado(), nombreAspirante, nombreCriterio, puntajeObtenido, puntajeTotal);
  }

  public static String cuerpoConfirmacionAlDirector(
      String nombreAspirante,
      String tipoActividad,
      String nombreActividad,
      LocalDate fecha,
      LocalTime hora,
      String lugar) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          %s
          <h2 style="color: #1a7a4a;">Confirmación de asistencia</h2>
          <p>El/La aspirante <strong>%s</strong> ha confirmado su asistencia a la %s
             <strong>%s</strong> con los siguientes detalles:</p>
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
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(encabezado(), nombreAspirante, tipoActividad, nombreActividad, fecha, hora, lugar);
  }

  public static String cuerpoActividadCompletada(
      String nombreAspirante,
      String tipoActividad,
      String nombreActividad,
      LocalDate fecha,
      LocalTime hora,
      String lugar) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          %s
          <h2 style="color: #1a7a4a;">Actividad completada</h2>
          <p>Estimado/a <strong>%s</strong>,</p>
          <p>La %s <strong>%s</strong> realizada el <strong>%s</strong> a las <strong>%s</strong>
             en <strong>%s</strong> ha sido registrada como <strong style="color: #1a7a4a;">completada</strong>.</p>
          <p>Puede consultar el estado de su proceso a través del sistema.</p>
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(encabezado(), nombreAspirante, tipoActividad, nombreActividad, fecha, hora, lugar);
  }

  public static String cuerpoCambioSolicitadoAlDirector(
      String nombreAspirante,
      String tipoActividad,
      String nombreActividad,
      LocalDate fecha,
      LocalTime hora,
      String motivo) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          %s
          <h2 style="color: #b7770d;">Solicitud de cambio</h2>
          <p>El/La aspirante <strong>%s</strong> ha solicitado un cambio en la %s
             <strong>%s</strong> programada para el <strong>%s</strong> a las <strong>%s</strong>.</p>
          <table style="border-collapse: collapse; width: 100%%; border: 1px solid #e0a800; border-radius: 4px;">
            <tr style="background-color: #fef9e7;">
              <td style="padding: 10px 14px; font-weight: bold; color: #b7770d;">Motivo:</td>
              <td style="padding: 10px 14px;">%s</td>
            </tr>
          </table>
          <p>Por favor, revise la solicitud y realice los ajustes necesarios en el sistema.</p>
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(encabezado(), nombreAspirante, tipoActividad, nombreActividad, fecha, hora, motivo);
  }

  public static String cuerpoCancelacionPorAspirante(
      String nombreAspirante,
      String tipoActividad,
      String nombreActividad,
      LocalDate fecha,
      LocalTime hora,
      String motivo) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          %s
          <h2 style="color: #a93226;">Cancelación por aspirante</h2>
          <p>El/La aspirante <strong>%s</strong> ha cancelado la %s <strong>%s</strong>
             programada para el <strong>%s</strong> a las <strong>%s</strong>.</p>
          <table style="border-collapse: collapse; width: 100%%; border: 1px solid #e74c3c; border-radius: 4px;">
            <tr style="background-color: #fdf2f0;">
              <td style="padding: 10px 14px; font-weight: bold; color: #a93226;">Motivo:</td>
              <td style="padding: 10px 14px;">%s</td>
            </tr>
          </table>
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(encabezado(), nombreAspirante, tipoActividad, nombreActividad, fecha, hora, motivo);
  }

  public static String cuerpoCancelacionPorDirector(
      String nombreAspirante,
      String tipoActividad,
      String nombreActividad,
      LocalDate fecha,
      LocalTime hora,
      String motivo) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          %s
          <h2 style="color: #a93226;">Actividad cancelada</h2>
          <p>Estimado/a <strong>%s</strong>,</p>
          <p>Lamentamos informarle que la %s <strong>%s</strong> programada para el
             <strong>%s</strong> a las <strong>%s</strong> ha sido
             <strong style="color: #a93226;">cancelada</strong> por el director de programa.</p>
          <table style="border-collapse: collapse; width: 100%%; border: 1px solid #e74c3c; border-radius: 4px;">
            <tr style="background-color: #fdf2f0;">
              <td style="padding: 10px 14px; font-weight: bold; color: #a93226;">Motivo:</td>
              <td style="padding: 10px 14px;">%s</td>
            </tr>
          </table>
          <p>Pronto recibirá más información sobre los próximos pasos en su proceso de admisión.</p>
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(encabezado(), nombreAspirante, tipoActividad, nombreActividad, fecha, hora, motivo);
  }

  public static String cuerpoConfirmacionCorreo(String nombres, String enlaceConfirmacion) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          %s
          <h2 style="color: #1a5276;">Confirma tu correo electrónico</h2>
          <p>Estimado/a <strong>%s</strong>,</p>
          <p>Gracias por inscribirte en el proceso de admisión de posgrados de la UFPS.
             Para continuar, por favor confirma tu correo electrónico haciendo clic en el siguiente botón:</p>
          <p style="text-align: center; margin: 32px 0;">
            <a href="%s"
               style="background-color: #1a5276; color: #ffffff; padding: 12px 28px;
                      text-decoration: none; border-radius: 4px; font-size: 1em; display: inline-block;">
              Confirmar correo
            </a>
          </p>
          <p style="color: #888; font-size: 0.85em;">
            Este enlace es válido por 24 horas. Si no realizaste esta solicitud, ignora este correo.
          </p>
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(encabezado(), nombres, enlaceConfirmacion);
  }

  public static String cuerpoAdmitido(
      String nombres,
      String apellidos,
      String nombreCohorte) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          %s
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
        """.formatted(encabezado(), nombres, apellidos, nombreCohorte);
  }

  public static String cuerpoRechazado(
      String nombres,
      String apellidos,
      String nombreCohorte) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          %s
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
        """.formatted(encabezado(), nombres, apellidos, nombreCohorte);
  }

  public static String cuerpoCodigoEstudiantil(
      String nombres,
      String apellidos,
      String codigoEstudiante,
      String nombrePrograma) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          %s
          <h2 style="color: #1a5276;">Tu código estudiantil ha sido generado</h2>
          <p>Estimado/a <strong>%s %s</strong>,</p>
          <p>Nos complace informarle que su código estudiantil ha sido generado exitosamente
             como parte de su proceso de matrícula en el programa <strong>%s</strong>.</p>
          <table style="border-collapse: collapse; width: 100%%; border: 1px solid #2e86c1; border-radius: 4px;">
            <tr style="background-color: #eaf4fb;">
              <td style="padding: 10px 14px; font-weight: bold; color: #1a5276;">Código estudiantil:</td>
              <td style="padding: 10px 14px; font-size: 1.2em; font-weight: bold; letter-spacing: 2px;">%s</td>
            </tr>
          </table>
          <p>Guarde este código, ya que lo necesitará para acceder a los servicios académicos
             de la universidad.</p>
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(encabezado(), nombres, apellidos, nombrePrograma, codigoEstudiante);
  }

  public static String cuerpoAprobacionPagoInscripcion(String nombres, String apellidos) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          %s
          <h2 style="color: #1a7a4a;">¡Pago de inscripción aprobado!</h2>
          <p>Estimado/a <strong>%s %s</strong>,</p>
          <p>Le informamos que su <strong>pago de inscripción</strong> ha sido
             <strong style="color: #1a7a4a;">aprobado</strong> satisfactoriamente por la
             Oficina de Posgrados de la UFPS.</p>
          <p>Su proceso de inscripción continúa activo. En los próximos días recibirá
             información sobre los siguientes pasos a seguir.</p>
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(encabezado(), nombres, apellidos);
  }

  public static String cuerpoRechazoPagoInscripcion(String nombres, String apellidos) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          %s
          <h2 style="color: #a93226;">Pago de inscripción rechazado</h2>
          <p>Estimado/a <strong>%s %s</strong>,</p>
          <p>Le informamos que su <strong>pago de inscripción</strong> ha sido
             <strong style="color: #a93226;">rechazado</strong> por la Oficina de
             Posgrados de la UFPS.</p>
          <p>Por favor, verifique que el comprobante cargado sea legible y corresponda
             al pago de inscripción. Puede volver a cargar su factura desde la plataforma.</p>
          <p>Si tiene dudas, comuníquese con la Oficina de Posgrados.</p>
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(encabezado(), nombres, apellidos);
  }

  public static String cuerpoAprobacionPagoMatricula(String nombres, String apellidos) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          %s
          <h2 style="color: #1a7a4a;">¡Pago de matrícula aprobado!</h2>
          <p>Estimado/a <strong>%s %s</strong>,</p>
          <p>Le informamos que su <strong>pago de matrícula</strong> ha sido
             <strong style="color: #1a7a4a;">aprobado</strong> satisfactoriamente por la
             Oficina de Posgrados de la UFPS.</p>
          <p>Su proceso de legalización ha sido completado. A continuación recibirá
             su código estudiantil para acceder a los servicios académicos.</p>
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(encabezado(), nombres, apellidos);
  }

  public static String cuerpoRechazoPagoMatricula(String nombres, String apellidos) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
          %s
          <h2 style="color: #a93226;">Pago de matrícula rechazado</h2>
          <p>Estimado/a <strong>%s %s</strong>,</p>
          <p>Le informamos que su <strong>pago de matrícula</strong> ha sido
             <strong style="color: #a93226;">rechazado</strong> por la Oficina de
             Posgrados de la UFPS.</p>
          <p>Por favor, verifique que el comprobante cargado sea legible y corresponda
             al pago de matrícula. Puede volver a cargar su factura desde la plataforma.</p>
          <p>Si tiene dudas, comuníquese con la Oficina de Posgrados.</p>
          <br/>
          <p style="color: #666; font-size: 0.9em;">
            Universidad Francisco de Paula Santander &mdash; Oficina de Posgrados
          </p>
        </body>
        </html>
        """.formatted(encabezado(), nombres, apellidos);
  }
}

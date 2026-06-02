package ufps.edu.co.processor.crud;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ufps.edu.co.domain.exceptions.errorcodes.AspiranteErrorCode;
import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.PagoErrorCode;
import ufps.edu.co.maps.specific.PagoMap;
import ufps.edu.co.rest.dto.AspiranteCheckoutDTO;
import ufps.edu.co.rest.dto.PagoCheckoutPreviewDTO;
import ufps.edu.co.rest.dto.PagoCheckoutPreviewDataDTO;
import ufps.edu.co.rest.dto.PagoreciboinscripcionDTO;
import ufps.edu.co.rest.dto.PagorecibomatriculaDTO;
import ufps.edu.co.records.output.entity.PagoListadoOutput;
import ufps.edu.co.records.output.entity.PagoconceptoResumenOutput;
import ufps.edu.co.records.output.entity.PagoOutput;
import ufps.edu.co.rest.dto.AspiranteDTO;
import ufps.edu.co.rest.dto.EstadoDTO;
import ufps.edu.co.rest.dto.PagoDTO;
import ufps.edu.co.rest.dto.PagoResumenDTO;
import ufps.edu.co.rest.dto.PagoconceptoDTO;
import ufps.edu.co.processor.receipt.ReciboInscripcionBuildInput;
import ufps.edu.co.processor.receipt.ReciboInscripcionBuilderPort;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.EstadoService;
import ufps.edu.co.rest.services.PagoService;
import ufps.edu.co.rest.services.PagoconceptoService;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.ProgramaService;
import ufps.edu.co.rest.services.PagorecibomatriculaService;
import ufps.edu.co.rest.services.PagoreciboinscripcionService;
import ufps.edu.co.rest.services.UsuarioService;
import ufps.edu.co.wompi.WompiGateway;
import ufps.edu.co.wompi.config.WompiProperties;
import ufps.edu.co.wompi.model.WompiCheckoutRequest;
import ufps.edu.co.wompi.model.WompiCheckoutResponse;
import ufps.edu.co.wompi.model.WompiCustomerData;
import ufps.edu.co.wompi.model.WompiReceiptData;
import ufps.edu.co.wompi.model.WompiWebhookRequest;

@Service
@Transactional
public class PagoProcessor {

    private static final Logger log = LoggerFactory.getLogger(PagoProcessor.class);

    @Autowired
    private PagoService pagoService;

    @Autowired
    private PagoconceptoService pagoconceptoService;

    @Autowired
    private EstadoService estadoService;

    @Autowired
    private ValoresglobalesProcessor valoresglobalesProcessor;

    @Autowired
    private AspiranteService aspiranteService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PagoMap pagoMap;

    @Autowired
    private WompiGateway wompiGateway;

    @Autowired
    private WompiProperties wompiProperties;

    @Autowired
    private PagoreciboinscripcionService pagoreciboinscripcionService;

    @Autowired
    private PagorecibomatriculaService pagorecibomatriculaService;

    @Autowired
    private CohorteService cohorteService;

    @Autowired
    private ProgramaService programaService;

    @Autowired
    private ReciboInscripcionBuilderPort reciboInscripcionBuilderPort;

    @Transactional(readOnly = true)
    public List<PagoListadoOutput> findByAspirante(Integer idAspirante) {
        List<PagoResumenDTO> pagos = pagoService.findResumenByIdAspirante(idAspirante);
        BigDecimal valorInscripcion = calcularMontoInscripcionEnPesos();
        BigDecimal valorMatricula = calcularMontoMatriculaEnPesos(idAspirante);
        // Pre-fetch receipts related to these pagos to prefer stored amounts when available
        var pagoIds = pagos.stream().map(PagoResumenDTO::id).filter(Objects::nonNull).collect(Collectors.toSet());
        List<PagoreciboinscripcionDTO> recibosInscripcion = pagoreciboinscripcionService.findAll().stream()
            .filter(item -> item.getIdPago() != null && pagoIds.contains(item.getIdPago()))
            .collect(Collectors.toList());
        List<PagorecibomatriculaDTO> recibosMatricula = pagorecibomatriculaService.findAll().stream()
            .filter(item -> item.getIdPago() != null && pagoIds.contains(item.getIdPago()))
            .collect(Collectors.toList());

        return pagos.stream()
            .map(dto -> {
                BigDecimal valor = null;
                String tipo = dto.pagoconceptoTipo();
                String tipoNorm = tipo != null ? tipo.trim().toUpperCase(Locale.ROOT) : null;
                if ("INSCRIPCION".equals(tipoNorm)) {
                PagoreciboinscripcionDTO recibo = recibosInscripcion.stream()
                    .filter(r -> Objects.equals(r.getIdPago(), dto.id()))
                    .filter(r -> !isReciboInscripcionVencido(r))
                    .findFirst()
                    .orElse(null);
                if (recibo != null && recibo.getValorpago() != null) {
                    valor = recibo.getValorpago();
                } else {
                    valor = valorInscripcion;
                }
                } else if ("MATRICULA".equals(tipoNorm)) {
                PagorecibomatriculaDTO recibo = recibosMatricula.stream()
                    .filter(r -> Objects.equals(r.getIdPago(), dto.id()))
                    .filter(r -> !isReciboMatriculaVencido(r))
                    .findFirst()
                    .orElse(null);
                if (recibo != null && recibo.getValorpago() != null) {
                    valor = recibo.getValorpago();
                } else {
                    valor = valorMatricula;
                }
                } else {
                valor = resolveValorPagoPorTipoConcepto(tipo, valorInscripcion, valorMatricula);
                }

                return PagoListadoOutput.builder()
                    .id(dto.id())
                    .idAspirante(dto.idAspirante())
                    .idEstado(dto.idEstado())
                    .idPagoconcepto(dto.idPagoconcepto())
                    .aspirante(dto.aspirante())
                    .estado(dto.estado())
                    .pagoconcepto(PagoconceptoResumenOutput.builder()
                        .id(dto.pagoconceptoId())
                        .tipo(dto.pagoconceptoTipo())
                        .build())
                    .valorPagoPesos(valor)
                    .build();
            })
            .toList();
    }

    public void ensureInitialPaymentsForAspirante(Integer idAspirante) {
        AspiranteCheckoutDTO aspirante = aspiranteService.findCheckoutById(idAspirante);
        if (aspirante == null) {
            throw new DomainException(PagoErrorCode.PAGO_NOT_FOUND, idAspirante);
        }

        EstadoDTO estadoPendiente = resolveEstadoPago("PENDIENTE");
        Map<String, PagoconceptoDTO> conceptos = pagoconceptoService.findAll().stream()
                .filter(concepto -> concepto.getTipo() != null)
                .collect(Collectors.toMap(concepto -> concepto.getTipo().toUpperCase(Locale.ROOT), concepto -> concepto,
                        (primero, segundo) -> primero));

        Set<Integer> conceptosExistentes = pagoService.findResumenByIdAspirante(idAspirante).stream()
                .map(PagoResumenDTO::idPagoconcepto)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        crearPagoPendienteSiFalta(idAspirante, estadoPendiente, conceptosExistentes, conceptos.get("INSCRIPCION"));
        crearPagoPendienteSiFalta(idAspirante, estadoPendiente, conceptosExistentes, conceptos.get("MATRICULA"));
    }

    @SuppressWarnings("null")
    public WompiCheckoutResponse iniciarCheckoutInscripcion(Integer idAspirante, Integer authenticatedUserId,
            boolean validarTitular) {
        PagoResumenDTO pago = encontrarPagoConceptoResumen(idAspirante, "INSCRIPCION");
        validarPagoPerteneceAspirante(pago, idAspirante);

        AspiranteCheckoutDTO aspirante = aspiranteService.findCheckoutById(idAspirante);
        validarAspiranteAutenticado(aspirante, authenticatedUserId, validarTitular);

        EstadoDTO estadoRealizado = resolveEstadoPagoOpcional("REALIZADO");
        if (estadoRealizado != null && Objects.equals(pago.idEstado(), estadoRealizado.getId())) {
            throw new DomainException(PagoErrorCode.PAGO_YA_REALIZADO_CONFLICT, pago.id());
        }

        BigDecimal monto = calcularMontoInscripcionEnPesos();
        String referencia = construirReferencia(pago, aspirante);
        String currency = resolverCurrency();
        String publicKey = resolverPublicKey();
        WompiCustomerData customerData = construirCustomerData(aspirante);
        PagoreciboinscripcionResultado pagoreciboResultado = obtenerOCrearPagoreciboInscripcion(idAspirante, pago,
                referencia, monto);
        PagoreciboinscripcionDTO pagoreciboinscripcion = pagoreciboResultado.pagoreciboinscripcion();

        // If there is already an EN CURSO receipt, preserve its stored amount/reference
        // for checkout.
        if (pagoreciboinscripcion != null) {
            if (pagoreciboinscripcion.getValorpago() != null) {
                monto = pagoreciboinscripcion.getValorpago();
            }
            if (pagoreciboinscripcion.getReferenciapago() != null
                    && !pagoreciboinscripcion.getReferenciapago().isBlank()) {
                referencia = pagoreciboinscripcion.getReferenciapago();
            }
        }

        long montoEnCents = calcularCentavosDesdePesos(monto);
        String signatureIntegrity = generarSignatureIntegrity(referencia, montoEnCents, currency);
        WompiReceiptData receiptData = construirReceiptData(pago, aspirante, referencia, monto, montoEnCents, currency);

        PagoreciboinscripcionDTO pagoreciboShallow = construirPagoreciboShallow(pagoreciboinscripcion, pago.id());

        WompiCheckoutRequest request = WompiCheckoutRequest.builder()
                .paymentId(pago.id())
                .aspiranteId(idAspirante)
                .pagoconceptoId(pago.idPagoconcepto())
                .concepto("INSCRIPCION")
                .reference(referencia)
                .amount(monto)
                .amountInCents(montoEnCents)
                .currency(currency)
                .publicKey(publicKey)
                .signatureIntegrity(signatureIntegrity)
                .redirectUrl(resolveReturnUrl())
                .widgetScriptUrl(resolveWidgetScriptUrl())
                // .customerEmail(aspirante != null && aspirante.getPersona() != null ?
                // aspirante.getPersona().getCorreo() : null)
                .customerEmail(null)
                .customerName(construirNombreAspirante(aspirante))
                .customerData(customerData)
                .receiptData(receiptData)
                .pagoreciboinscripcion(pagoreciboShallow)
                .creationDate(LocalDateTime.now())
                .returnUrl(resolveReturnUrl())
                .webhookUrl(resolveWebhookUrl())
                .metadata(Map.of(
                        "paymentId", String.valueOf(pago.id()),
                        "aspiranteId", String.valueOf(idAspirante),
                        "concepto", "INSCRIPCION"))
                .build();

        WompiCheckoutResponse checkoutResponse = wompiGateway.createCheckout(request);
        log.info("Checkout generado para aspirante={} pago={} referencia={} monto={} reciboNuevo={}", idAspirante,
                pago.id(), checkoutResponse != null ? checkoutResponse.reference() : null, monto,
                pagoreciboResultado.creadoNuevo());

        if (pagoreciboResultado.creadoNuevo()) {
            String urlRecibo = construirYSubirReciboInscripcion(checkoutResponse, pagoreciboinscripcion, idAspirante,
                    aspirante);
            log.info("Resultado construcción recibo aspirante={} pago={} urlRecibo={}", idAspirante, pago.id(),
                    urlRecibo);
            if (urlRecibo != null && !urlRecibo.isBlank() && pagoreciboinscripcion != null
                    && pagoreciboinscripcion.getId() != null) {
                PagoreciboinscripcionDTO pagoreciboPersistido = pagoreciboinscripcionService
                        .findById(pagoreciboinscripcion.getId());
                if (pagoreciboPersistido != null) {
                    pagoreciboPersistido.setUrlrecibo(urlRecibo);
                    pagoreciboinscripcion = pagoreciboinscripcionService.update(pagoreciboPersistido.getId(),
                            pagoreciboPersistido);
                    log.info("Pagoreciboinscripcion actualizado con urlrecibo id={} urlRecibo={}",
                            pagoreciboinscripcion.getId(), urlRecibo);
                    pagoreciboShallow = construirPagoreciboShallow(pagoreciboinscripcion, pago.id());
                } else {
                    log.warn("No se encontró pagoreciboinscripcion persistido para actualizar urlrecibo. id={}",
                            pagoreciboinscripcion.getId());
                }
            }
        } else {
            log.info("No se construyó recibo porque ya existía un pagoreciboinscripcion EN CURSO. aspirante={} pago={}",
                    idAspirante, pago.id());
        }

        String checkoutUrl = pagoreciboShallow != null && pagoreciboShallow.getUrlrecibo() != null
                && !pagoreciboShallow.getUrlrecibo().isBlank()
                        ? pagoreciboShallow.getUrlrecibo()
                        : checkoutResponse.checkoutUrl();

        return WompiCheckoutResponse.builder()
                .paymentId(checkoutResponse.paymentId())
                .aspiranteId(checkoutResponse.aspiranteId())
                .pagoconceptoId(checkoutResponse.pagoconceptoId())
                .concepto(checkoutResponse.concepto())
                .reference(checkoutResponse.reference())
                .amount(checkoutResponse.amount())
                .amountInCents(checkoutResponse.amountInCents())
                .currency(checkoutResponse.currency())
                .publicKey(checkoutResponse.publicKey())
                .signatureIntegrity(checkoutResponse.signatureIntegrity())
                .redirectUrl(checkoutResponse.redirectUrl())
                .widgetScriptUrl(checkoutResponse.widgetScriptUrl())
                .checkoutUrl(checkoutUrl)
                .simulated(checkoutResponse.simulated())
                .message(checkoutResponse.message())
                .transactionId(checkoutResponse.transactionId())
                .customerEmail(checkoutResponse.customerEmail())
                .customerName(checkoutResponse.customerName())
                .customerData(checkoutResponse.customerData())
                .receiptData(checkoutResponse.receiptData())
                .pagoreciboinscripcion(pagoreciboShallow)
                .creationDate(checkoutResponse.creationDate())
                .status(checkoutResponse.status())
                .build();
    }

    @SuppressWarnings("null")
    public WompiCheckoutResponse iniciarCheckoutMatricula(Integer idAspirante, Integer authenticatedUserId,
            BigDecimal montoElegido, boolean validarTitular) {
        PagoResumenDTO pago = encontrarPagoConceptoResumen(idAspirante, "MATRICULA");
        validarPagoPerteneceAspirante(pago, idAspirante);

        AspiranteCheckoutDTO aspirante = aspiranteService.findCheckoutById(idAspirante);
        validarAspiranteAutenticado(aspirante, authenticatedUserId, validarTitular);

        BigDecimal montoTotal = calcularMontoMatriculaEnPesos(idAspirante);
        PagorecibomatriculaResultado resultado = obtenerOCrearPagoreciboMatricula(idAspirante, pago,
                montoElegido, montoTotal);
        PagorecibomatriculaDTO pagorecibomatricula = resultado.pagorecibomatricula();

        BigDecimal monto = pagorecibomatricula != null && pagorecibomatricula.getValorpago() != null
                ? pagorecibomatricula.getValorpago()
                : validarMontoElegidoMatricula(montoElegido, montoTotal);
        String referencia = pagorecibomatricula != null && pagorecibomatricula.getReferenciapago() != null
                && !pagorecibomatricula.getReferenciapago().isBlank()
                        ? pagorecibomatricula.getReferenciapago()
                        : construirReferenciaMatricula(pago, aspirante);
        String currency = resolverCurrency();
        String publicKey = resolverPublicKey();
        WompiCustomerData customerData = construirCustomerData(aspirante);

        long montoEnCents = calcularCentavosDesdePesos(monto);
        String signatureIntegrity = generarSignatureIntegrity(referencia, montoEnCents, currency);
        WompiReceiptData receiptData = construirReceiptData(pago, aspirante, referencia, monto, montoEnCents, currency,
                "MATRICULA");

        PagorecibomatriculaDTO pagoreciboShallow = construirPagoreciboMatriculaShallow(pagorecibomatricula,
                pago.id());

        WompiCheckoutRequest request = WompiCheckoutRequest.builder()
                .paymentId(pago.id())
                .aspiranteId(idAspirante)
                .pagoconceptoId(pago.idPagoconcepto())
                .concepto("MATRICULA")
                .reference(referencia)
                .amount(monto)
                .amountInCents(montoEnCents)
                .currency(currency)
                .publicKey(publicKey)
                .signatureIntegrity(signatureIntegrity)
                .redirectUrl(resolveReturnUrl())
                .widgetScriptUrl(resolveWidgetScriptUrl())
                .customerEmail(null)
                .customerName(construirNombreAspirante(aspirante))
                .customerData(customerData)
                .receiptData(receiptData)
                .pagoreciboinscripcion(null)
                .pagorecibomatricula(pagoreciboShallow)
                .creationDate(LocalDateTime.now())
                .returnUrl(resolveReturnUrl())
                .webhookUrl(resolveWebhookUrl())
                .metadata(Map.of(
                        "paymentId", String.valueOf(pago.id()),
                        "aspiranteId", String.valueOf(idAspirante),
                        "concepto", "MATRICULA"))
                .build();

        WompiCheckoutResponse checkoutResponse = wompiGateway.createCheckout(request);
        log.info("Checkout matrícula generado para aspirante={} pago={} referencia={} monto={} reciboNuevo={}",
                idAspirante, pago.id(), checkoutResponse != null ? checkoutResponse.reference() : null, monto,
                resultado.creadoNuevo());

        if (resultado.creadoNuevo()) {
            String urlRecibo = construirYSubirReciboMatricula(checkoutResponse, pagorecibomatricula, idAspirante,
                    aspirante);
            log.info("Resultado construcción recibo matrícula aspirante={} pago={} urlRecibo={}", idAspirante,
                    pago.id(), urlRecibo);
            if (urlRecibo != null && !urlRecibo.isBlank() && pagorecibomatricula != null
                    && pagorecibomatricula.getId() != null) {
                PagorecibomatriculaDTO pagoreciboPersistido = pagorecibomatriculaService
                        .findById(pagorecibomatricula.getId());
                if (pagoreciboPersistido != null) {
                    pagoreciboPersistido.setUrlrecibo(urlRecibo);
                    pagorecibomatricula = pagorecibomatriculaService.update(pagoreciboPersistido.getId(),
                            pagoreciboPersistido);
                    log.info("Pagorecibomatricula actualizado con urlrecibo id={} urlRecibo={}",
                            pagorecibomatricula.getId(), urlRecibo);
                    pagoreciboShallow = construirPagoreciboMatriculaShallow(pagorecibomatricula, pago.id());
                } else {
                    log.warn("No se encontró pagorecibomatricula persistido para actualizar urlrecibo. id={}",
                            pagorecibomatricula.getId());
                }
            }
        }

        String checkoutUrl = pagoreciboShallow != null && pagoreciboShallow.getUrlrecibo() != null
                && !pagoreciboShallow.getUrlrecibo().isBlank() ? pagoreciboShallow.getUrlrecibo()
                        : checkoutResponse.checkoutUrl();

        return WompiCheckoutResponse.builder()
                .paymentId(checkoutResponse.paymentId())
                .aspiranteId(checkoutResponse.aspiranteId())
                .pagoconceptoId(checkoutResponse.pagoconceptoId())
                .concepto(checkoutResponse.concepto())
                .reference(checkoutResponse.reference())
                .amount(checkoutResponse.amount())
                .amountInCents(checkoutResponse.amountInCents())
                .currency(checkoutResponse.currency())
                .publicKey(checkoutResponse.publicKey())
                .signatureIntegrity(checkoutResponse.signatureIntegrity())
                .redirectUrl(checkoutResponse.redirectUrl())
                .widgetScriptUrl(checkoutResponse.widgetScriptUrl())
                .checkoutUrl(checkoutUrl)
                .simulated(checkoutResponse.simulated())
                .message(checkoutResponse.message())
                .transactionId(checkoutResponse.transactionId())
                .customerEmail(checkoutResponse.customerEmail())
                .customerName(checkoutResponse.customerName())
                .customerData(checkoutResponse.customerData())
                .receiptData(checkoutResponse.receiptData())
                .pagoreciboinscripcion(null)
                .pagorecibomatricula(pagoreciboShallow)
                .creationDate(checkoutResponse.creationDate())
                .status(checkoutResponse.status())
                .build();
    }

    public WompiReceiptData prepararReciboInscripcion(Integer idAspirante, Integer authenticatedUserId,
            boolean validarTitular) {
        PagoResumenDTO pago = encontrarPagoConceptoResumen(idAspirante, "INSCRIPCION");
        validarPagoPerteneceAspirante(pago, idAspirante);

        AspiranteCheckoutDTO aspirante = aspiranteService.findCheckoutById(idAspirante);
        validarAspiranteAutenticado(aspirante, authenticatedUserId, validarTitular);

        BigDecimal monto = calcularMontoInscripcionEnPesos();
        String referencia = construirReferencia(pago, aspirante);

        PagoreciboinscripcionDTO pagoreciboinscripcion = pagoreciboinscripcionService
            .findCurrentByIdAspirante(idAspirante);
        if (pagoreciboinscripcion == null) {
            pagoreciboinscripcion = pagoreciboinscripcionService.findAll().stream()
                .filter(item -> item.getIdPago() != null && Objects.equals(item.getIdPago(), pago.id()))
                .findFirst()
                .orElse(null);
        }
        if (pagoreciboinscripcion != null) {
            if (pagoreciboinscripcion.getValorpago() != null) {
                monto = pagoreciboinscripcion.getValorpago();
            }
            if (pagoreciboinscripcion.getReferenciapago() != null
                    && !pagoreciboinscripcion.getReferenciapago().isBlank()) {
                referencia = pagoreciboinscripcion.getReferenciapago();
            }
        }

        long montoEnCents = calcularCentavosDesdePesos(monto);
        return construirReceiptData(pago, aspirante, referencia, monto, montoEnCents, resolverCurrency(),
                "INSCRIPCION");
    }

    public WompiReceiptData prepararReciboMatricula(Integer idAspirante, Integer authenticatedUserId,
            BigDecimal montoElegido, boolean validarTitular) {
        PagoResumenDTO pago = encontrarPagoConceptoResumen(idAspirante, "MATRICULA");
        validarPagoPerteneceAspirante(pago, idAspirante);

        AspiranteCheckoutDTO aspirante = aspiranteService.findCheckoutById(idAspirante);
        validarAspiranteAutenticado(aspirante, authenticatedUserId, validarTitular);

        BigDecimal montoTotal = calcularMontoMatriculaEnPesos(idAspirante);
        PagorecibomatriculaDTO recibo = pagorecibomatriculaService.findCurrentByIdAspirante(idAspirante);
        if (recibo == null) {
            recibo = pagorecibomatriculaService.findAll().stream()
                .filter(item -> item.getIdPago() != null && Objects.equals(item.getIdPago(), pago.id()))
                .findFirst()
                .orElse(null);
        }
        BigDecimal monto;
        if (recibo != null && recibo.getValorpago() != null) {
            monto = recibo.getValorpago();
        } else {
            monto = montoTotal;
        }
        String referencia = recibo != null && recibo.getReferenciapago() != null
            && !recibo.getReferenciapago().isBlank() ? recibo.getReferenciapago()
                : construirReferenciaMatricula(pago, aspirante);

        long montoEnCents = calcularCentavosDesdePesos(monto);
        return construirReceiptData(pago, aspirante, referencia, monto, montoEnCents, resolverCurrency(),
                "MATRICULA");
    }

    @Transactional(readOnly = true)
    public PagoCheckoutPreviewDTO obtenerResumenCheckoutInscripcion(Integer idAspirante, Integer authenticatedUserId,
            boolean validarTitular) {
        PagoCheckoutPreviewDataDTO data = aspiranteService.findCheckoutPreviewById(idAspirante);
        if (data == null) {
            throw new DomainException(PagoErrorCode.PAGO_NOT_FOUND, idAspirante);
        }

        validarAspiranteAutenticado(data.idPersona(), authenticatedUserId, validarTitular);

        PagoResumenDTO pago = encontrarPagoConceptoResumen(idAspirante, "INSCRIPCION");
        validarPagoPerteneceAspirante(pago, idAspirante);

        PagoreciboinscripcionDTO pagoreciboinscripcion = pagoreciboinscripcionService
            .findCurrentByIdAspirante(idAspirante);
        if (pagoreciboinscripcion == null) {
            pagoreciboinscripcion = pagoreciboinscripcionService.findAll().stream()
                .filter(item -> item.getIdPago() != null && Objects.equals(item.getIdPago(), pago.id()))
                .findFirst()
                .orElse(null);
        }
        BigDecimal monto = calcularMontoInscripcionEnPesos();
        @SuppressWarnings("unused")
        String referencia = construirReferencia(pago, null);

        if (pagoreciboinscripcion != null) {
            if (pagoreciboinscripcion.getValorpago() != null) {
                monto = pagoreciboinscripcion.getValorpago();
            }
            if (pagoreciboinscripcion.getReferenciapago() != null
                    && !pagoreciboinscripcion.getReferenciapago().isBlank()) {
                referencia = pagoreciboinscripcion.getReferenciapago();
            }
        }

        String urlrecibo = null;
        String urlfactura = null;
        String estadoName = null;
        if (pagoreciboinscripcion != null) {
            urlrecibo = pagoreciboinscripcion.getUrlrecibo();
            urlfactura = pagoreciboinscripcion.getUrlfactura();
            if (pagoreciboinscripcion.getEstado() != null) {
                estadoName = pagoreciboinscripcion.getEstado().getTipo();
            } else if (pagoreciboinscripcion.getIdEstado() != null) {
                EstadoDTO est = estadoService.findById(pagoreciboinscripcion.getIdEstado());
                if (est != null) {
                    estadoName = est.getTipo();
                }
            }
        }

        return new PagoCheckoutPreviewDTO(
            data.programa(),
            data.periodo(),
            construirNombreCompleto(data.nombres(), data.apellidos()),
            formatearDocumento(data.numerodocumento()),
            data.facultad(),
            data.tipo(),
            monto,
            null,
            null,
            urlrecibo,
            urlfactura,
            estadoName);
    }

    @Transactional(readOnly = true)
    public PagoCheckoutPreviewDTO obtenerResumenCheckoutMatricula(Integer idAspirante, Integer authenticatedUserId,
            BigDecimal montoElegido, boolean validarTitular) {
        PagoCheckoutPreviewDataDTO data = aspiranteService.findCheckoutPreviewById(idAspirante);
        if (data == null) {
            throw new DomainException(PagoErrorCode.PAGO_NOT_FOUND, idAspirante);
        }

        validarAspiranteAutenticado(data.idPersona(), authenticatedUserId, validarTitular);

        PagoResumenDTO pago = encontrarPagoConceptoResumen(idAspirante, "MATRICULA");
        validarPagoPerteneceAspirante(pago, idAspirante);

        PagorecibomatriculaDTO recibo = pagorecibomatriculaService.findCurrentByIdAspirante(idAspirante);
        if (recibo == null) {
            recibo = pagorecibomatriculaService.findAll().stream()
                    .filter(item -> item.getIdPago() != null && Objects.equals(item.getIdPago(), pago.id()))
                    .findFirst()
                    .orElse(null);
        }
        BigDecimal montoTotal = calcularMontoMatriculaEnPesos(idAspirante);
        BigDecimal valorMatricula = montoTotal;
        BigDecimal valorMinimo = null;
        if (valorMatricula != null) {
            valorMinimo = valorMatricula.multiply(BigDecimal.valueOf(0.20d)).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal monto = recibo != null && recibo.getValorpago() != null ? recibo.getValorpago() : montoTotal;

        String urlrecibo = null;
        String urlfactura = null;
        String estadoName = null;
        if (recibo != null) {
            urlrecibo = recibo.getUrlrecibo();
            urlfactura = recibo.getUrlfactura();
            if (recibo.getEstado() != null) {
                estadoName = recibo.getEstado().getTipo();
            } else if (recibo.getIdEstado() != null) {
                EstadoDTO est = estadoService.findById(recibo.getIdEstado());
                if (est != null) {
                    estadoName = est.getTipo();
                }
            }
        }

        return new PagoCheckoutPreviewDTO(
            data.programa(),
            data.periodo(),
            construirNombreCompleto(data.nombres(), data.apellidos()),
            formatearDocumento(data.numerodocumento()),
            data.facultad(),
            data.tipo(),
            monto,
            valorMatricula,
            valorMinimo,
            urlrecibo,
            urlfactura,
            estadoName);
    }

    public PagoOutput confirmarWebhook(WompiWebhookRequest request) {
        return procesarWebhook(request, false);
    }

    public PagoOutput confirmarWebhookAutomatico(WompiWebhookRequest request) {
        return procesarWebhook(request, true);
    }

    private PagoOutput procesarWebhook(WompiWebhookRequest request, boolean actualizarEstado) {
        if (request == null) {
            throw new DomainException(PagoErrorCode.PAGO_NOT_FOUND, null);
        }

        log.info("Procesando webhook Wompi: event={} paymentId={} reference={} status={}",
                request.event(), request.paymentId(), request.reference(), request.status());

        PagoDTO pago = resolvePagoDesdeWebhook(request);
        if (pago == null) {
            log.warn("No se pudo resolver pago desde webhook: reference={} paymentId={}", request.reference(),
                    request.paymentId());
            throw new DomainException(PagoErrorCode.PAGO_NOT_FOUND, request.reference());
        }

        log.info("Pago resuelto desde webhook: id={} idPagoconcepto={} idAspirante={}", pago.getId(),
                pago.getIdPagoconcepto(), pago.getIdAspirante());

        if (request.reference() != null && request.reference().startsWith("PAGO-")) {
            String[] parts = request.reference().split("-");
            if (parts.length >= 4) {
                Integer idPagoReferencia = tryParseInt(parts[1]);
                if (idPagoReferencia != null && !Objects.equals(idPagoReferencia, pago.getId())) {
                    log.warn("Referencia Wompi {} apunta a pago {} pero el recibo resuelto usa pago {}",
                            request.reference(), idPagoReferencia, pago.getId());
                }
            }
        }

        String status = request.status();
        if (status == null || !esEstadoAprobado(status)) {
            log.info("Webhook Wompi ignorado por estado no aprobado paymentId={} reference={} status=({},)",
                    request.paymentId(), request.reference(), status);
            return pagoMap.toOutput(pago);
        }

        WompiCheckoutResponse wompiConfirmation = wompiGateway.confirmPayment(request);
        if (wompiConfirmation != null && wompiConfirmation.status() != null
                && !esEstadoAprobado(wompiConfirmation.status())) {
            log.info("Confirmación Wompi ignorada por estado no aprobado paymentId={} reference={} status={}",
                    request.paymentId(), request.reference(), wompiConfirmation.status());
            return pagoMap.toOutput(pago);
        }

        if (!actualizarEstado) {
            return pagoMap.toOutput(pago);
        }

        EstadoDTO estadoRealizado = resolveEstadoPago("REALIZADO");
        log.info("Webhook aprobado. Actualizando estados para pago id={}", pago.getId());
        actualizarEstadosPagoYRecibo(pago, estadoRealizado);
        PagoDTO updated = pagoService.findById(pago.getId());

        // TODO: disparar notificación por correo al aspirante cuando el pago de
        // inscripción quede realizado.
        return pagoMap.toOutput(updated);
    }

    private PagoDTO resolvePagoDesdeWebhook(WompiWebhookRequest request) {
        PagoDTO pagoDesdeRecibo = resolvePagoDesdeReciboPorReferencia(request.reference());
        if (pagoDesdeRecibo != null) {
            return pagoDesdeRecibo;
        }

        Integer paymentId = request.paymentId();
        if (paymentId == null) {
            paymentId = tryParsePaymentIdFromReference(request.reference());
        }
        if (paymentId == null) {
            return null;
        }

        return pagoService.findById(paymentId);
    }

    private PagoDTO resolvePagoDesdeReciboPorReferencia(String reference) {
        if (reference == null || reference.isBlank()) {
            return null;
        }
        PagoreciboinscripcionDTO reciboInscripcion = pagoreciboinscripcionService.findAll().stream()
                .filter(item -> reference.equalsIgnoreCase(item.getReferenciapago()))
                .findFirst()
                .orElse(null);
        if (reciboInscripcion != null) {
            log.info("Recibo inscripcion encontrado por referencia: id={} idPago={} referencia={}",
                    reciboInscripcion.getId(), reciboInscripcion.getIdPago(), reciboInscripcion.getReferenciapago());
            if (reciboInscripcion.getIdPago() != null) {
                PagoDTO pago = pagoService.findById(reciboInscripcion.getIdPago());
                if (pago != null) {
                    return pago;
                }
            }
        }

        PagorecibomatriculaDTO reciboMatricula = pagorecibomatriculaService.findAll().stream()
                .filter(item -> reference.equalsIgnoreCase(item.getReferenciapago()))
                .findFirst()
                .orElse(null);
        if (reciboMatricula != null) {
            log.info("Recibo matricula encontrado por referencia: id={} idPago={} referencia={}",
                    reciboMatricula.getId(), reciboMatricula.getIdPago(), reciboMatricula.getReferenciapago());
            if (reciboMatricula.getIdPago() != null) {
                return pagoService.findById(reciboMatricula.getIdPago());
            }
        }

        return null;
    }

    private Integer tryParsePaymentIdFromReference(String reference) {
        if (reference == null || !reference.startsWith("PAGO-")) {
            return null;
        }

        String[] parts = reference.split("-");
        if (parts.length < 2) {
            return null;
        }

        return tryParseInt(parts[1]);
    }

    private void actualizarEstadosPagoYRecibo(PagoDTO pago, EstadoDTO estadoRealizado) {
        if (pago == null || estadoRealizado == null) {
            throw new IllegalArgumentException("pago y estadoRealizado son requeridos");
        }

        String tipoConcepto = pagoconceptoService.findAll().stream()
                .filter(concepto -> concepto.getId() != null
                        && Objects.equals(concepto.getId(), pago.getIdPagoconcepto()))
                .map(PagoconceptoDTO::getTipo)
                .filter(tipo -> tipo != null && !tipo.isBlank())
                .findFirst()
                .orElse(null);

        if (tipoConcepto == null) {
            throw new DomainException(PagoErrorCode.PAGO_CONCEPTO_NOT_FOUND, pago.getIdPagoconcepto());
        }

        if ("INSCRIPCION".equalsIgnoreCase(tipoConcepto)) {
            actualizarReciboInscripcionYPago(pago, estadoRealizado);
            return;
        }

        if ("MATRICULA".equalsIgnoreCase(tipoConcepto)) {
            actualizarReciboMatriculaYPago(pago, estadoRealizado);
            return;
        }

        throw new DomainException(PagoErrorCode.PAGO_CONCEPTO_INVALIDO, tipoConcepto);
    }

    private void actualizarReciboInscripcionYPago(PagoDTO pago, EstadoDTO estadoRealizado) {
        PagoreciboinscripcionDTO recibo = pagoreciboinscripcionService.findCurrentByIdAspirante(pago.getIdAspirante());
        if (recibo == null) {
            throw new DomainException(PagoErrorCode.PAGO_NOT_FOUND, pago.getIdAspirante());
        }

        recibo.setIdEstado(resolveEstadoReciboCompletado().getId());
        pagoreciboinscripcionService.update(recibo.getId(), recibo);

        pago.setIdEstado(estadoRealizado.getId());
        pagoService.update(pago.getId(), pago);

        actualizarEstadoAspirantePazYSalvo(pago.getIdAspirante());
    }

    private void actualizarReciboMatriculaYPago(PagoDTO pago, EstadoDTO estadoRealizado) {
        log.info("Actualizando recibo matricula para pago id={}", pago.getId());
        PagorecibomatriculaDTO recibo = pagorecibomatriculaService.findAll().stream()
                .filter(item -> item.getIdPago() != null && Objects.equals(item.getIdPago(), pago.getId()))
                .findFirst()
                .orElse(null);
        if (recibo == null) {
            log.warn("No se encontro recibo matricula para pago id={} aspirante={}", pago.getId(), pago.getIdAspirante());
            throw new DomainException(PagoErrorCode.PAGO_NOT_FOUND, pago.getIdAspirante());
        }

        log.info("Recibo matricula encontrado id={} idPago={} idEstadoActual={}", recibo.getId(), recibo.getIdPago(),
                recibo.getIdEstado());

        Integer nuevoEstadoRecibo = resolveEstadoReciboCompletado().getId();
        log.info("Seteando nuevo estado de recibo matricula id={} -> idEstado={}", recibo.getId(), nuevoEstadoRecibo);
        recibo.setIdEstado(nuevoEstadoRecibo);
        pagorecibomatriculaService.update(recibo.getId(), recibo);

        pago.setIdEstado(estadoRealizado.getId());
        pagoService.update(pago.getId(), pago);

        actualizarEstadoAspiranteLegalizado(pago.getIdAspirante());
    }

    private void actualizarEstadoAspirantePazYSalvo(Integer idAspirante) {
        EstadoDTO estadoPazYSalvo = resolveEstadoAspirantePazYSalvo();
        AspiranteDTO aspirante = aspiranteService.findById(idAspirante);
        if (aspirante == null) {
            throw new DomainException(AspiranteErrorCode.ASPIRANTE_NOT_FOUND, idAspirante);
        }

        aspirante.setIdEstado(estadoPazYSalvo.getId());
        aspirante.setEstado(estadoPazYSalvo);
        aspiranteService.update(aspirante.getId(), aspirante);
    }

    private void actualizarEstadoAspiranteLegalizado(Integer idAspirante) {
        EstadoDTO estadoLegalizado = resolveEstadoAspiranteLegalizado();
        AspiranteDTO aspirante = aspiranteService.findById(idAspirante);
        if (aspirante == null) {
            throw new DomainException(AspiranteErrorCode.ASPIRANTE_NOT_FOUND, idAspirante);
        }

        aspirante.setIdEstado(estadoLegalizado.getId());
        aspirante.setEstado(estadoLegalizado);
        aspiranteService.update(aspirante.getId(), aspirante);
    }

    private EstadoDTO resolveEstadoReciboCompletado() {
        EstadoDTO estado = estadoService.findByTipoAndEntidad("COMPLETADO", "pagoinscripcion");
        if (estado == null) {
            estado = estadoService.findByTipoAndEntidad("COMPLETADO", "PAGOINSCRIPCION");
        }
        if (estado == null) {
            estado = estadoService.findByTipoAndEntidad("COMPLETADO", "pagomatricula");
        }
        if (estado == null) {
            estado = estadoService.findByTipoAndEntidad("COMPLETADO", "PAGOMATRICULA");
        }
        if (estado == null) {
            throw new DomainException(PagoErrorCode.PAGO_ESTADO_NOT_FOUND, "COMPLETADO");
        }
        return estado;
    }

    private EstadoDTO resolveEstadoAspirantePazYSalvo() {
        EstadoDTO estado = estadoService.findByTipoAndEntidad("PAZ Y SALVO", "aspirante");
        if (estado == null) {
            estado = estadoService.findByTipoAndEntidad("PAZ Y SALVO", "ASPIRANTE");
        }
        if (estado == null) {
            throw new DomainException(PagoErrorCode.PAGO_ESTADO_NOT_FOUND, "PAZ Y SALVO");
        }
        return estado;
    }

    private EstadoDTO resolveEstadoAspiranteLegalizado() {
        EstadoDTO estado = estadoService.findByTipoAndEntidad("LEGALIZADO", "aspirante");
        if (estado == null) {
            estado = estadoService.findByTipoAndEntidad("LEGALIZADO", "ASPIRANTE");
        }
        if (estado == null) {
            throw new DomainException(PagoErrorCode.PAGO_ESTADO_NOT_FOUND, "LEGALIZADO");
        }
        return estado;
    }

    private void crearPagoPendienteSiFalta(Integer idAspirante, EstadoDTO estadoPendiente,
            Set<Integer> conceptosExistentes, PagoconceptoDTO concepto) {
        if (concepto == null || conceptosExistentes.contains(concepto.getId())) {
            return;
        }
        pagoService.create(PagoDTO.builder()
                .idAspirante(idAspirante)
                .idEstado(estadoPendiente.getId())
                .idPagoconcepto(concepto.getId())
                .build());
    }

    private PagoResumenDTO encontrarPagoConceptoResumen(Integer idAspirante, String tipoConcepto) {
        PagoconceptoDTO concepto = pagoconceptoService.findAll().stream()
                .filter(item -> item.getTipo() != null && item.getTipo().equalsIgnoreCase(tipoConcepto))
                .findFirst()
                .orElseThrow(() -> new DomainException(PagoErrorCode.PAGO_CONCEPTO_NOT_FOUND, tipoConcepto));

        return pagoService.findResumenByIdAspirante(idAspirante).stream()
                .filter(pago -> Objects.equals(pago.idPagoconcepto(), concepto.getId()))
                .findFirst()
                .orElseThrow(() -> new DomainException(PagoErrorCode.PAGO_NOT_FOUND, idAspirante));
    }

    private void validarPagoPerteneceAspirante(PagoResumenDTO pago, Integer idAspirante) {
        if (pago == null) {
            throw new DomainException(PagoErrorCode.PAGO_NOT_FOUND, idAspirante);
        }
        if (!Objects.equals(pago.idAspirante(), idAspirante)) {
            throw new DomainException(PagoErrorCode.PAGO_NO_PERTENECE_AL_ASPIRANTE_FORBIDDEN, idAspirante);
        }
    }

    private EstadoDTO resolveEstadoPago(String tipo) {
        EstadoDTO estado = estadoService.findByTipoAndEntidad(tipo, "pago");
        if (estado == null) {
            estado = estadoService.findByTipoAndEntidad(tipo, "PAGO");
        }
        if (estado == null) {
            throw new DomainException(PagoErrorCode.PAGO_ESTADO_NOT_FOUND, tipo);
        }
        return estado;
    }

    private EstadoDTO resolveEstadoPagoOpcional(String tipo) {
        EstadoDTO estado = estadoService.findByTipoAndEntidad(tipo, "pago");
        if (estado != null) {
            return estado;
        }
        return estadoService.findByTipoAndEntidad(tipo, "PAGO");
    }

    private BigDecimal calcularMontoInscripcionEnPesos() {
        return valoresglobalesProcessor.calcularValorInscripcionPesos();
    }

    private long calcularCentavosDesdePesos(BigDecimal valorPesos) {
        if (valorPesos == null) {
            return 0L;
        }
        return valorPesos.multiply(BigDecimal.valueOf(100L)).setScale(0, RoundingMode.HALF_UP).longValue();
    }

    private PagoreciboinscripcionResultado obtenerOCrearPagoreciboInscripcion(Integer idAspirante, PagoResumenDTO pago,
            String referencia, BigDecimal valorPago) {
        PagoreciboinscripcionDTO existente = pagoreciboinscripcionService.findCurrentByIdAspirante(idAspirante);
        if (existente != null) {
            return new PagoreciboinscripcionResultado(existente, false);
        }

        String referenciaNueva = construirReferenciaUnica(referencia);

        EstadoDTO estadoEnCurso = resolveEstadoPagoInscripcionEnCurso();
        PagoreciboinscripcionDTO nuevo = PagoreciboinscripcionDTO.builder()
                .fechavencimiento(LocalDate.now().plusDays(5))
                .referenciapago(referenciaNueva)
                .valorpago(valorPago)
                .idEstado(estadoEnCurso.getId())
                .idPago(pago.id())
                .build();

        return new PagoreciboinscripcionResultado(pagoreciboinscripcionService.create(nuevo), true);
    }

    private PagorecibomatriculaResultado obtenerOCrearPagoreciboMatricula(Integer idAspirante, PagoResumenDTO pago,
            BigDecimal montoElegido, BigDecimal montoTotal) {
        PagorecibomatriculaDTO existente = pagorecibomatriculaService.findCurrentByIdAspirante(idAspirante);
        if (existente != null) {
            return new PagorecibomatriculaResultado(existente, false);
        }

        BigDecimal valorValidado = validarMontoElegidoMatricula(montoElegido, montoTotal);
        String referencia = construirReferenciaUnica(construirReferenciaMatricula(pago, null));

        EstadoDTO estadoEnCurso = resolveEstadoPagoMatriculaEnCurso();
        PagorecibomatriculaDTO nuevo = PagorecibomatriculaDTO.builder()
                .fechavencimiento(LocalDate.now().plusDays(5))
                .referenciapago(referencia)
                .valorpago(valorValidado)
                .idEstado(estadoEnCurso.getId())
                .idPago(pago.id())
                .build();

        return new PagorecibomatriculaResultado(pagorecibomatriculaService.create(nuevo), true);
    }

    private String construirYSubirReciboInscripcion(WompiCheckoutResponse checkoutResponse,
            PagoreciboinscripcionDTO pagoreciboinscripcion, Integer idAspirante, AspiranteCheckoutDTO aspirante) {
        if (reciboInscripcionBuilderPort == null) {
            throw new IllegalStateException("No existe un bean ReciboInscripcionBuilderPort disponible en el contexto");
        }
        if (checkoutResponse == null || pagoreciboinscripcion == null) {
            return null;
        }

        PagoCheckoutPreviewDataDTO resumenData = aspiranteService.findCheckoutPreviewById(idAspirante);
        String programa = resumenData != null ? resumenData.programa() : null;
        String periodo = resumenData != null ? resumenData.periodo() : null;

        String nombreCompleto = construirNombreAspirante(aspirante);
        String documento = aspirante != null && aspirante.numerodocumento() != null
                ? String.valueOf(aspirante.numerodocumento())
                : null;
        String correo = aspirante != null ? aspirante.correo() : null;

        ReciboInscripcionBuildInput input = new ReciboInscripcionBuildInput(
                checkoutResponse.reference(),
                periodo,
                programa,
                nombreCompleto,
                "Aspirante",
                documento,
                correo,
                pagoreciboinscripcion.getFechavencimiento(),
                "Derechos de inscripción posgrado",
                checkoutResponse.amount(),
                checkoutResponse.amountInCents(),
                checkoutResponse.creationDate(),
                checkoutResponse.reference());

        return reciboInscripcionBuilderPort.construirYSubirRecibo(input);
    }

    private String construirYSubirReciboMatricula(WompiCheckoutResponse checkoutResponse,
            PagorecibomatriculaDTO pagorecibomatricula, Integer idAspirante, AspiranteCheckoutDTO aspirante) {
        if (reciboInscripcionBuilderPort == null) {
            throw new IllegalStateException("No existe un bean ReciboInscripcionBuilderPort disponible en el contexto");
        }
        if (checkoutResponse == null || pagorecibomatricula == null) {
            return null;
        }

        PagoCheckoutPreviewDataDTO resumenData = aspiranteService.findCheckoutPreviewById(idAspirante);
        String programa = resumenData != null ? resumenData.programa() : null;
        String periodo = resumenData != null ? resumenData.periodo() : null;

        String nombreCompleto = construirNombreAspirante(aspirante);
        String documento = aspirante != null && aspirante.numerodocumento() != null
                ? String.valueOf(aspirante.numerodocumento())
                : null;
        String correo = aspirante != null ? aspirante.correo() : null;

        ReciboInscripcionBuildInput input = new ReciboInscripcionBuildInput(
                checkoutResponse.reference(),
                periodo,
                programa,
                nombreCompleto,
                "Aspirante",
                documento,
                correo,
                pagorecibomatricula.getFechavencimiento(),
                "Matrícula posgrados UFPS",
                checkoutResponse.amount(),
                checkoutResponse.amountInCents(),
                checkoutResponse.creationDate(),
                checkoutResponse.reference());

        return reciboInscripcionBuilderPort.construirYSubirRecibo(input);
    }

    private PagoreciboinscripcionDTO construirPagoreciboShallow(PagoreciboinscripcionDTO pagoreciboinscripcion,
            Integer pagoId) {
        if (pagoreciboinscripcion == null) {
            return null;
        }

        PagoDTO pagoShallow = PagoDTO.builder().id(pagoId).build();
        return PagoreciboinscripcionDTO.builder()
                .id(pagoreciboinscripcion.getId())
                .fechavencimiento(pagoreciboinscripcion.getFechavencimiento())
                .urlrecibo(pagoreciboinscripcion.getUrlrecibo())
                .urlfactura(pagoreciboinscripcion.getUrlfactura())
                .referenciapago(pagoreciboinscripcion.getReferenciapago())
                .valorpago(pagoreciboinscripcion.getValorpago())
                .idEstado(pagoreciboinscripcion.getIdEstado())
                .idPago(pagoreciboinscripcion.getIdPago())
                .pago(pagoShallow)
                .estado(null)
                .build();
    }

    private record PagoreciboinscripcionResultado(PagoreciboinscripcionDTO pagoreciboinscripcion,
            boolean creadoNuevo) {
    }

    private record PagorecibomatriculaResultado(PagorecibomatriculaDTO pagorecibomatricula,
            boolean creadoNuevo) {
    }

    private EstadoDTO resolveEstadoPagoInscripcionEnCurso() {
        EstadoDTO estado = estadoService.findByTipoAndEntidad("EN CURSO", "pagoinscripcion");
        if (estado == null) {
            estado = estadoService.findByTipoAndEntidad("EN CURSO", "PAGOINSCRIPCION");
        }
        if (estado == null) {
            throw new DomainException(PagoErrorCode.PAGO_ESTADO_NOT_FOUND, "EN CURSO/pagoinscripcion");
        }
        return estado;
    }

    private EstadoDTO resolveEstadoPagoMatriculaEnCurso() {
        EstadoDTO estado = estadoService.findByTipoAndEntidad("EN CURSO", "pagomatricula");
        if (estado == null) {
            estado = estadoService.findByTipoAndEntidad("EN CURSO", "PAGOMATRICULA");
        }
        if (estado == null) {
            throw new DomainException(PagoErrorCode.PAGO_ESTADO_NOT_FOUND, "EN CURSO/pagomatricula");
        }
        return estado;
    }

    private BigDecimal calcularMontoMatriculaEnPesos(Integer idAspirante) {
        Integer idCohorte = aspiranteService.findIdCohorteById(idAspirante);
        if (idCohorte == null) {
            return null;
        }
        var cohorte = cohorteService.findById(idCohorte);
        if (cohorte == null || cohorte.getIdPrograma() == null) {
            return null;
        }
        var programa = programaService.findById(cohorte.getIdPrograma());
        if (programa == null || programa.getValormatricula() == null) {
            return null;
        }
        BigDecimal salariosMinimos = programa.getValormatricula();
        BigDecimal salarioMinimoPesos = BigDecimal.valueOf(valoresglobalesProcessor.getCurrentSalaryMinimumPesos());
        return salariosMinimos.multiply(salarioMinimoPesos);
    }

    private boolean isReciboInscripcionVencido(PagoreciboinscripcionDTO recibo) {
        if (recibo == null) {
            return true;
        }
        if (recibo.getEstado() != null && recibo.getEstado().getTipo() != null) {
            return "VENCIDO".equalsIgnoreCase(recibo.getEstado().getTipo());
        }
        if (recibo.getIdEstado() != null) {
            EstadoDTO est = estadoService.findById(recibo.getIdEstado());
            if (est != null && est.getTipo() != null) {
                return "VENCIDO".equalsIgnoreCase(est.getTipo());
            }
        }
        return false;
    }

    private boolean isReciboMatriculaVencido(PagorecibomatriculaDTO recibo) {
        if (recibo == null) {
            return true;
        }
        if (recibo.getEstado() != null && recibo.getEstado().getTipo() != null) {
            return "VENCIDO".equalsIgnoreCase(recibo.getEstado().getTipo());
        }
        if (recibo.getIdEstado() != null) {
            EstadoDTO est = estadoService.findById(recibo.getIdEstado());
            if (est != null && est.getTipo() != null) {
                return "VENCIDO".equalsIgnoreCase(est.getTipo());
            }
        }
        return false;
    }

    private BigDecimal resolveValorPagoPorTipoConcepto(String tipoConcepto, BigDecimal valorInscripcion,
            BigDecimal valorMatricula) {
        if (tipoConcepto == null) {
            return null;
        }

        String tipoNormalizado = tipoConcepto.trim().toUpperCase(Locale.ROOT);
        if ("INSCRIPCION".equals(tipoNormalizado)) {
            return valorInscripcion;
        }
        if ("MATRICULA".equals(tipoNormalizado)) {
            return valorMatricula;
        }
        return null;
    }

    private String resolverCurrency() {
        String currency = wompiProperties.getCurrency();
        return currency == null || currency.isBlank() ? "COP" : currency;
    }

    private String resolveReturnUrl() {
        return normalizeConfigValue(wompiProperties.getReturnUrl());
    }

    private String resolveWebhookUrl() {
        return normalizeConfigValue(wompiProperties.getWebhookUrl());
    }

    private String resolveWidgetScriptUrl() {
        String widgetScriptUrl = wompiProperties.getWidgetScriptUrl();
        return widgetScriptUrl == null || widgetScriptUrl.isBlank() ? "https://checkout.wompi.co/widget.js"
                : widgetScriptUrl;
    }

    private String resolverPublicKey() {
        String publicKey = wompiProperties.getPublicKey();
        if (publicKey != null && !publicKey.isBlank()) {
            return publicKey;
        }
        return wompiProperties.isSimulated() ? "simulated-public-key" : null;
    }

    private String generarSignatureIntegrity(String reference, long amountInCents, String currency) {
        String secret = wompiProperties.getIntegritySecret();
        if (secret == null || secret.isBlank()) {
            secret = "simulated-secret";
        }

        String payload = reference + amountInCents + currency + secret;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo calcular la firma de integridad de Wompi", e);
        }
    }

    private String normalizeConfigValue(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String construirReferencia(PagoResumenDTO pago, AspiranteCheckoutDTO aspirante) {
        String nombre = aspirante != null && aspirante.correo() != null
                ? aspirante.correo().replaceAll("[^a-zA-Z0-9]", "")
                : "aspirante";
        return "PAGO-" + pago.id() + "-" + nombre.toUpperCase(Locale.ROOT) + "-" + LocalDate.now().getYear();
    }

    private String construirReferenciaMatricula(PagoResumenDTO pago, AspiranteCheckoutDTO aspirante) {
        String nombre = aspirante != null && aspirante.correo() != null
                ? aspirante.correo().replaceAll("[^a-zA-Z0-9]", "")
                : "aspirante";
        return "PAGO-" + pago.id() + "-MATRICULA-POSGRADOS-UFPS-" + nombre.toUpperCase(Locale.ROOT) + "-"
                + LocalDate.now().getYear();
    }

    private String construirReferenciaUnica(String referenciaBase) {
        if (referenciaBase == null || referenciaBase.isBlank()) {
            return referenciaBase;
        }
        return referenciaBase + "-" + Instant.now().toEpochMilli();
    }

    private String construirNombreAspirante(AspiranteCheckoutDTO aspirante) {
        if (aspirante == null) {
            return null;
        }
        String nombres = aspirante.nombres() != null ? aspirante.nombres() : "";
        String apellidos = aspirante.apellidos() != null ? aspirante.apellidos() : "";
        return (nombres + " " + apellidos).trim();
    }

    private WompiCustomerData construirCustomerData(AspiranteCheckoutDTO aspirante) {
        if (aspirante == null) {
            return null;
        }

        String fullName = construirNombreAspirante(aspirante);
        String email = aspirante.correo();
        String phoneNumber = aspirante.celular() != null ? aspirante.celular() : aspirante.telefono();
        String legalId = aspirante.numerodocumento() != null ? String.valueOf(aspirante.numerodocumento()) : null;
        String legalIdType = aspirante.tipoDocumento();

        return WompiCustomerData.builder()
                .email(email)
                .fullName(fullName)
                .phoneNumber(phoneNumber)
                .phoneNumberPrefix("+57")
                .legalId(legalId)
                .legalIdType(legalIdType)
                .build();
    }

    private WompiReceiptData construirReceiptData(PagoResumenDTO pago, AspiranteCheckoutDTO aspirante,
            String referencia,
            BigDecimal monto, long montoEnCents, String currency) {
        return construirReceiptData(pago, aspirante, referencia, monto, montoEnCents, currency, "INSCRIPCION");
    }

    private WompiReceiptData construirReceiptData(PagoResumenDTO pago, AspiranteCheckoutDTO aspirante,
            String referencia,
            BigDecimal monto, long montoEnCents, String currency, String concepto) {
        Integer personaId = aspirante != null ? aspirante.idPersona() : null;
        Integer cohorteId = aspirante != null ? aspirante.idCohorte() : null;
        WompiCustomerData customerData = construirCustomerData(aspirante);

        return WompiReceiptData.builder()
                .paymentId(pago != null ? pago.id() : null)
                .aspiranteId(aspirante != null ? aspirante.id() : null)
                .personaId(personaId)
                .cohorteId(cohorteId)
                .pagoconceptoId(pago != null ? pago.idPagoconcepto() : null)
                .concepto(concepto)
                .reference(referencia)
                .amount(monto)
                .amountInCents(montoEnCents)
                .currency(currency)
                .fullName(customerData != null ? customerData.fullName() : null)
                .email(customerData != null ? customerData.email() : null)
                .phoneNumber(customerData != null ? customerData.phoneNumber() : null)
                .legalId(customerData != null ? customerData.legalId() : null)
                .legalIdType(customerData != null ? customerData.legalIdType() : null)
                .build();
    }

    private PagorecibomatriculaDTO construirPagoreciboMatriculaShallow(PagorecibomatriculaDTO pagorecibomatricula,
            Integer pagoId) {
        if (pagorecibomatricula == null) {
            return null;
        }

        PagoDTO pagoShallow = PagoDTO.builder().id(pagoId).build();
        return PagorecibomatriculaDTO.builder()
                .id(pagorecibomatricula.getId())
                .fechavencimiento(pagorecibomatricula.getFechavencimiento())
                .urlrecibo(pagorecibomatricula.getUrlrecibo())
                .urlfactura(pagorecibomatricula.getUrlfactura())
                .referenciapago(pagorecibomatricula.getReferenciapago())
                .valorpago(pagorecibomatricula.getValorpago())
                .idEstado(pagorecibomatricula.getIdEstado())
                .idPago(pagorecibomatricula.getIdPago())
                .pago(pagoShallow)
                .estado(null)
                .build();
    }

    private BigDecimal validarMontoElegidoMatricula(BigDecimal montoElegido, BigDecimal montoTotal) {
        if (montoTotal == null) {
            throw new DomainException(PagoErrorCode.VALOR_GLOBAL_NO_CONFIGURADO_NOT_FOUND, "MATRICULA");
        }
        if (montoElegido == null || montoElegido.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException(PagoErrorCode.MATRICULA_MONTO_ELEGIDO_INVALIDO, montoElegido);
        }

        BigDecimal minimoPermitido = montoTotal.multiply(BigDecimal.valueOf(0.20d));
        if (montoElegido.compareTo(minimoPermitido) < 0) {
            throw new DomainException(PagoErrorCode.MATRICULA_MONTO_ELEGIDO_INVALIDO, montoElegido);
        }

        return montoElegido;
    }

    private void validarAspiranteAutenticado(AspiranteCheckoutDTO aspirante, Integer authenticatedUserId,
            boolean validarTitular) {
        if (!validarTitular) {
            return;
        }
        if (authenticatedUserId == null) {
            throw new DomainException(PagoErrorCode.ASPIRANTE_AUTENTICADO_NO_COINCIDE_FORBIDDEN, null);
        }

        Integer idPersonaUsuario = usuarioService.findIdPersonaById(authenticatedUserId);
        if (idPersonaUsuario != null && aspirante != null && aspirante.idPersona() != null
                && idPersonaUsuario.equals(aspirante.idPersona())) {
            return;
        }

        throw new DomainException(PagoErrorCode.ASPIRANTE_AUTENTICADO_NO_COINCIDE_FORBIDDEN, authenticatedUserId);
    }

    private void validarAspiranteAutenticado(Integer idPersonaAspirante, Integer authenticatedUserId,
            boolean validarTitular) {
        if (!validarTitular) {
            return;
        }
        if (authenticatedUserId == null) {
            throw new DomainException(PagoErrorCode.ASPIRANTE_AUTENTICADO_NO_COINCIDE_FORBIDDEN, null);
        }

        Integer idPersonaUsuario = usuarioService.findIdPersonaById(authenticatedUserId);
        if (idPersonaUsuario != null && idPersonaAspirante != null && idPersonaUsuario.equals(idPersonaAspirante)) {
            return;
        }

        throw new DomainException(PagoErrorCode.ASPIRANTE_AUTENTICADO_NO_COINCIDE_FORBIDDEN, authenticatedUserId);
    }

    private String construirNombreCompleto(String nombres, String apellidos) {
        String nombresLimpios = nombres != null ? nombres : "";
        String apellidosLimpios = apellidos != null ? apellidos : "";
        return (nombresLimpios + " " + apellidosLimpios).trim();
    }

    private String formatearDocumento(Integer numerodocumento) {
        if (numerodocumento == null) {
            return null;
        }
        return String.format(Locale.ROOT, "%d", numerodocumento);
    }

    private Boolean esEstadoAprobado(String status) {
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        return normalized.equals("APPROVED") || normalized.equals("APPROVAL") || normalized.equals("APPROVAL_SUCCESS")
                || normalized.equals("SUCCESS") || normalized.equals("PAID") || normalized.equals("PAYED");
    }

    private Integer tryParseInt(String value) {
        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }
}
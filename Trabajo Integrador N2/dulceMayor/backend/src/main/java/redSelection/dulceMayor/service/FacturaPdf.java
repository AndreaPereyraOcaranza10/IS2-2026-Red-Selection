package redSelection.dulceMayor.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.enums.EstadoFactura;
import redSelection.dulceMayor.model.*;
import redSelection.dulceMayor.security.UsuarioAutenticado;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * PDF de la factura (BK-05), generado con OpenPDF al momento de descargarla: así siempre refleja el estado actual
 * (por ejemplo, si se anuló). Usa la misma fuente (Nunito) y el mismo color de marca que los frontends, el
 * caramelo del logo y una marca de agua. Es un comprobante interno: no reemplaza la factura electrónica de AFIP.
 */
@Service
public class FacturaPdf {

    // ---------- Identidad visual (igual que el front) ----------
    private static final Color MARCA = new Color(194, 24, 91);
    private static final Color MARCA_SUAVE = new Color(244, 205, 220);
    private static final Color TEXTO = new Color(40, 40, 40);
    private static final Color GRIS = new Color(120, 120, 120);

    private static final BaseFont NUNITO = fuente("Nunito-Regular.ttf");
    private static final BaseFont NUNITO_BOLD = fuente("Nunito-Bold.ttf");

    private static final Font EMPRESA = new Font(NUNITO_BOLD, 20, Font.NORMAL, MARCA);
    private static final Font TITULO = new Font(NUNITO_BOLD, 22, Font.NORMAL, MARCA);
    private static final Font NEGRITA = new Font(NUNITO_BOLD, 10, Font.NORMAL, TEXTO);
    private static final Font NORMAL = new Font(NUNITO, 10, Font.NORMAL, TEXTO);
    private static final Font CHICA = new Font(NUNITO, 8, Font.NORMAL, GRIS);
    private static final Font CABECERA = new Font(NUNITO_BOLD, 10, Font.NORMAL, MARCA);
    private static final Font CABECERA_DESTACADA = new Font(NUNITO_BOLD, 10, Font.NORMAL, Color.WHITE);
    private static final Font TOTAL = new Font(NUNITO_BOLD, 15, Font.NORMAL, MARCA);

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final FacturaClienteService facturas;
    private final DulceMayorService dulceMayorService;

    public FacturaPdf(FacturaClienteService facturas, DulceMayorService dulceMayorService) {
        this.facturas = facturas;
        this.dulceMayorService = dulceMayorService;
    }

    /** Número con 8 dígitos, como en los comprobantes: 00000012. */
    public static String numero(FacturaCliente factura) {
        return String.format("%08d", factura.getNumeroFactura());
    }

    /** Para descargar: controla que quien la pide pueda verla. */
    @Transactional(readOnly = true)
    public byte[] generar(String facturaId, UsuarioAutenticado usuario) {
        return generar(facturas.obtenerParaDescarga(facturaId, usuario));
    }

    /** Para uso interno del sistema (ej. el correo de BK-08): sin control de acceso. Llamar dentro de una transacción. */
    @Transactional(readOnly = true)
    public byte[] generar(FacturaCliente factura) {
        Pedido pedido = factura.getPedido();
        ClienteMinorista cliente = pedido.getCliente();
        DulceMayor sede = dulceMayorService.sedeCentral().orElse(null);

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, salida);
        writer.setPageEvent(new Fondo(factura.getEstado() == EstadoFactura.ANULADA));
        doc.open();

        encabezado(doc, writer, factura, sede);
        separador(doc, 14);
        cliente(doc, pedido, cliente);
        separador(doc, 14);
        detalle(doc, factura, pedido);

        Paragraph pie = new Paragraph("Comprobante interno de Dulce Mayor. No reemplaza la factura electrónica.", CHICA);
        pie.setSpacingBefore(24);
        doc.add(pie);
        doc.close();
        return salida.toByteArray();
    }

    // =====================================================
    // SECCIONES
    // =====================================================

    /** Logo y datos del emisor a la izquierda; tipo, número, fecha y estado del comprobante a la derecha. */
    private void encabezado(Document doc, PdfWriter writer, FacturaCliente factura, DulceMayor sede) {
        PdfPTable t = tabla(new float[]{0.9f, 4.1f, 3f});

        PdfPCell logo = sinBorde();
        logo.addElement(new Chunk(Image.getInstance(logo(writer, 44)), 0, 0, true));
        logo.setVerticalAlignment(Element.ALIGN_TOP);
        t.addCell(logo);

        PdfPCell emisor = sinBorde();
        emisor.addElement(new Paragraph(sede != null ? sede.getRazonSocial() : "Dulce Mayor", EMPRESA));
        if (sede != null) {
            emisor.addElement(new Paragraph("CUIT " + sede.getCuit(), NORMAL));
            emisor.addElement(new Paragraph(direccion(sede.getDireccion()), NORMAL));
        }
        emisor.addElement(new Paragraph("Mayorista de golosinas", CHICA));
        t.addCell(emisor);

        PdfPCell comprobante = sinBorde();
        Paragraph titulo = new Paragraph(espaciado("FACTURA", 2f, TITULO));
        titulo.setAlignment(Element.ALIGN_RIGHT);
        comprobante.addElement(titulo);
        comprobante.addElement(derecha("N° " + numero(factura), NEGRITA));
        comprobante.addElement(derecha("Fecha: " + factura.getFechaFactura().format(FECHA), NORMAL));
        comprobante.addElement(derecha("Estado: " + estado(factura.getEstado()),
                factura.getEstado() == EstadoFactura.ANULADA ? new Font(NUNITO_BOLD, 10, Font.NORMAL, MARCA) : NORMAL));
        t.addCell(comprobante);
        doc.add(t);
    }

    private void cliente(Document doc, Pedido pedido, ClienteMinorista cliente) {
        doc.add(new Paragraph(espaciado("CLIENTE", 1.2f, CABECERA)));
        boolean mismoNombre = cliente.getRazonSocial().equalsIgnoreCase(cliente.getNombreComercio());
        doc.add(new Paragraph(cliente.getRazonSocial() + (mismoNombre ? "" : " (" + cliente.getNombreComercio() + ")"), NEGRITA));
        doc.add(new Paragraph("CUIT " + cliente.getCuit() + "  ·  " + direccion(cliente.getDireccion()), NORMAL));
        doc.add(new Paragraph("Pedido del " + pedido.getFechaHoraCarga().format(FECHA) + "  ·  Preventista: "
                + pedido.getPreventista().getNombre() + " " + pedido.getPreventista().getApellido(), NORMAL));
    }

    /**
     * Detalle con el estilo del modelo: encabezados en color de marca y sin bordes verticales, la columna del
     * subtotal resaltada, filas limpias, y el total separado por una línea fina.
     */
    private void detalle(Document doc, FacturaCliente factura, Pedido pedido) {
        PdfPTable t = tabla(new float[]{1.1f, 5f, 2.2f, 2.2f});
        t.setSpacingBefore(4);
        t.addCell(cabecera("CANT.", Element.ALIGN_LEFT, false));
        t.addCell(cabecera("PRODUCTO", Element.ALIGN_LEFT, false));
        t.addCell(cabecera("PRECIO UNITARIO", Element.ALIGN_RIGHT, false));
        t.addCell(cabecera("SUBTOTAL", Element.ALIGN_CENTER, true));

        for (DetalleFactura d : factura.getDetalles()) {
            t.addCell(fila(String.valueOf(d.getCantidad()), Element.ALIGN_LEFT));
            t.addCell(fila(d.getProducto().getNombre(), Element.ALIGN_LEFT));
            t.addCell(fila(pesos(d.getPrecioUnitario()), Element.ALIGN_RIGHT));
            t.addCell(fila(pesos(d.getSubtotal()), Element.ALIGN_RIGHT));
        }

        // Subtotal y descuento: etiqueta en negrita alineada a la derecha, como en el modelo
        totalParcial(t, "Subtotal", pesos(factura.getSubtotal()), true);
        if (factura.getDescuento().signum() > 0) {
            String promo = pedido.getPromocion() != null ? " (código " + pedido.getPromocion().getCodigo() + ")" : "";
            totalParcial(t, "Descuento" + promo, "− " + pesos(factura.getDescuento()), false);
        }

        // Total: línea fina arriba, etiqueta a la izquierda y monto a la derecha, ambos espaciados y en color
        PdfPCell etiqueta = new PdfPCell(new Phrase(espaciado("Total Factura:", 1.2f, TOTAL)));
        etiqueta.setColspan(2);
        lineaArriba(etiqueta);
        PdfPCell monto = new PdfPCell(new Phrase(espaciado(pesos(factura.getTotal()), 1.2f, TOTAL)));
        monto.setColspan(2);
        monto.setHorizontalAlignment(Element.ALIGN_RIGHT);
        lineaArriba(monto);
        t.addCell(etiqueta);
        t.addCell(monto);
        doc.add(t);
    }

    // =====================================================
    // FONDO: franjas, marca de agua y logo (dibujo vectorial del caramelo del front)
    // =====================================================

    /** Se dibuja en cada página, por debajo del contenido. */
    private static final class Fondo extends PdfPageEventHelper {
        private final boolean anulada;

        Fondo(boolean anulada) {
            this.anulada = anulada;
        }

        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContentUnder();
            Rectangle pagina = document.getPageSize();

            // Franjas finas de color arriba y abajo
            cb.setColorFill(MARCA);
            cb.rectangle(0, pagina.getHeight() - 8, pagina.getWidth(), 8);
            cb.rectangle(0, 0, pagina.getWidth(), 4);
            cb.fill();

            // Marca de agua: un caramelo inclinado y casi transparente en la parte baja de la hoja
            // (debajo del detalle, para no tapar los totales de una factura corta)
            cb.saveState();
            PdfGState transparente = new PdfGState();
            transparente.setFillOpacity(0.06f);
            cb.setGState(transparente);
            float cx = pagina.getWidth() / 2, cy = pagina.getHeight() * 0.3f;
            double angulo = Math.toRadians(-18);
            float cos = (float) Math.cos(angulo), sin = (float) Math.sin(angulo);
            cb.concatCTM(cos, sin, -sin, cos, cx - cx * cos + cy * sin, cy - cx * sin - cy * cos);
            cb.setColorFill(MARCA);
            caramelo(cb, cx, cy, 70);
            cb.fill();
            cb.restoreState();

            if (anulada) {
                // Sello "ANULADA" cruzado sobre la hoja
                cb.saveState();
                PdfGState sello = new PdfGState();
                sello.setFillOpacity(0.25f);
                cb.setGState(sello);
                cb.beginText();
                cb.setFontAndSize(NUNITO_BOLD, 90);
                cb.setColorFill(MARCA);
                cb.showTextAligned(Element.ALIGN_CENTER, "ANULADA", cx, cy, 30);
                cb.endText();
                cb.restoreState();
            }
        }
    }

    /** Logo de la app: caramelo blanco sobre un cuadrado redondeado color marca. */
    private static PdfTemplate logo(PdfWriter writer, float lado) {
        PdfTemplate t = writer.getDirectContent().createTemplate(lado, lado);
        t.setColorFill(MARCA);
        t.roundRectangle(0, 0, lado, lado, lado * 0.24f);
        t.fill();
        t.setColorFill(Color.WHITE);
        caramelo(t, lado / 2, lado / 2, lado * 0.16f);
        t.fill();
        return t;
    }

    /** Caramelo: un círculo con un "moño" triangular a cada lado (agrega los trazos; quien llama los rellena). */
    private static void caramelo(PdfContentByte cb, float cx, float cy, float r) {
        cb.circle(cx, cy, r);
        // El moño nace en el borde del círculo (sin superponerse: con transparencia, la superposición deja un corte)
        // y no es más alto que el caramelo
        float punta = r, base = r * 2f, alto = r * 0.75f;
        cb.moveTo(cx - punta, cy);
        cb.lineTo(cx - base, cy + alto);
        cb.lineTo(cx - base, cy - alto);
        cb.closePath();
        cb.moveTo(cx + punta, cy);
        cb.lineTo(cx + base, cy + alto);
        cb.lineTo(cx + base, cy - alto);
        cb.closePath();
    }

    // =====================================================
    // AUXILIARES
    // =====================================================

    private static BaseFont fuente(String archivo) {
        try (InputStream in = FacturaPdf.class.getResourceAsStream("/fonts/" + archivo)) {
            if (in == null) {
                throw new IllegalStateException("Falta la fuente " + archivo + " en resources/fonts");
            }
            return BaseFont.createFont(archivo, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, in.readAllBytes(), null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static PdfPTable tabla(float[] anchos) {
        PdfPTable t = new PdfPTable(anchos);
        t.setWidthPercentage(100);
        return t;
    }

    private static PdfPCell sinBorde() {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    private static PdfPCell cabecera(String texto, int alineacion, boolean destacada) {
        PdfPCell c = new PdfPCell(new Phrase(texto, destacada ? CABECERA_DESTACADA : CABECERA));
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(MARCA_SUAVE);
        c.setHorizontalAlignment(alineacion);
        c.setPaddingTop(6);
        c.setPaddingBottom(7);
        c.setPaddingLeft(6);
        c.setPaddingRight(6);
        if (destacada) {
            c.setBackgroundColor(MARCA);
            c.setBorderColor(MARCA);
        }
        return c;
    }

    private static PdfPCell fila(String texto, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto, NORMAL));
        c.setBorder(Rectangle.NO_BORDER);
        c.setHorizontalAlignment(alineacion);
        c.setPadding(6);
        return c;
    }

    private static void totalParcial(PdfPTable t, String etiqueta, String valor, boolean primera) {
        PdfPCell e = new PdfPCell(new Phrase(etiqueta, NEGRITA));
        e.setColspan(3);
        e.setHorizontalAlignment(Element.ALIGN_RIGHT);
        e.setBorder(Rectangle.NO_BORDER);
        e.setPadding(6);
        PdfPCell v = new PdfPCell(new Phrase(valor, NORMAL));
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        v.setBorder(Rectangle.NO_BORDER);
        v.setPadding(6);
        if (primera) {
            e.setPaddingTop(12);
            v.setPaddingTop(12);
        }
        t.addCell(e);
        t.addCell(v);
    }

    private static void lineaArriba(PdfPCell c) {
        c.setBorder(Rectangle.TOP);
        c.setBorderColor(MARCA_SUAVE);
        c.setBorderWidth(1f);
        c.setPaddingTop(12);
        c.setPaddingLeft(6);
        c.setPaddingRight(6);
    }

    /** Separador horizontal fino en color de marca suave. */
    private static void separador(Document doc, float espacio) {
        PdfPTable linea = tabla(new float[]{1});
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(MARCA_SUAVE);
        c.setFixedHeight(1);
        linea.addCell(c);
        linea.setSpacingBefore(espacio / 2);
        linea.setSpacingAfter(espacio / 2);
        doc.add(linea);
    }

    private static Chunk espaciado(String texto, float espacio, Font fuente) {
        Chunk c = new Chunk(texto, fuente);
        c.setCharacterSpacing(espacio);
        return c;
    }

    private static Paragraph derecha(String texto, Font fuente) {
        Paragraph p = new Paragraph(texto, fuente);
        p.setAlignment(Element.ALIGN_RIGHT);
        return p;
    }

    public static String pesos(BigDecimal monto) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR")).format(monto);
    }

    private static String direccion(Direccion d) {
        if (d == null) return "";
        return d.getCalle() + " " + d.getNumeracion() + ", " + d.getLocalidadNombre() + ", " + d.getProvinciaNombre();
    }

    private static String estado(EstadoFactura estado) {
        return switch (estado) {
            case EMITIDA -> "Emitida (pendiente de pago)";
            case PAGADA -> "Pagada";
            case ANULADA -> "ANULADA";
        };
    }
}
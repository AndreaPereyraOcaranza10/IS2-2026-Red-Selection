package redSelection.dulceMayor.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redSelection.dulceMayor.enums.EstadoFactura;
import redSelection.dulceMayor.enums.TipoEmpresa;
import redSelection.dulceMayor.model.*;
import redSelection.dulceMayor.repository.DulceMayorRepository;
import redSelection.dulceMayor.security.UsuarioAutenticado;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * PDF de la factura (BK-05), generado con OpenPDF al momento de descargarla: así siempre refleja el estado actual
 * (por ejemplo, si se anuló). Es un comprobante interno: no reemplaza la factura electrónica de AFIP.
 */
@Service
public class FacturaPdf {

    private static final Color MARCA = new Color(194, 24, 91);
    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, MARCA);
    private static final Font NEGRITA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font CHICA = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
    private static final Font CABECERA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final FacturaClienteService facturas;
    private final DulceMayorRepository empresas;

    public FacturaPdf(FacturaClienteService facturas, DulceMayorRepository empresas) {
        this.facturas = facturas;
        this.empresas = empresas;
    }

    /** Número con 8 dígitos, como en los comprobantes: 00000012. */
    public static String numero(FacturaCliente factura) {
        return String.format("%08d", factura.getNumeroFactura());
    }

    @Transactional(readOnly = true)
    public byte[] generar(String facturaId, UsuarioAutenticado usuario) {
        FacturaCliente factura = facturas.obtenerParaDescarga(facturaId, usuario);
        Pedido pedido = factura.getPedido();
        ClienteMinorista cliente = pedido.getCliente();
        DulceMayor sede = empresas.findByEliminadoFalse().stream()
                .filter(e -> e.getTipoEmpresa() == TipoEmpresa.SEDE_CENTRAL).findFirst().orElse(null);

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(doc, salida);
        doc.open();

        // Encabezado: emisor a la izquierda, comprobante a la derecha
        PdfPTable encabezado = tabla(new float[]{3, 2});
        PdfPCell emisor = celdaSinBorde();
        emisor.addElement(new Paragraph(sede != null ? sede.getRazonSocial() : "Dulce Mayor", TITULO));
        if (sede != null) {
            emisor.addElement(new Paragraph("CUIT " + sede.getCuit(), NORMAL));
            emisor.addElement(new Paragraph(direccion(sede.getDireccion()), NORMAL));
        }
        encabezado.addCell(emisor);
        PdfPCell comprobante = celdaSinBorde();
        comprobante.addElement(new Paragraph("FACTURA N° " + numero(factura), NEGRITA));
        comprobante.addElement(new Paragraph("Fecha: " + factura.getFechaFactura().format(FECHA), NORMAL));
        comprobante.addElement(new Paragraph("Estado: " + estado(factura.getEstado()), NORMAL));
        encabezado.addCell(comprobante);
        doc.add(encabezado);

        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Cliente", NEGRITA));
        boolean mismoNombre = cliente.getRazonSocial().equalsIgnoreCase(cliente.getNombreComercio());
        doc.add(new Paragraph(cliente.getRazonSocial() + (mismoNombre ? "" : " (" + cliente.getNombreComercio() + ")"), NORMAL));
        doc.add(new Paragraph("CUIT " + cliente.getCuit() + " · " + direccion(cliente.getDireccion()), NORMAL));
        doc.add(new Paragraph("Pedido del " + pedido.getFechaHoraCarga().format(FECHA)
                + " · Preventista: " + pedido.getPreventista().getNombre() + " " + pedido.getPreventista().getApellido(), NORMAL));
        doc.add(new Paragraph(" "));

        // Detalle
        PdfPTable detalle = tabla(new float[]{1, 5, 2, 2});
        for (String titulo : new String[]{"Cant.", "Producto", "Precio unit.", "Subtotal"}) {
            PdfPCell c = new PdfPCell(new Phrase(titulo, CABECERA));
            c.setBackgroundColor(MARCA);
            c.setPadding(5);
            detalle.addCell(c);
        }
        for (DetalleFactura d : factura.getDetalles()) {
            detalle.addCell(celda(String.valueOf(d.getCantidad()), Element.ALIGN_CENTER));
            detalle.addCell(celda(d.getProducto().getNombre(), Element.ALIGN_LEFT));
            detalle.addCell(celda(pesos(d.getPrecioUnitario()), Element.ALIGN_RIGHT));
            detalle.addCell(celda(pesos(d.getSubtotal()), Element.ALIGN_RIGHT));
        }
        doc.add(detalle);

        // Totales
        PdfPTable totales = tabla(new float[]{7, 3});
        fila(totales, "Subtotal", pesos(factura.getSubtotal()), NORMAL);
        if (factura.getDescuento().signum() > 0) {
            String promo = pedido.getPromocion() != null ? " (código " + pedido.getPromocion().getCodigo() + ")" : "";
            fila(totales, "Descuento" + promo, "− " + pesos(factura.getDescuento()), NORMAL);
        }
        fila(totales, "TOTAL", pesos(factura.getTotal()), TITULO);
        doc.add(totales);

        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Comprobante interno de Dulce Mayor. No reemplaza la factura electrónica.", CHICA));
        doc.close();
        return salida.toByteArray();
    }

    private static PdfPTable tabla(float[] anchos) {
        PdfPTable t = new PdfPTable(anchos);
        t.setWidthPercentage(100);
        return t;
    }

    private static PdfPCell celdaSinBorde() {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    private static PdfPCell celda(String texto, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto, NORMAL));
        c.setHorizontalAlignment(alineacion);
        c.setPadding(5);
        return c;
    }

    private static void fila(PdfPTable t, String etiqueta, String valor, Font fuente) {
        PdfPCell e = new PdfPCell(new Phrase(etiqueta, fuente));
        e.setBorder(Rectangle.NO_BORDER);
        e.setHorizontalAlignment(Element.ALIGN_RIGHT);
        e.setPaddingTop(6);
        PdfPCell v = new PdfPCell(new Phrase(valor, fuente));
        v.setBorder(Rectangle.NO_BORDER);
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        v.setPaddingTop(6);
        t.addCell(e);
        t.addCell(v);
    }

    private static String pesos(BigDecimal monto) {
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

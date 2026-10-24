package carrito.datos;

import carrito.modelo.Articulo;
import carrito.modelo.Imagen;
import carrito.modelo.Proveedor;
import java.math.BigDecimal;
import java.util.List;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class CatalogoDemo {
    private static final Proveedor PROVEEDOR_NORTE = new Proveedor("prov-1", "Mercado Norte");
    private static final Proveedor PROVEEDOR_TECNO = new Proveedor("prov-2", "Tecno Sur");

    public static List<Articulo> crearArticulos() {
        return List.of(
                new Articulo("art-1", "Auriculares Studio", new BigDecimal("32500"), PROVEEDOR_TECNO,
                        List.of(new Imagen("img-1", "Auriculares Studio", "image/svg+xml",
                                imagenSvg("Studio", "#dce7ef", "#bd5d38")))),
                new Articulo("art-2", "Mochila Urbana", new BigDecimal("28900"), PROVEEDOR_NORTE,
                        List.of(new Imagen("img-2", "Mochila Urbana", "image/svg+xml",
                                imagenSvg("Urbana", "#e8dfd2", "#203b4f")))),
                new Articulo("art-3", "Termo Acero", new BigDecimal("18400"), PROVEEDOR_NORTE,
                        List.of(new Imagen("img-3", "Termo Acero", "image/svg+xml",
                                imagenSvg("Acero", "#dbe8dc", "#bd5d38")))),
                new Articulo("art-4", "Mouse Inalambrico", new BigDecimal("22100"), PROVEEDOR_TECNO,
                        List.of(new Imagen("img-4", "Mouse Inalambrico", "image/svg+xml",
                                imagenSvg("Mouse", "#eee4dc", "#203b4f")))));
    }

    private static String imagenSvg(String titulo, String color, String acento) {
        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 480">
                  <rect width="640" height="480" fill="%s"/>
                  <circle cx="120" cy="110" r="76" fill="%s" opacity=".75"/>
                  <rect x="175" y="145" width="290" height="210" rx="28" fill="#fff" opacity=".88"/>
                  <path d="M225 235h190M225 285h150" stroke="#203b4f" stroke-width="24" stroke-linecap="round"/>
                  <text x="320" y="405" text-anchor="middle" font-family="Arial" font-size="34" font-weight="700" fill="#203b4f">%s</text>
                </svg>
                """.formatted(color, acento, titulo);
        return "data:image/svg+xml;charset=UTF-8," + URLEncoder.encode(svg, StandardCharsets.UTF_8).replace("+", "%20");
    }

}

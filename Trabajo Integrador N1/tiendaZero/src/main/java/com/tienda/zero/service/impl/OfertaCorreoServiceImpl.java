package com.tienda.zero.service.impl;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import com.tienda.zero.enums.TipoCorreo;
import com.tienda.zero.enums.TipoUsuario;
import com.tienda.zero.model.Producto;
import com.tienda.zero.model.Usuario;
import com.tienda.zero.model.VigenciaPrecio;
import com.tienda.zero.repository.ProductoRepository;
import com.tienda.zero.repository.UsuarioRepository;
import com.tienda.zero.repository.VigenciaPrecioRepository;
import com.tienda.zero.service.ConfiguracionCorreoEmpresaService;
import com.tienda.zero.service.CorreoService;
import com.tienda.zero.service.OfertaCorreoService;

import jakarta.transaction.Transactional;

@Service
public class OfertaCorreoServiceImpl implements OfertaCorreoService {

    private static final String ASUNTO_POR_DEFECTO = "Ofertas destacadas de Zero";
    private static final String LOGO_SVG = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 742 741" width="96" height="96" role="img" aria-label="Zero" style="display: block; margin: 0 auto; width: 96px; height: 96px;">
              <path d="M452.24 423.96C446.15 423.97 440.05 423.98 433.96 423.99C423.27 422.74 413.12 420.23 403.46 415.44C397.88 412.67 392.8 409.17 388.26 404.89C354.3 372.96 373.94 327.28 386.22 290.34C389 281.98 391.56 273.62 394.31 265.25C395.47 261.71 398.58 256.53 397.74 252.85C359.72 283.93 321.69 315 283.67 346.07C283.4 345.8 283.14 345.54 282.87 345.27C290.18 339.22 296.57 330.61 302.82 323.45C312.38 312.51 322.51 302.05 332.11 291.16C356.83 263.13 383.23 236.57 407.78 208.41C421.39 192.81 435.16 175.47 451.71 162.92C485.12 137.6 532.96 125.14 570.75 148.6C591.83 161.69 602.32 189.54 598.58 213.6C596.13 229.35 591.15 244.32 586.01 259.35C579.39 278.7 573.77 298.29 567.09 317.62C563.19 328.89 560.34 340.72 554.78 351.38C540.54 378.62 516.79 403.65 487.75 415C476.16 419.53 464.65 422.57 452.24 423.96ZM328.35 178.26C282.76 178.23 237.18 178.21 191.59 178.18C190.49 175.17 193.04 168.99 193.93 165.88C196.33 157.57 196.99 148.75 199.53 140.48C278.42 140.48 357.31 140.48 436.2 140.48C412.33 154.6 395.5 178.51 376.89 198.52C339.49 238.75 303.37 280.22 265.77 320.28C252.68 334.23 240.51 349.17 227.16 362.89C223.14 367.02 219.44 371.45 215.56 375.69C213.59 377.85 210.59 380.06 209.95 382.85C254.49 382.99 299.04 383.13 343.58 383.28C347.2 394.78 352.85 405.95 362.35 413.82C365.37 416.33 368.75 418.31 371.79 420.76C295.62 420.76 219.44 420.76 143.26 420.76C145.11 412.46 147.57 404.2 149.94 396.09C151.03 392.37 151.26 387.82 153.27 384.42C156.46 379.03 162.38 374.62 166.54 369.96C177.71 357.46 189.39 345.43 200.58 332.95C228.74 301.55 257.65 270.83 285.87 239.48C299.44 224.42 313.48 209.77 327.1 194.76C330.12 191.43 340.55 182.09 340.91 178.39C336.72 178.35 332.54 178.3 328.35 178.26ZM509.56 174.24C483.62 178.46 463.47 197.2 451.98 220.1C445.39 233.22 442.41 248.19 437.67 262.05C431.77 279.33 426.79 296.99 420.77 314.24C412.78 337.13 402.28 372.21 432.7 384.11C442.37 387.89 452.92 387.72 462.87 385.46C510.46 374.67 522.37 328.86 534.25 288.12C536.98 278.73 540.1 269.22 543.33 259.96C548.81 244.25 556.65 225.03 555.97 208.18C554.95 182.84 533.3 170.38 509.56 174.24ZM571.15 475.48C598.63 470.85 633.12 484.33 645.61 510.35C663.91 548.48 644.19 593.59 602.15 603.05C589.13 605.98 574.67 606.25 561.69 603.02C514.94 591.39 495.23 536.58 526.15 498.73C537.31 485.07 554.3 478.32 571.15 475.48ZM90.02 603.07C89.96 596.8 89.9 590.53 89.84 584.26C93.57 577.13 100.84 570.86 106.12 564.81C117.92 551.31 129.56 537.67 141.2 524.06C145.56 518.97 150.01 513.92 154.43 508.88C156.38 506.65 159.9 503.73 160.35 500.99C137.63 500.78 114.92 500.57 92.2 500.36C92.27 492.68 92.34 484.99 92.41 477.3C128.52 477.37 164.63 477.44 200.73 477.51C201.09 481.68 201.95 491.73 200.64 495.47C198.94 500.34 190.59 507.47 187.1 511.68C175.13 526.09 162.81 540.21 150.61 554.44C145.72 560.15 140.76 565.84 135.88 571.56C133.87 573.91 131.09 576.04 130.94 579.18C155.01 579.41 179.07 579.63 203.13 579.86C203.08 587.53 203.03 595.2 202.99 602.88C165.33 602.94 127.67 603.01 90.02 603.07ZM334.1 477.24C334.1 484.89 334.1 492.54 334.1 500.19C310.74 500.19 287.37 500.19 264.01 500.19C264.01 509.23 264.01 518.28 264.01 527.32C284.67 527.32 305.33 527.32 325.99 527.32C325.99 534.93 325.99 542.53 325.99 550.14C305.33 550.14 284.67 550.14 264.01 550.14C264.01 559.88 264.01 569.62 264.01 579.35C288.32 579.35 312.63 579.35 336.95 579.35C336.95 587.29 336.95 595.23 336.95 603.16C302.66 603.16 268.38 603.16 234.1 603.16C234.1 561.19 234.1 519.22 234.1 477.24C267.43 477.24 300.77 477.24 334.1 477.24ZM458.36 561.58C467.78 575.34 477.19 589.11 486.6 602.88C479.07 604.26 470.41 603.18 462.73 603.18C460.3 603.18 455.88 604.03 453.76 602.69C451.29 601.13 449.44 596.81 447.78 594.44C443.58 588.47 439.31 582.51 435.35 576.37C433.85 574.03 431.36 568.5 428.77 567.35C426.61 566.4 423.09 567.05 420.76 567.05C414.19 567.05 406.79 566.32 400.38 567.67C400.38 579.5 400.38 591.32 400.38 603.15C390.46 603.15 380.55 603.15 370.63 603.15C370.63 561.31 370.63 519.47 370.63 477.63C380.23 475.82 391.33 477.24 401.13 477.24C421.1 477.22 444.35 474.01 462.31 484.5C484.58 497.51 490.59 527.39 475.52 548.32C471.17 554.37 464.81 558.17 458.36 561.58ZM542.07 534.14C542.07 537.98 542.07 541.82 542.07 545.66C543.06 552.55 545.4 559.41 549.72 564.99C571.97 593.74 620.86 579.89 622.04 542.62C622.28 535.11 621.03 528 617.78 521.22C601.14 486.48 547.6 495.71 542.07 534.14ZM401.35 500.76C399.91 503.47 400.6 507.68 400.6 510.8C400.6 518.02 400.62 525.24 400.62 532.47C400.62 535.69 399.81 540.48 401.38 543.17C411.58 543.18 421.78 543.18 431.98 543.18C444.54 542.11 453.5 533.9 453.2 520.96C453.09 516.11 451.54 511.09 448.02 507.58C443.01 502.57 435.97 501.17 429.19 500.77C419.91 500.77 410.63 500.77 401.35 500.76Z" fill="#010101" fill-rule="evenodd" stroke="#010101" stroke-width="0.25" stroke-linejoin="round"/>
              <path d="M191.59 178.18C237.18 178.21 282.76 178.23 328.35 178.26C282.76 178.23 237.18 178.21 191.59 178.18ZM282.87 345.27C283.14 345.54 283.4 345.8 283.67 346.07C282.73 347.31 282.41 347.62 280.96 347.64C281.6 346.85 282.24 346.06 282.87 345.27ZM433.96 423.99C440.05 423.98 446.15 423.97 452.24 423.96C446.15 423.97 440.05 423.98 433.96 423.99ZM92.2 500.36C114.92 500.57 137.63 500.78 160.35 500.99C143.79 500.99 127.24 500.99 110.69 500.99C106.8 500.99 94.82 502.1 92.2 500.36ZM429.19 500.77C419.91 500.77 410.63 500.77 401.35 500.76C410.63 500.77 419.91 500.77 429.19 500.77ZM542.07 534.14C542.07 537.98 542.07 541.82 542.07 545.66C542.07 541.82 542.07 537.98 542.07 534.14ZM401.38 543.17C411.58 543.18 421.78 543.18 431.98 543.18C421.78 543.18 411.58 543.18 401.38 543.17ZM89.84 584.26C89.9 590.53 89.96 596.8 90.02 603.07C88.84 601.41 88.86 586.48 89.84 584.26Z" fill="#ffffff" fill-rule="evenodd" stroke="#ffffff" stroke-width="0.25" stroke-linejoin="round"/>
            </svg>
            """;
    private static final String LOGO_HTML = """
            <img src="cid:logoZero" width="96" height="96" alt="Zero"
                 style="display: block; width: 96px; height: 96px; margin: 0 auto; border: 0;">
            """;
    private static final Pattern SVG_INLINE = Pattern.compile("(?is)(?:<\\?xml[^>]*\\?>\\s*)?<svg\\b[^>]*>.*?</svg>");

    private static final String CUERPO_POR_DEFECTO = """
            <html>
              <body style="font-family: Arial, sans-serif; color: #202020; margin: 0; padding: 0; background: #f4f4f4;">
                <div style="display: none; max-height: 0; overflow: hidden;">Ofertas seleccionadas de Zero.</div>
                <div style="max-width: 680px; margin: 0 auto; background: #ffffff;">
                  <div style="padding: 24px 28px; text-align: center; border-bottom: 1px solid #eeeeee;">
                    <a href="{{LINK_TIENDA}}" style="display: inline-block; text-decoration: none;">
                      <?xml version="1.0" encoding="UTF-8"?>
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 742 741" width="742" height="741">
                        <path d="M452.24 423.96C446.15 423.97 440.05 423.98 433.96 423.99C423.27 422.74 413.12 420.23 403.46 415.44C397.88 412.67 392.8 409.17 388.26 404.89C354.3 372.96 373.94 327.28 386.22 290.34C389 281.98 391.56 273.62 394.31 265.25C395.47 261.71 398.58 256.53 397.74 252.85C359.72 283.93 321.69 315 283.67 346.07C283.4 345.8 283.14 345.54 282.87 345.27C290.18 339.22 296.57 330.61 302.82 323.45C312.38 312.51 322.51 302.05 332.11 291.16C356.83 263.13 383.23 236.57 407.78 208.41C421.39 192.81 435.16 175.47 451.71 162.92C485.12 137.6 532.96 125.14 570.75 148.6C591.83 161.69 602.32 189.54 598.58 213.6C596.13 229.35 591.15 244.32 586.01 259.35C579.39 278.7 573.77 298.29 567.09 317.62C563.19 328.89 560.34 340.72 554.78 351.38C540.54 378.62 516.79 403.65 487.75 415C476.16 419.53 464.65 422.57 452.24 423.96ZM328.35 178.26C282.76 178.23 237.18 178.21 191.59 178.18C190.49 175.17 193.04 168.99 193.93 165.88C196.33 157.57 196.99 148.75 199.53 140.48C278.42 140.48 357.31 140.48 436.2 140.48C412.33 154.6 395.5 178.51 376.89 198.52C339.49 238.75 303.37 280.22 265.77 320.28C252.68 334.23 240.51 349.17 227.16 362.89C223.14 367.02 219.44 371.45 215.56 375.69C213.59 377.85 210.59 380.06 209.95 382.85C254.49 382.99 299.04 383.13 343.58 383.28C347.2 394.78 352.85 405.95 362.35 413.82C365.37 416.33 368.75 418.31 371.79 420.76C295.62 420.76 219.44 420.76 143.26 420.76C145.11 412.46 147.57 404.2 149.94 396.09C151.03 392.37 151.26 387.82 153.27 384.42C156.46 379.03 162.38 374.62 166.54 369.96C177.71 357.46 189.39 345.43 200.58 332.95C228.74 301.55 257.65 270.83 285.87 239.48C299.44 224.42 313.48 209.77 327.1 194.76C330.12 191.43 340.55 182.09 340.91 178.39C336.72 178.35 332.54 178.3 328.35 178.26ZM509.56 174.24C483.62 178.46 463.47 197.2 451.98 220.1C445.39 233.22 442.41 248.19 437.67 262.05C431.77 279.33 426.79 296.99 420.77 314.24C412.78 337.13 402.28 372.21 432.7 384.11C442.37 387.89 452.92 387.72 462.87 385.46C510.46 374.67 522.37 328.86 534.25 288.12C536.98 278.73 540.1 269.22 543.33 259.96C548.81 244.25 556.65 225.03 555.97 208.18C554.95 182.84 533.3 170.38 509.56 174.24ZM571.15 475.48C598.63 470.85 633.12 484.33 645.61 510.35C663.91 548.48 644.19 593.59 602.15 603.05C589.13 605.98 574.67 606.25 561.69 603.02C514.94 591.39 495.23 536.58 526.15 498.73C537.31 485.07 554.3 478.32 571.15 475.48ZM90.02 603.07C89.96 596.8 89.9 590.53 89.84 584.26C93.57 577.13 100.84 570.86 106.12 564.81C117.92 551.31 129.56 537.67 141.2 524.06C145.56 518.97 150.01 513.92 154.43 508.88C156.38 506.65 159.9 503.73 160.35 500.99C137.63 500.78 114.92 500.57 92.2 500.36C92.27 492.68 92.34 484.99 92.41 477.3C128.52 477.37 164.63 477.44 200.73 477.51C201.09 481.68 201.95 491.73 200.64 495.47C198.94 500.34 190.59 507.47 187.1 511.68C175.13 526.09 162.81 540.21 150.61 554.44C145.72 560.15 140.76 565.84 135.88 571.56C133.87 573.91 131.09 576.04 130.94 579.18C155.01 579.41 179.07 579.63 203.13 579.86C203.08 587.53 203.03 595.2 202.99 602.88C165.33 602.94 127.67 603.01 90.02 603.07ZM334.1 477.24C334.1 484.89 334.1 492.54 334.1 500.19C310.74 500.19 287.37 500.19 264.01 500.19C264.01 509.23 264.01 518.28 264.01 527.32C284.67 527.32 305.33 527.32 325.99 527.32C325.99 534.93 325.99 542.53 325.99 550.14C305.33 550.14 284.67 550.14 264.01 550.14C264.01 559.88 264.01 569.62 264.01 579.35C288.32 579.35 312.63 579.35 336.95 579.35C336.95 587.29 336.95 595.23 336.95 603.16C302.66 603.16 268.38 603.16 234.1 603.16C234.1 561.19 234.1 519.22 234.1 477.24C267.43 477.24 300.77 477.24 334.1 477.24ZM458.36 561.58C467.78 575.34 477.19 589.11 486.6 602.88C479.07 604.26 470.41 603.18 462.73 603.18C460.3 603.18 455.88 604.03 453.76 602.69C451.29 601.13 449.44 596.81 447.78 594.44C443.58 588.47 439.31 582.51 435.35 576.37C433.85 574.03 431.36 568.5 428.77 567.35C426.61 566.4 423.09 567.05 420.76 567.05C414.19 567.05 406.79 566.32 400.38 567.67C400.38 579.5 400.38 591.32 400.38 603.15C390.46 603.15 380.55 603.15 370.63 603.15C370.63 561.31 370.63 519.47 370.63 477.63C380.23 475.82 391.33 477.24 401.13 477.24C421.1 477.22 444.35 474.01 462.31 484.5C484.58 497.51 490.59 527.39 475.52 548.32C471.17 554.37 464.81 558.17 458.36 561.58ZM542.07 534.14C542.07 537.98 542.07 541.82 542.07 545.66C543.06 552.55 545.4 559.41 549.72 564.99C571.97 593.74 620.86 579.89 622.04 542.62C622.28 535.11 621.03 528 617.78 521.22C601.14 486.48 547.6 495.71 542.07 534.14ZM401.35 500.76C399.91 503.47 400.6 507.68 400.6 510.8C400.6 518.02 400.62 525.24 400.62 532.47C400.62 535.69 399.81 540.48 401.38 543.17C411.58 543.18 421.78 543.18 431.98 543.18C444.54 542.11 453.5 533.9 453.2 520.96C453.09 516.11 451.54 511.09 448.02 507.58C443.01 502.57 435.97 501.17 429.19 500.77C419.91 500.77 410.63 500.77 401.35 500.76Z" fill="#010101" fill-rule="evenodd" stroke="#010101" stroke-width="0.25" stroke-linejoin="round"/>
                        <path d="M191.59 178.18C237.18 178.21 282.76 178.23 328.35 178.26C282.76 178.23 237.18 178.21 191.59 178.18ZM282.87 345.27C283.14 345.54 283.4 345.8 283.67 346.07C282.73 347.31 282.41 347.62 280.96 347.64C281.6 346.85 282.24 346.06 282.87 345.27ZM433.96 423.99C440.05 423.98 446.15 423.97 452.24 423.96C446.15 423.97 440.05 423.98 433.96 423.99ZM92.2 500.36C114.92 500.57 137.63 500.78 160.35 500.99C143.79 500.99 127.24 500.99 110.69 500.99C106.8 500.99 94.82 502.1 92.2 500.36ZM429.19 500.77C419.91 500.77 410.63 500.77 401.35 500.76C410.63 500.77 419.91 500.77 429.19 500.77ZM542.07 534.14C542.07 537.98 542.07 541.82 542.07 545.66C542.07 541.82 542.07 537.98 542.07 534.14ZM401.38 543.17C411.58 543.18 421.78 543.18 431.98 543.18C421.78 543.18 411.58 543.18 401.38 543.17ZM89.84 584.26C89.9 590.53 89.96 596.8 90.02 603.07C88.84 601.41 88.86 586.48 89.84 584.26Z" fill="#ffffff" fill-rule="evenodd" stroke="#ffffff" stroke-width="0.25" stroke-linejoin="round"/>
                      </svg>
                    </a>
                  </div>
                  <div style="padding: 28px;">
                    <h2 style="margin: 0 0 10px; font-size: 26px; line-height: 1.25; color: #111111;">Ofertas destacadas de Zero</h2>
                    <p style="margin: 0 0 22px; font-size: 15px; line-height: 1.6; color: #555555;">Hola, tenemos estas ofertas seleccionadas para vos:</p>
                    <p style="margin: 0 0 26px;">
                      <a href="{{LINK_TIENDA}}" style="display: inline-block; background: #111111; color: #ffffff; text-decoration: none; padding: 12px 20px; border-radius: 4px; font-weight: 700;">Ir a la tienda</a>
                    </p>
                  </div>
                  {{OFERTAS}}
                  <div style="padding: 26px 28px 32px; text-align: center; border-top: 1px solid #eeeeee;">
                    <p style="margin: 0 0 14px; color: #555555;">Te esperamos en nuestra tienda.</p>
                    <a href="{{LINK_TIENDA}}" style="color: #111111; font-weight: 700;">Ver todas las ofertas</a>
                  </div>
                </div>
              </body>
            </html>
            """;

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final VigenciaPrecioRepository vigenciaPrecioRepository;
    private final ConfiguracionCorreoEmpresaService configuracionCorreoEmpresaService;
    private final CorreoService correoService;
    private final int cantidadMaximaOfertas;
    private final String tiendaUrl;

    public OfertaCorreoServiceImpl(UsuarioRepository usuarioRepository,
                                   ProductoRepository productoRepository,
                                   VigenciaPrecioRepository vigenciaPrecioRepository,
                                   ConfiguracionCorreoEmpresaService configuracionCorreoEmpresaService,
                                   CorreoService correoService,
                                   @Value("${app.ofertas.mail.max-productos:6}") int cantidadMaximaOfertas,
                                   @Value("${app.tienda.url:http://localhost:8080}") String tiendaUrl) {
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.vigenciaPrecioRepository = vigenciaPrecioRepository;
        this.configuracionCorreoEmpresaService = configuracionCorreoEmpresaService;
        this.correoService = correoService;
        this.cantidadMaximaOfertas = cantidadMaximaOfertas;
        this.tiendaUrl = limpiarUrlBase(tiendaUrl);
    }

    @Override
    @Transactional
    public void enviarOfertasAClientesRegistrados() {
        List<Producto> productosEnOferta = productoRepository.findByEnOfertaTrueAndEliminadoFalse();
        if (productosEnOferta.isEmpty()) {
            return;
        }

        List<Usuario> destinatarios = usuarioRepository
                .findByRolAndCuentaActivadaTrueAndEliminadoFalse(TipoUsuario.CLIENTE);
        if (destinatarios.isEmpty()) {
            return;
        }

        String asunto = ASUNTO_POR_DEFECTO;
        String cuerpo = CUERPO_POR_DEFECTO;

        var configuracion = configuracionCorreoEmpresaService.buscarActivaPorTipo(TipoCorreo.OFERTA);
        if (configuracion.isPresent()) {
            asunto = configuracion.get().getAsunto();
            cuerpo = configuracion.get().getCuerpoHtml();
        }

        String ofertasHtml = construirBloqueOfertas(productosEnOferta);
        String cuerpoHtml = SVG_INLINE.matcher(cuerpo)
                .replaceAll(Matcher.quoteReplacement(LOGO_HTML))
                .replace("{{OFERTAS}}", ofertasHtml)
                .replace("{{LINK_TIENDA}}", escapar(urlTienda()))
                .replace("{{LOGO_HTML}}", LOGO_HTML);

        for (Usuario destinatario : destinatarios) {
            correoService.enviarCorreoHtmlConImagenInline(destinatario.getNombreUsuario(), asunto, cuerpoHtml,
                    "logoZero", new ClassPathResource("static/tienda/images/logo-zero-black.png"), "image/png");
        }
    }

    private String construirBloqueOfertas(List<Producto> productosEnOferta) {
        StringBuilder html = new StringBuilder("""
                <div style="padding: 0 28px 8px;">
                <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="border-collapse: collapse;">
                """);

        productosEnOferta.stream()
                .limit(cantidadMaximaOfertas)
                .forEach(producto -> agregarFilaOferta(html, producto));

        html.append("</table></div>");
        return html.toString();
    }

    private void agregarFilaOferta(StringBuilder html, Producto producto) {
        String descripcion = producto.getDescripcion() == null ? "" : producto.getDescripcion();
        String talle = producto.getTalle() == null ? "" : "Talle: " + producto.getTalle();
        String precio = obtenerPrecio(producto);
        String productoUrl = urlAbsoluta("/product/" + producto.getId());

        html.append("""
                  <tr>
                    <td style="padding: 18px 0; border-top: 1px solid #e5e5e5;">
                      <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="border-collapse: collapse;">
                        <tr>
                          <td valign="top" style="padding: 2px 0 0;">
                            <a href="
                """)
                .append(escapar(productoUrl))
                .append("""
                " style="font-size: 18px; line-height: 1.35; color: #111111; text-decoration: none; font-weight: 700;">
                """)
                .append(escapar(producto.getNombre()))
                .append("""
                            </a>
                """);

        if (!descripcion.isBlank()) {
            html.append("<p style=\"margin: 8px 0 0; color: #555555; font-size: 14px; line-height: 1.5;\">")
                    .append(escapar(descripcion))
                    .append("</p>");
        }

        if (!talle.isBlank()) {
            html.append("<p style=\"margin: 8px 0 0; color: #777777; font-size: 13px; line-height: 1.4;\">")
                    .append(escapar(talle))
                    .append("</p>");
        }

        if (precio != null) {
            html.append("<p style=\"margin: 10px 0 0; font-size: 20px; font-weight: 700; color: #111111;\">")
                    .append(escapar(precio))
                    .append("</p>");
        }

        html.append("""
                            <p style="margin: 12px 0 0;">
                              <a href="
                """)
                .append(escapar(productoUrl))
                .append("""
                " style="color: #111111; font-size: 13px; font-weight: 700;">Ver producto</a>
                            </p>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                """);
    }

    private String obtenerPrecio(Producto producto) {
        VigenciaPrecio vigencia = vigenciaPrecioRepository
                .findByProductoIdAndFechaHastaIsNullAndEliminadoFalse(producto.getId());
        if (vigencia == null) {
            return null;
        }
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"));
        return formatoMoneda.format(vigencia.getPrecio());
    }

    private String urlTienda() {
        return urlAbsoluta("/shop");
    }

    private String urlAbsoluta(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            return tiendaUrl;
        }
        if (ruta.startsWith("http://") || ruta.startsWith("https://")) {
            return ruta;
        }
        return tiendaUrl + (ruta.startsWith("/") ? ruta : "/" + ruta);
    }

    private String limpiarUrlBase(String url) {
        String base = (url == null || url.isBlank()) ? "http://localhost:8080" : url.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base;
    }

    private String escapar(String texto) {
        return HtmlUtils.htmlEscape(texto);
    }
}

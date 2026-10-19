import os
import sys
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
)
from reportlab.pdfgen import canvas

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super(NumberedCanvas, self).__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            canvas.Canvas.showPage(self)
        canvas.Canvas.save(self)

    def draw_page_decorations(self, page_count):
        if self._pageNumber > 1:
            self.saveState()
            self.setFont("Helvetica", 8)
            self.setFillColor(colors.HexColor("#64748B"))
            
            # Header
            self.drawString(54, 750, "ShopEase — Manual de Arquitectura, Funcionamiento y Conceptos")
            self.setStrokeColor(colors.HexColor("#CBD5E1"))
            self.setLineWidth(0.5)
            self.line(54, 744, 558, 744)
            
            # Footer
            page_text = f"Página {self._pageNumber} de {page_count}"
            self.drawRightString(558, 36, page_text)
            self.drawString(54, 36, "Proyecto Full-Stack E-Commerce (ReactJS + Spring Boot + MySQL)")
            self.line(54, 46, 558, 46)
            self.restoreState()

def create_pdf(filename):
    doc = SimpleDocTemplate(
        filename,
        pagesize=letter,
        leftMargin=54,
        rightMargin=54,
        topMargin=54,
        bottomMargin=54
    )

    styles = getSampleStyleSheet()

    # Custom styles
    primary_color = colors.HexColor("#1E3A8A")   # Deep Blue
    secondary_color = colors.HexColor("#0D9488") # Teal
    dark_text = colors.HexColor("#0F172A")       # Slate 900
    body_text_color = colors.HexColor("#334155") # Slate 700
    light_bg = colors.HexColor("#F8FAFC")        # Slate 50
    code_bg = colors.HexColor("#F1F5F9")         # Slate 100
    border_color = colors.HexColor("#E2E8F0")

    title_style = ParagraphStyle(
        'CoverTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=26,
        leading=32,
        textColor=primary_color,
        alignment=1, # Center
        spaceAfter=12
    )

    subtitle_style = ParagraphStyle(
        'CoverSubtitle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=13,
        leading=18,
        textColor=secondary_color,
        alignment=1,
        spaceAfter=25
    )

    meta_style = ParagraphStyle(
        'CoverMeta',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.5,
        leading=14,
        textColor=body_text_color,
        alignment=1
    )

    h1_style = ParagraphStyle(
        'Heading1_Custom',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=16,
        leading=20,
        textColor=primary_color,
        spaceBefore=16,
        spaceAfter=8,
        keepWithNext=True
    )

    h2_style = ParagraphStyle(
        'Heading2_Custom',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=12,
        leading=16,
        textColor=secondary_color,
        spaceBefore=12,
        spaceAfter=6,
        keepWithNext=True
    )

    body_style = ParagraphStyle(
        'Body_Custom',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.5,
        leading=14,
        textColor=body_text_color,
        spaceAfter=7
    )

    bullet_style = ParagraphStyle(
        'Bullet_Custom',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.5,
        leading=13.5,
        textColor=body_text_color,
        leftIndent=15,
        spaceAfter=4
    )

    code_style = ParagraphStyle(
        'Code_Custom',
        parent=styles['Normal'],
        fontName='Courier',
        fontSize=8.5,
        leading=11.5,
        textColor=colors.HexColor("#0F172A")
    )

    box_text_style = ParagraphStyle(
        'BoxText',
        parent=styles['Normal'],
        fontName='Helvetica-Oblique',
        fontSize=9,
        leading=13,
        textColor=colors.HexColor("#1E293B")
    )

    table_header_style = ParagraphStyle(
        'TableHeader',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=9,
        leading=12,
        textColor=colors.white
    )

    table_cell_style = ParagraphStyle(
        'TableCell',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=8.5,
        leading=11.5,
        textColor=body_text_color
    )

    story = []

    # ==========================
    # PORTADA
    # ==========================
    story.append(Spacer(1, 40))
    story.append(Paragraph("🛒 SHOPEASE", title_style))
    story.append(Paragraph("Manual Integral de Arquitectura, Funcionamiento y Conceptos de Software", subtitle_style))
    story.append(HRFlowable(width="80%", thickness=2, color=secondary_color, spaceAfter=20))
    
    cover_info = (
        "<b>Proyecto:</b> Full-Stack E-Commerce Web Platform<br/>"
        "<b>Tutorial de Referencia:</b> The Code Reveal (YouTube: p8tBD3YTCPo)<br/>"
        "<b>Frontend:</b> React.js 18, Redux Toolkit, Tailwind CSS, React Router v6, Axios<br/>"
        "<b>Backend:</b> Spring Boot 3.3.2, Spring Security 6, JWT, Spring Data JPA, Hibernate<br/>"
        "<b>Base de Datos:</b> MySQL 8.0 (Motor InnoDB)<br/>"
        "<b>Entorno:</b> Java 22 / Node.js v22 / Maven Wrapper / Windows"
    )
    story.append(Paragraph(cover_info, meta_style))
    story.append(Spacer(1, 35))

    # Resumen Ejecutivo Box
    exec_summary = (
        "<b>Resumen Ejecutivo:</b><br/>"
        "Este documento recopila la visión conceptual, arquitectura técnica, modelado de datos, "
        "mecanismos de seguridad y decisiones de diseño implementadas para el proyecto <b>ShopEase</b>. "
        "Abarca desde la concepción del frontend en React hasta la persistencia relacional en MySQL 8, "
        "detallando los patrones de diseño y los desafíos de ingeniería resueltos durante la integración."
    )
    summary_table = Table([[Paragraph(exec_summary, box_text_style)]], colWidths=[500])
    summary_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#EFF6FF")),
        ('BOX', (0,0), (-1,-1), 1, colors.HexColor("#93C5FD")),
        ('PADDING', (0,0), (-1,-1), 12),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ]))
    story.append(summary_table)
    story.append(PageBreak())

    # ===================================
    # SECCIÓN 1: INTRODUCCIÓN Y ALCANCE
    # ===================================
    story.append(Paragraph("1. Introducción y Contexto del Proyecto", h1_style))
    story.append(Paragraph(
        "El proyecto <b>ShopEase</b> es una solución integral de comercio electrónico de arquitectura "
        "moderna desacoplada, creada con el propósito de aplicar las mejores prácticas de la "
        "Ingeniería de Software contemporánea.",
        body_style
    ))
    story.append(Paragraph(
        "<b>Origen y Tutorial:</b> La base del desarrollo proviene de la serie de tutoriales producida por "
        "el canal <i>The Code Reveal</i>. El video inicial de referencia (ID: <code>p8tBD3YTCPo</code>) "
        "establece los cimientos del cliente web: estructura de componentes funcionales, maquetación "
        "estética mediante Tailwind CSS, tipografía personalizada y componentes visuales primarios "
        "(Navbar, Hero Section, Carousel de productos, Secciones por género y Footer).",
        body_style
    ))
    story.append(Paragraph(
        "<b>Evolución a Full-Stack:</b> Posteriormente, el sistema se completó agregando un backend robusto "
        "con Spring Boot, seguridad por tokens JWT, persistencia de datos relacional y un panel de administración "
        "para el control de inventario y pedidos.",
        body_style
    ))

    # ========================================================
    # SECCIÓN 2: ARQUITECTURA GLOBAL Y PATRONES DE DISEÑO
    # ========================================================
    story.append(Spacer(1, 10))
    story.append(Paragraph("2. Arquitectura Global del Sistema", h1_style))
    story.append(Paragraph(
        "ShopEase sigue una arquitectura <b>Cliente-Servidor Desacoplada (Decoupled Client-Server Architecture)</b> "
        "utilizando el paradigma de <b>Single Page Application (SPA)</b> en el frontend y una "
        "<b>RESTful API</b> sin estado (Stateless) en el backend.",
        body_style
    ))

    # Tabla Arquitectura
    arch_data = [
        [Paragraph("Capa", table_header_style), Paragraph("Tecnología", table_header_style), Paragraph("Responsabilidad y Rol", table_header_style)],
        [
            Paragraph("<b>Frontend (UI)</b>", table_cell_style),
            Paragraph("React.js 18<br/>Tailwind CSS<br/>Redux Toolkit", table_cell_style),
            Paragraph("Renderizado de interfaces, interacción con el usuario, routing del lado del cliente, gestión de estado de carrito y sesión.", table_cell_style)
        ],
        [
            Paragraph("<b>Backend (API)</b>", table_cell_style),
            Paragraph("Spring Boot 3.3.2<br/>Spring Security 6<br/>JPA / Hibernate", table_cell_style),
            Paragraph("Exposición de endpoints REST, lógica de negocio, validaciones, generación y validación de tokens JWT, transacciones ACID.", table_cell_style)
        ],
        [
            Paragraph("<b>Base de Datos</b>", table_cell_style),
            Paragraph("MySQL 8.0<br/>(Motor InnoDB)", table_cell_style),
            Paragraph("Almacenamiento persistente, integridad referencial, claves foráneas, índices de unicidad y soporte transaccional completo.", table_cell_style)
        ]
    ]
    t_arch = Table(arch_data, colWidths=[90, 120, 290])
    t_arch.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), primary_color),
        ('ALIGN', (0,0), (-1,-1), 'LEFT'),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
        ('GRID', (0,0), (-1,-1), 0.5, border_color),
        ('BACKGROUND', (0,1), (-1,1), light_bg),
        ('BACKGROUND', (0,3), (-1,3), light_bg),
        ('PADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(t_arch)
    story.append(Spacer(1, 12))

    story.append(Paragraph("Patrones de Diseño Empleados:", h2_style))
    story.append(Paragraph("• <b>MVC (Model-View-Controller) / Controlador-Servicio-Repositorio:</b> Separación estricta en el backend entre puntos de entrada HTTP (Controllers), reglas operativas (Services) y persistencia (Repositories).", bullet_style))
    story.append(Paragraph("• <b>Data Transfer Object (DTO):</b> Desacoplamiento entre las entidades persistentes de la base de datos y los objetos serializados en formato JSON transmitidos por la red.", bullet_style))
    story.append(Paragraph("• <b>Inversión de Control (IoC) e Inyección de Dependencias (DI):</b> Spring administra el ciclo de vida de los beans y los inyecta mediante <code>@Autowired</code>.", bullet_style))
    story.append(Paragraph("• <b>Patrón Builder:</b> Construcción fluida y legible de objetos inmutables mediante anotaciones <code>@Builder</code> de Lombok.", bullet_style))
    story.append(Paragraph("• <b>Filtros en Cadena (Chain of Responsibility):</b> Procesamiento de solicitudes HTTP a través de la cadena de filtros de Spring Security antes de alcanzar los controladores.", bullet_style))

    # ========================================================
    # SECCIÓN 3: EL FRONTEND (REACT.JS Y ECOSYSTEM)
    # ========================================================
    story.append(Spacer(1, 10))
    story.append(Paragraph("3. Frontend: Tecnologías y Componentes", h1_style))
    story.append(Paragraph(
        "El cliente web está construido como una aplicación React 18 moderna, estructurada modularmente "
        "en componentes reutilizables, estilos utilitarios y almacenamiento global centralizado.",
        body_style
    ))

    story.append(Paragraph("3.1 Componentes Principales de la Pantalla de Inicio (Video 1):", h2_style))
    story.append(Paragraph("• <b>Navigation (Navbar):</b> Barra de navegación con soporte para búsqueda, branding de ShopEase, navegación por categorías (/men, /women, /kids) y badges dinámicos con la cantidad de productos en el carrito.", bullet_style))
    story.append(Paragraph("• <b>HeroSection:</b> Banner principal con imagen de impacto visual, eslogan comercial y botón de llamado a la acción (CTA) para dirigir al usuario al catálogo.", bullet_style))
    story.append(Paragraph("• <b>NewArrivals & Carousel:</b> Carrusel interactivo que utiliza tarjetas reutilizables (<code>Card.jsx</code>) para destacar los productos recién llegados con precios, calificaciones y títulos.", bullet_style))
    story.append(Paragraph("• <b>Category Grid:</b> Módulo de categorización para Hombres y Mujeres con tarjetas gráficas para navegar rápidamente hacia ropa casual, remeras, abrigos y jeans.", bullet_style))
    story.append(Paragraph("• <b>Footer:</b> Pie de página corporativo con enlaces informativos, atención al cliente, suscripción a newsletter e iconos de redes sociales.", bullet_style))

    story.append(Spacer(1, 6))
    story.append(Paragraph("3.2 Gestión de Estado con Redux Toolkit:", h2_style))
    story.append(Paragraph(
        "Se implementó Redux Toolkit para gestionar el estado de manera predecible evitando el antipatrón "
        "<i>prop drilling</i>. Los slices destacados son:",
        body_style
    ))
    story.append(Paragraph("• <b>Cart Slice:</b> Permite agregar, remover y calcular totales de ítems en el carrito de compras en tiempo real, persistiendo la selección del cliente.", bullet_style))
    story.append(Paragraph("• <b>Category Slice:</b> Mantiene en caché las categorías obtenidas del backend para abastecer menús y filtros.", bullet_style))
    story.append(Paragraph("• <b>Common Slice:</b> Controla estados globales de interfaz como spinners de carga (<code>isLoading</code>) y notificaciones emergentes.", bullet_style))

    story.append(Spacer(1, 6))
    story.append(Paragraph("3.3 Enrutamiento y Conexión de Red (Axios):", h2_style))
    story.append(Paragraph(
        "Mediante <b>React Router DOM v6</b> se configuran rutas protegidas y públicas. La capa de "
        "comunicación HTTP se centraliza en <code>src/api/</code> con Axios, donde cada solicitud "
        "incluye automáticamente el token JWT en el encabezado <code>Authorization: Bearer &lt;token&gt;</code>.",
        body_style
    ))

    story.append(PageBreak())

    # ========================================================
    # SECCIÓN 4: EL BACKEND (SPRING BOOT 3 Y SEGURIDAD)
    # ========================================================
    story.append(Paragraph("4. Backend: Spring Boot 3 y Arquitectura de Servicios", h1_style))
    story.append(Paragraph(
        "El servidor backend expone endpoints RESTful orientados a recursos bajo el protocolo HTTP, "
        "garantizando validación de datos, manejo de excepciones y autorización basada en roles.",
        body_style
    ))

    story.append(Paragraph("4.1 Mecanismo de Seguridad y Autenticación JWT:", h2_style))
    story.append(Paragraph(
        "La autenticación se implementa con <b>JSON Web Tokens (JWT)</b> bajo el estándar RFC 7519:",
        body_style
    ))
    story.append(Paragraph("1. El cliente envía credenciales mediante <code>POST /api/auth/login</code>.", bullet_style))
    story.append(Paragraph("2. Spring Security autentica contra la base de datos comparando el hash de contraseña (BCrypt).", bullet_style))
    story.append(Paragraph("3. Si es exitoso, <code>JWTTokenHelper</code> firma un token digital con clave secreta y expiración.", bullet_style))
    story.append(Paragraph("4. En cada petición posterior, <code>JWTAuthenticationFilter</code> intercepta la cabecera HTTP, valida la firma del token y establece el contexto de seguridad en <code>SecurityContextHolder</code>.", bullet_style))

    story.append(Spacer(1, 8))
    story.append(Paragraph("4.2 Resumen de Endpoints REST Principales:", h2_style))

    endpoints_data = [
        [Paragraph("Método", table_header_style), Paragraph("Ruta / Endpoint", table_header_style), Paragraph("Acceso", table_header_style), Paragraph("Descripción", table_header_style)],
        [Paragraph("<code>POST</code>", table_cell_style), Paragraph("<code>/api/auth/register</code>", table_cell_style), Paragraph("Público", table_cell_style), Paragraph("Registro de nuevos clientes.", table_cell_style)],
        [Paragraph("<code>POST</code>", table_cell_style), Paragraph("<code>/api/auth/login</code>", table_cell_style), Paragraph("Público", table_cell_style), Paragraph("Autenticación y emisión de token JWT.", table_cell_style)],
        [Paragraph("<code>GET</code>", table_cell_style), Paragraph("<code>/api/products</code>", table_cell_style), Paragraph("Público", table_cell_style), Paragraph("Listado y filtrado de productos.", table_cell_style)],
        [Paragraph("<code>GET</code>", table_cell_style), Paragraph("<code>/api/category</code>", table_cell_style), Paragraph("Público", table_cell_style), Paragraph("Obtención de categorías y tipos de ropa.", table_cell_style)],
        [Paragraph("<code>POST</code>", table_cell_style), Paragraph("<code>/api/order</code>", table_cell_style), Paragraph("Protegido (JWT)", table_cell_style), Paragraph("Creación y procesamiento de pedidos.", table_cell_style)],
        [Paragraph("<code>POST</code>", table_cell_style), Paragraph("<code>/api/products</code>", table_cell_style), Paragraph("Admin (JWT)", table_cell_style), Paragraph("Creación de nuevos productos en catálogo.", table_cell_style)],
        [Paragraph("<code>GET</code>", table_cell_style), Paragraph("<code>/swagger-ui/index.html</code>", table_cell_style), Paragraph("Público", table_cell_style), Paragraph("Documentación interactiva Swagger OpenAPI.", table_cell_style)],
    ]
    t_end = Table(endpoints_data, colWidths=[55, 135, 80, 230])
    t_end.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), primary_color),
        ('ALIGN', (0,0), (-1,-1), 'LEFT'),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
        ('GRID', (0,0), (-1,-1), 0.5, border_color),
        ('PADDING', (0,0), (-1,-1), 5),
    ]))
    story.append(t_end)

    # ========================================================
    # SECCIÓN 5: MODELADO DE DATOS (MYSQL 8)
    # ========================================================
    story.append(Spacer(1, 12))
    story.append(Paragraph("5. Modelado y Base de Datos (MySQL 8)", h1_style))
    story.append(Paragraph(
        "El motor relacional elegido es <b>MySQL 8.0</b> con soporte de transacciones <b>InnoDB</b>. "
        "Hibernate mapea las entidades Java directamente a tablas relacionales generando claves primarias "
        "basadas en <code>UUID</code> (almacenadas como <code>binary(16)</code> para máxima eficiencia).",
        body_style
    ))

    db_tables_data = [
        [Paragraph("Tabla en MySQL", table_header_style), Paragraph("Entidad JPA", table_header_style), Paragraph("Campos Clave y Relaciones", table_header_style)],
        [Paragraph("<code>auth_user_details</code>", table_cell_style), Paragraph("<code>User</code>", table_cell_style), Paragraph("id, email (UNIQUE), password, first_name, last_name, enabled", table_cell_style)],
        [Paragraph("<code>auth_authority</code>", table_cell_style), Paragraph("<code>Authority</code>", table_cell_style), Paragraph("id, role_code (e.g. ROLE_USER, ROLE_ADMIN), role_description", table_cell_style)],
        [Paragraph("<code>categories</code>", table_cell_style), Paragraph("<code>Category</code>", table_cell_style), Paragraph("id, code, name, description. Relación 1:N con category_type.", table_cell_style)],
        [Paragraph("<code>category_type</code>", table_cell_style), Paragraph("<code>CategoryType</code>", table_cell_style), Paragraph("id, name, code, category_id (FK).", table_cell_style)],
        [Paragraph("<code>products</code>", table_cell_style), Paragraph("<code>Product</code>", table_cell_style), Paragraph("id, name, slug (UNIQUE), price, brand, rating, category_id (FK)", table_cell_style)],
        [Paragraph("<code>product_variant</code>", table_cell_style), Paragraph("<code>ProductVariant</code>", table_cell_style), Paragraph("id, color, size, stock_quantity, product_id (FK)", table_cell_style)],
        [Paragraph("<code>product_resources</code>", table_cell_style), Paragraph("<code>Resources</code>", table_cell_style), Paragraph("id, name, url, is_primary, product_id (FK)", table_cell_style)],
        [Paragraph("<code>orders</code>", table_cell_style), Paragraph("<code>Order</code>", table_cell_style), Paragraph("id, order_date, total_amount, order_status, user_id (FK), address_id (FK)", table_cell_style)],
        [Paragraph("<code>order_items</code>", table_cell_style), Paragraph("<code>OrderItem</code>", table_cell_style), Paragraph("id, quantity, item_price, order_id (FK), product_id (FK)", table_cell_style)],
    ]
    t_db = Table(db_tables_data, colWidths=[120, 110, 270])
    t_db.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), secondary_color),
        ('ALIGN', (0,0), (-1,-1), 'LEFT'),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
        ('GRID', (0,0), (-1,-1), 0.5, border_color),
        ('PADDING', (0,0), (-1,-1), 5),
    ]))
    story.append(t_db)

    # ========================================================
    # SECCIÓN 6: RESOLUCIÓN DE PROBLEMAS Y CONFIGURACIÓN
    # ========================================================
    story.append(Spacer(1, 10))
    story.append(Paragraph("6. Desafíos de Ingeniería Resueltos en este Entorno", h1_style))
    story.append(Paragraph(
        "Durante la configuración y puesta en marcha del proyecto en el entorno de desarrollo local, "
        "se resolvieron los siguientes retos técnicos indispensables para la estabilidad:",
        body_style
    ))

    sol_1 = (
        "<b>1. Migración de PostgreSQL a MySQL:</b><br/>"
        "El repositorio original dependía de PostgreSQL en la nube (Vercel). Se modificó <code>pom.xml</code> "
        "para incorporar <code>mysql-connector-j</code> y se reconfiguró <code>application.properties</code> "
        "con <code>jdbc:mysql://localhost:3306/shopease?createDatabaseIfNotExist=true</code>, adaptando el dialecto "
        "Hibernate a MySQLDialect y conectando exitosamente con el servicio local MySQL 8.0."
    )
    story.append(Paragraph(sol_1, body_style))
    story.append(Spacer(1, 4))

    sol_2 = (
        "<b>2. Incompatibilidad Java 22 vs Lombok (ClassFormatError):</b><br/>"
        "Java 22 introdujo cambios en el parsing del AST de constructores (JEP 447 - Flexible Constructor Bodies). "
        "La versión previa de Lombok generaba bytecode con longitud de código cero en <code>Authority.class</code>, "
        "arrojando <code>ClassFormatError</code> al iniciar Spring Boot. "
        "<b>Solución:</b> Se actualizó la dependencia de Lombok a la versión <code>1.18.36</code> en Maven, "
        "restaurando la generación limpia y válida del bytecode."
    )
    story.append(Paragraph(sol_2, body_style))
    story.append(Spacer(1, 4))

    sol_3 = (
        "<b>3. Referencia Circular de Placeholders en Spring Boot:</b><br/>"
        "Propiedades como <code>FILE_ZONE=${FILE_ZONE:dummy}</code> generaban un error de circularidad "
        "(<code>Circular placeholder reference</code>) al intentar resolver variables no exportadas en el SO. "
        "<b>Solución:</b> Se establecieron valores literales por defecto en <code>application.properties</code>, "
        "garantizando arranque en frío 100% resiliente sin depender de claves externas de CDN."
    )
    story.append(Paragraph(sol_3, body_style))

    # ========================================================
    # SECCIÓN 7: GUÍA RÁPIDA DE COMANDOS Y ACCESO
    # ========================================================
    story.append(Spacer(1, 10))
    story.append(Paragraph("7. Guía de Ejecución y Monitoreo", h1_style))

    guide_content = (
        "<b>1. Servidor Frontend (React):</b><br/>"
        "&nbsp;&nbsp;&nbsp;&nbsp;Directorio: <code>UI/</code><br/>"
        "&nbsp;&nbsp;&nbsp;&nbsp;Comando: <code>npm start</code><br/>"
        "&nbsp;&nbsp;&nbsp;&nbsp;URL de Acceso: <a href='http://localhost:3000'><b>http://localhost:3000</b></a><br/><br/>"
        "<b>2. Servidor Backend (Spring Boot):</b><br/>"
        "&nbsp;&nbsp;&nbsp;&nbsp;Directorio: <code>backend/</code><br/>"
        "&nbsp;&nbsp;&nbsp;&nbsp;Comando: <code>.\\mvnw.cmd spring-boot:run</code><br/>"
        "&nbsp;&nbsp;&nbsp;&nbsp;URL de Acceso API: <a href='http://localhost:8080'><b>http://localhost:8080</b></a><br/>"
        "&nbsp;&nbsp;&nbsp;&nbsp;Documentación Swagger: <a href='http://localhost:8080/swagger-ui/index.html'><b>http://localhost:8080/swagger-ui/index.html</b></a><br/><br/>"
        "<b>3. Base de Datos (MySQL):</b><br/>"
        "&nbsp;&nbsp;&nbsp;&nbsp;Servidor: <code>localhost:3306</code><br/>"
        "&nbsp;&nbsp;&nbsp;&nbsp;Base de Datos: <code>shopease</code> (Usuario: <code>root</code> / Clave: <code>1234</code>)<br/>"
        "&nbsp;&nbsp;&nbsp;&nbsp;Tablas sincronizadas: 12 tablas generadas por JPA Hibernate."
    )
    story.append(Table([[Paragraph(guide_content, body_style)]], colWidths=[500], style=[
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#F8FAFC")),
        ('BOX', (0,0), (-1,-1), 1, colors.HexColor("#CBD5E1")),
        ('PADDING', (0,0), (-1,-1), 10),
    ]))

    story.append(PageBreak())

    # ========================================================
    # SECCIÓN 8: FLUJO COMPLETO END-TO-END Y CRITERIOS IS-2
    # ========================================================
    story.append(Paragraph("8. Ciclo de Vida de una Transacción (Flujo End-to-End)", h1_style))
    story.append(Paragraph(
        "A continuación se sintetiza el ciclo de vida completo de interacción entre el usuario, "
        "la interfaz en React, la API en Spring Boot y el motor MySQL:",
        body_style
    ))

    flow_data = [
        [Paragraph("Paso", table_header_style), Paragraph("Fase", table_header_style), Paragraph("Acción y Procesamiento Técnico", table_header_style)],
        [
            Paragraph("<b>1</b>", table_cell_style),
            Paragraph("Carga Inicial", table_cell_style),
            Paragraph("El cliente accede a <code>/</code>. React monta los componentes Navbar, HeroSection, Carousel y solicita categorías a <code>GET /api/category</code>. Redux almacena el catálogo.", table_cell_style)
        ],
        [
            Paragraph("<b>2</b>", table_cell_style),
            Paragraph("Selección y Carrito", table_cell_style),
            Paragraph("El usuario navega a <code>/men</code> o <code>/women</code>, selecciona un producto, elige talle/color y presiona 'Add to Cart'. Redux despacha la acción <code>addToCart</code> actualizando el badge del Navbar.", table_cell_style)
        ],
        [
            Paragraph("<b>3</b>", table_cell_style),
            Paragraph("Autenticación JWT", table_cell_style),
            Paragraph("Al avanzar hacia el checkout, el usuario ingresa sus credenciales en <code>/v1/login</code>. Spring Security valida el hash BCrypt y retorna un token JWT que se guarda en <code>localStorage</code>.", table_cell_style)
        ],
        [
            Paragraph("<b>4</b>", table_cell_style),
            Paragraph("Confirmación de Pedido", table_cell_style),
            Paragraph("Se envía <code>POST /api/order</code> con el token en la cabecera <code>Authorization</code>. El backend persiste la orden en la tabla <code>orders</code> y los ítems correspondientes en <code>order_items</code> con integridad ACID.", table_cell_style)
        ]
    ]
    t_flow = Table(flow_data, colWidths=[35, 115, 350])
    t_flow.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), primary_color),
        ('ALIGN', (0,0), (-1,-1), 'LEFT'),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
        ('GRID', (0,0), (-1,-1), 0.5, border_color),
        ('PADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(t_flow)

    story.append(Spacer(1, 10))
    story.append(Paragraph("9. Principios de Ingeniería de Software Aplicados", h1_style))
    story.append(Paragraph("• <b>Single Responsibility Principle (SRP):</b> Cada clase de servicio en Spring Boot (e.g. <code>CategoryService</code>, <code>OrderService</code>, <code>RegistrationService</code>) tiene una única responsabilidad de dominio bien delimitada.", bullet_style))
    story.append(Paragraph("• <b>Open/Closed Principle (OCP):</b> Nuevos métodos de pago o proveedores de autenticación (como OAuth2 con Google o Stripe) se integran mediante interfaces sin alterar la lógica central de órdenes.", bullet_style))
    story.append(Paragraph("• <b>Alta Cohesión y Bajo Acoplamiento:</b> El desacoplamiento físico entre Frontend y Backend permite iterar o desplegar cualquiera de los dos módulos de forma totalmente independiente.", bullet_style))
    story.append(Paragraph("• <b>Manejo Transaccional ACID:</b> La persistencia en MySQL a través de <code>@Transactional</code> garantiza que un pedido y sus ítems se graben completamente o se realice un rollback ante cualquier eventualidad.", bullet_style))

    story.append(Spacer(1, 10))
    story.append(Paragraph("10. Conclusión", h1_style))
    story.append(Paragraph(
        "El proyecto <b>ShopEase</b> constituye un caso de estudio paradigmático para la materia "
        "<b>Ingeniería de Software</b>. Abarca con éxito el ciclo de vida completo del desarrollo de software: "
        "análisis de requisitos, diseño de interfaces modernas y accesibles, desacoplamiento cliente-servidor mediante "
        "APIs REST, seguridad robusta por tokens JWT y persistencia normalizada con soporte transaccional en MySQL 8. "
        "La integración lograda entre React 18, Spring Boot 3 y MySQL consolida una base profesional lista para su evaluación académica.",
        body_style
    ))

    # Build PDF with custom NumberedCanvas
    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"PDF generado exitosamente en: {filename}")

if __name__ == "__main__":
    out_pdf = os.path.abspath("ShopEase_Documentacion_Proyecto.pdf")
    if len(sys.argv) > 1:
        out_pdf = sys.argv[1]
    create_pdf(out_pdf)

package com.tienda.zero.model;

import com.tienda.zero.enums.TipoCorreo;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuracion_correo_empresa")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ConfiguracionCorreoEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCorreo tipoCorreo;

    @Column(nullable = false)
    private String asunto;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String cuerpoHtml;

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
}
package com.example.mascotas.controladores;

import com.example.mascotas.dto.ReporteVotosDTO;
import com.example.mascotas.servicios.VotoServicio;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.PrintWriter;
import java.util.List;

@Controller
@RequestMapping("/reportes")
public class ReporteControlador {

    @Autowired
    private VotoServicio votoServicio;

    @GetMapping("/votos-txt")
    public void descargarReporteVotos(HttpServletResponse response) throws Exception {

        List<ReporteVotosDTO> reporte = votoServicio.generarReporteVotos();

        response.setContentType("text/plain; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"reporte-votos.txt\"");

        PrintWriter writer = response.getWriter();
        writer.println("REPORTE DE VOTOS POR MASCOTA");
        writer.println("================================================");

        for (ReporteVotosDTO r : reporte) {
            writer.printf("Usuario: %s %s | Mascota: %s | Votos: %d%n",
                    r.getNombreUsuario(),
                    r.getApellidoUsuario(),
                    r.getNombreMascota(),
                    r.getCantidadVotos());
        }

        writer.flush();
        writer.close();
    }
}

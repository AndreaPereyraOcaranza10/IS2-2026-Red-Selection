package redselection.ejercicio_c.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import redselection.ejercicio_c.entities.Libro;
import redselection.ejercicio_c.repositories.LibroRepository;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.Optional;

@Service
public class LibroServiceImpl extends BaseServiceImpl<Libro, Long> implements LibroService {

    private final LibroRepository libroRepository;

    @Value("${biblioteca.ruta}")
    private String rutaBiblioteca;

    public LibroServiceImpl(LibroRepository libroRepository) {
        super(libroRepository);
        this.libroRepository = libroRepository;
    }

    @Override
    @Transactional
    public Libro guardarPdf(Long id, MultipartFile archivo) throws Exception {
        try {
            if (archivo == null || archivo.isEmpty()) {
                throw new Exception("El archivo está vacío.");
            }
            if (!esPdf(archivo)) {
                throw new Exception("El archivo no es un PDF válido.");
            }

            Optional<Libro> libroOptional = libroRepository.findById(id);
            if (libroOptional.isEmpty()) {
                throw new Exception("No existe el libro con id " + id);
            }
            Libro libro = libroOptional.get();

            String nombreArchivo = "libro_" + limpiarNombre(libro.getTitulo()) + ".pdf";
            Path carpeta = Paths.get(rutaBiblioteca).toAbsolutePath().normalize();
            Files.createDirectories(carpeta);

            Path destino = carpeta.resolve(nombreArchivo).normalize();
            if (!destino.startsWith(carpeta)) {
                throw new Exception("Nombre de archivo inválido.");
            }

            try (InputStream entrada = archivo.getInputStream()) {
                Files.copy(entrada, destino, StandardCopyOption.REPLACE_EXISTING);
            }

            libro.setArchivoPdf(nombreArchivo);
            return libroRepository.save(libro);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Resource obtenerPdf(Long id) throws Exception {
        try {
            Optional<Libro> libroOptional = libroRepository.findById(id);
            if (libroOptional.isEmpty()) {
                throw new Exception("No existe el libro con id " + id);
            }
            Libro libro = libroOptional.get();
            if (libro.getArchivoPdf() == null || libro.getArchivoPdf().isBlank()) {
                throw new Exception("El libro no tiene un PDF cargado.");
            }

            Path carpeta = Paths.get(rutaBiblioteca).toAbsolutePath().normalize();
            Path archivo = carpeta.resolve(libro.getArchivoPdf()).normalize();
            if (!archivo.startsWith(carpeta) || !Files.isReadable(archivo)) {
                throw new Exception("No se encontró el archivo PDF en el servidor.");
            }
            return new FileSystemResource(archivo);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private boolean esPdf(MultipartFile archivo) throws Exception {
        try (InputStream entrada = archivo.getInputStream()) {
            byte[] cabecera = entrada.readNBytes(4);
            return cabecera.length == 4 && cabecera[0] == '%' && cabecera[1] == 'P'
                    && cabecera[2] == 'D' && cabecera[3] == 'F';
        }
    }

    private String limpiarNombre(String titulo) {
        String base = (titulo == null || titulo.isBlank()) ? "sin_titulo" : titulo.trim();
        base = Normalizer.normalize(base, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        base = base.replaceAll("[^A-Za-z0-9_-]+", "_");
        base = base.replaceAll("^_+|_+$", "");
        return base.isEmpty() ? "sin_titulo" : base;
    }
}
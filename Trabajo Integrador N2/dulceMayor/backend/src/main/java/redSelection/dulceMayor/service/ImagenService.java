package redSelection.dulceMayor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import redSelection.dulceMayor.dto.ImagenContenidoDTO;
import redSelection.dulceMayor.dto.ImagenRespuestaDTO;
import redSelection.dulceMayor.enums.TipoImagen;
import redSelection.dulceMayor.exception.RecursoNoEncontradoException;
import redSelection.dulceMayor.exception.ReglaNegocioException;
import redSelection.dulceMayor.model.Imagen;
import redSelection.dulceMayor.repository.ImagenRepository;

import java.io.IOException;
import java.util.Set;

@Service
public class ImagenService {

    private static final long MAX_BYTES = 2L * 1024 * 1024;
    private static final Set<String> MIMES = Set.of("image/png", "image/jpeg", "image/webp");

    private final ImagenRepository imagenRepository;

    public ImagenService(ImagenRepository imagenRepository) { this.imagenRepository = imagenRepository; }

    @Transactional
    public ImagenRespuestaDTO subir(MultipartFile archivo, TipoImagen tipo) {
        if (archivo == null || archivo.isEmpty()) throw new ReglaNegocioException("Falta el archivo de imagen.");
        if (archivo.getSize() > MAX_BYTES) throw new ReglaNegocioException("La imagen no puede superar los 2 MB.");
        String mime = archivo.getContentType();
        if (mime == null || !MIMES.contains(mime)) throw new ReglaNegocioException("Formato no permitido. Usá PNG, JPG o WEBP.");
        byte[] bytes;
        try { bytes = archivo.getBytes(); }
        catch (IOException e) { throw new ReglaNegocioException("No se pudo leer el archivo."); }
        if (!firmaValida(mime, bytes)) throw new ReglaNegocioException("El contenido del archivo no coincide con una imagen válida.");
        Imagen guardada = imagenRepository.save(Imagen.builder()
                .nombre(limpiarNombre(archivo.getOriginalFilename())).mime(mime).contenido(bytes).tipoImagen(tipo).build());
        return new ImagenRespuestaDTO(guardada.getId(), guardada.getNombre(), guardada.getMime(), bytes.length);
    }

    @Transactional(readOnly = true)
    public ImagenContenidoDTO contenido(String id) {
        Imagen i = obtenerEntidad(id);
        return new ImagenContenidoDTO(i.getMime(), i.getContenido());
    }

    @Transactional(readOnly = true)
    public Imagen obtenerEntidad(String id) {
        return imagenRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Imagen", id));
    }

    /** Para relaciones opcionales: null o vacío significa "sin imagen". */
    @Transactional(readOnly = true)
    public Imagen buscarOpcional(String id) {
        return (id == null || id.isBlank()) ? null : obtenerEntidad(id);
    }

    private boolean firmaValida(String mime, byte[] b) {
        return switch (mime) {
            case "image/png" -> b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
            case "image/jpeg" -> b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
            case "image/webp" -> b.length > 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                    && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
            default -> false;
        };
    }

    private String limpiarNombre(String original) {
        String base = original == null ? "imagen" : original.replaceAll("[\\\\/]", "_");
        return base.length() > 200 ? base.substring(0, 200) : base;
    }
}
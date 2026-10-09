package redselection.ejercicio_c.services;


import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import redselection.ejercicio_c.entities.Libro;

public interface LibroService extends BaseService<Libro, Long>{
    public Libro guardarPdf(Long id, MultipartFile archivo) throws Exception;
    public Resource obtenerPdf(Long id) throws Exception;
}

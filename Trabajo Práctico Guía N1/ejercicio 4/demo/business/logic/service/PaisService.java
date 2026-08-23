package com.example.demo.business.logic.service;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.business.domain.entity.Pais;
import com.example.demo.business.logic.error.ErrorServiceException;
import com.example.demo.business.persitence.repository.PaisRepository;

import jakarta.persistence.NoResultException;

/* @Service: Indica que esta clase es un componente de servicio de Spring,
 * lo que permite la inyección de dependencias y otras funcionalidades proporcionadas 
 * por el contenedor de Spring.
 */

@Service
public class PaisService {

	/* Repositorio que permite la interacción con la base de datos.
	 * @Autowired realiza la inyección de dependencia.
	 */ 
	@Autowired
	private PaisRepository repository; 
    
	/* @Transactional: Indica que el método debe ejecutarse dentro de una transacción.
     * Esto significa que todas las operaciones de base de datos dentro del método
     * serán tratadas como una única unidad de trabajo, y pueden ser confirmadas (commit) o revertidas (rollback) juntas.
     * En el método delete, @Transactional asegura que la operación
     * de marcar la entidad como eliminada y guardarla nuevamente ocurra dentro de una transacción.
     * Esto garantiza que ambos pasos se ejecuten correctamente o ninguno se ejecute en caso de un error.
     */
	
	@Transactional
    public void crearPais(String nombre) throws ErrorServiceException {

        try {
            
            validar(nombre);

            try {
            	Pais paisAux = repository.buscarPaisPorNombre(nombre);
            	if (paisAux != null && !paisAux.isEliminado()) {
                 throw new ErrorServiceException("Existe un país con el nombre indicado");
            	} 
            } catch (NoResultException ex) {}

            Pais pais = new Pais();
            pais.setId(UUID.randomUUID().toString());
            pais.setNombre(nombre);
            pais.setEliminado(false);

            repository.save(pais);

        } catch (ErrorServiceException e) {
            throw e;
        } catch (Exception ex){
            ex.printStackTrace();
            throw new ErrorServiceException("Error de Sistemas");
        }
	}
	
    public void validar(String nombre)throws ErrorServiceException {
        
        try{
            
            if (nombre == null || nombre.isEmpty()) {
                throw new ErrorServiceException("Debe indicar el nombre");
            }
            
        } catch (ErrorServiceException e) {
            throw e;
        } catch (Exception ex){
            ex.printStackTrace();
            throw new ErrorServiceException("Error de Sistemas");
        }
    }
	
    /* @Transactional: Indica que el método debe ejecutarse dentro de una transacción.
     * Esto significa que todas las operaciones de base de datos dentro del método
     * serán tratadas como una única unidad de trabajo, y pueden ser confirmadas (commit) o revertidas (rollback) juntas.
     * En el método delete, @Transactional asegura que la operación
     * de marcar la entidad como eliminada y guardarla nuevamente ocurra dentro de una transacción.
     * Esto garantiza que ambos pasos se ejecuten correctamente o ninguno se ejecute en caso de un error.
     */
    
	@Transactional
    public void modificarPais(String idPais, String nombre) throws ErrorServiceException {

        try {

        	validar(nombre);
        	
            Pais pais = buscarPais(idPais);

            try{
                Pais paisExsitente = repository.buscarPaisPorNombre(nombre);
                if (paisExsitente != null && !paisExsitente.getId().equals(idPais) && !paisExsitente.isEliminado()){
                  throw new ErrorServiceException("Existe un país con el nombre indicado");  
                }
            } catch (NoResultException ex) {}

            pais.setNombre(nombre);
            pais.setEliminado(false);
            
            repository.save(pais);

        } catch (ErrorServiceException e) {
            throw e;
        } catch (Exception ex){
            ex.printStackTrace();
            throw new ErrorServiceException("Error de Sistemas");
        }
    }
	
	public Pais buscarPais(String id) throws ErrorServiceException {

        try {
            
            if (id == null || id.isEmpty()) {
                throw new ErrorServiceException("Debe indicar el país");
            }

            /* El método findById heredado de JpaRepository para buscar la entidad por su ID.
             * Este método devuelve un Optional<E>, que puede contener la entidad si se encuentra,
             * o estar vacío si no se encuentra.
             * ¿Qué es Optional?
             * Un Optional<E> es un contenedor que puede o no contener un valor no nulo de tipo E.
             * Los Optional se utilizan para evitar NullPointerException
             * y para expresar la ausencia de un valor de manera más clara.
             */
            
            Optional<Pais> optional = repository.findById(id);
            Pais pais = null;
            if (optional.isPresent()) {
            	
            	/*
            	 * Si la entidad se encuentra, se obtiene de Optional usando get()
            	 */
            	pais= optional.get();
            	
            	/* Verifica si la entidad está marcada como eliminada lógicamente (entity.isEliminado()).
                 * Si es así, registra un mensaje de error y lanza una RuntimeException.
                 */
    			if (pais.isEliminado()){
                    throw new ErrorServiceException("No se encuentra el país indicado");
                }
    		}
            
            return pais;
            
        } catch (ErrorServiceException ex) {  
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ErrorServiceException("Error de sistema");
        }
    }

	/* @Transactional: Indica que el método debe ejecutarse dentro de una transacción.
     * Esto significa que todas las operaciones de base de datos dentro del método
     * serán tratadas como una única unidad de trabajo, y pueden ser confirmadas (commit) o revertidas (rollback) juntas.
     * En el método delete, @Transactional asegura que la operación
     * de marcar la entidad como eliminada y guardarla nuevamente ocurra dentro de una transacción.
     * Esto garantiza que ambos pasos se ejecuten correctamente o ninguno se ejecute en caso de un error.
     */
	
    @Transactional
    public void eliminarPais(String id) throws ErrorServiceException {

        try {

            Pais pais = buscarPais(id);
            pais.setEliminado(true);
            
            repository.save(pais);

        } catch (ErrorServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ErrorServiceException("Error de sistema");
        }

    }

    public Collection<Pais> listarPais() throws ErrorServiceException {
        try {
            
        	/* findAll(): Este método de JpaRepository
             * se utiliza para obtener todas las entidades del tipo E desde la base de datos.
             * Devuelve una lista (List<E>) de todas las entidades.
             */
        	
            return repository.findAll();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ErrorServiceException("Error de sistema");
        }
    }
    
    public Collection<Pais> listarPaisActivo() throws ErrorServiceException {
        try {
            
            return repository.listarPaisActivo();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ErrorServiceException("Error de sistema");
        }
    }
}

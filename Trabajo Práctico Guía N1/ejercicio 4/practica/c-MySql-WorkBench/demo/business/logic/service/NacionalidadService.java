package com.example.demo.business.logic.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.business.domain.entity.Nacionalidad;
import com.example.demo.business.logic.error.ErrorServiceException;
import com.example.demo.business.persitence.repository.NacionalidadRepository;

import jakarta.persistence.NoResultException;

@Service
public class NacionalidadService {

	@Autowired
	private NacionalidadRepository repository; 
    
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

	@Transactional
    public void crearNacionalidad(String nombre) throws ErrorServiceException {

        try {
            
            validar(nombre);

            try {
            	Nacionalidad nacionalidad = repository.buscarNacionalidadPorNombre(nombre);
            	if (nacionalidad != null && !nacionalidad.isEliminado()) {
                 throw new ErrorServiceException("Existe una nacionalidad con el nombre indicado");
            	} 
            } catch (NoResultException ex) {}

            Nacionalidad nacionalidad = new Nacionalidad();
            nacionalidad.setId(UUID.randomUUID().toString());
            nacionalidad.setNombre(nombre);
            nacionalidad.setEliminado(false);

            repository.save(nacionalidad);

        } catch (ErrorServiceException e) {
            throw e;
        } catch (Exception ex){
            ex.printStackTrace();
            throw new ErrorServiceException("Error de Sistemas");
        }
    }

	@Transactional
    public void modificarNacionalidad(String idNacionalidad, String nombre) throws ErrorServiceException {

        try {

            Nacionalidad nacionalidad = buscarNacionalidad(idNacionalidad);

            validar(nombre);

            try{
                Nacionalidad NacionalidadExsitente = repository.buscarNacionalidadPorNombre(nombre);
                if (NacionalidadExsitente != null && !NacionalidadExsitente.getId().equals(idNacionalidad) && !NacionalidadExsitente.isEliminado()){
                  throw new ErrorServiceException("Existe una nacionalidad con el nombre indicado");  
                }
            } catch (NoResultException ex) {}

            nacionalidad.setNombre(nombre);
            nacionalidad.setEliminado(false);
            
            repository.save(nacionalidad);

        } catch (ErrorServiceException e) {
            throw e;
        } catch (Exception ex){
            ex.printStackTrace();
            throw new ErrorServiceException("Error de Sistemas");
        }
    }
	
	public Nacionalidad buscarNacionalidad(String id) throws ErrorServiceException {

        try {
            
            if (id == null || id.isEmpty()) {
                throw new ErrorServiceException("Debe indicar la nacionalidad");
            }

            Optional<Nacionalidad> optional = repository.findById(id);
            Nacionalidad nacionalidad = null;
            if (optional.isPresent()) {
            	nacionalidad= optional.get();
    			if (nacionalidad.isEliminado()){
                    throw new ErrorServiceException("No se encuentra la nacionalidad indicada");
                }
    		}
            
            return nacionalidad;
            
        } catch (ErrorServiceException ex) {  
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ErrorServiceException("Error de sistema");
        }
    }

    @Transactional
    public void eliminarNacionalidad(String id) throws ErrorServiceException {

        try {

            Nacionalidad nacionalidad = buscarNacionalidad(id);
            nacionalidad.setEliminado(true);
            
            repository.save(nacionalidad);

        } catch (ErrorServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ErrorServiceException("Error de sistema");
        }

    }

    public Collection<Nacionalidad> listarNacionalidad() throws ErrorServiceException {
        try {
            
            return repository.findAll();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ErrorServiceException("Error de sistema");
        }
    }
    
    public List<Nacionalidad> listarNacionalidadActivo() throws ErrorServiceException {
        try {
            
            return repository.listarNacionalidadActivo();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ErrorServiceException("Error de sistema");
        }
    }
}

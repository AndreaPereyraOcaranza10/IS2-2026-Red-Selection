package com.ejercicio_c.biblioteca.services;

import com.ejercicio_c.biblioteca.exceptions.ErrorServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class InicioAplicacionService {

	@Autowired
	private UsuarioService usuarioService;
	
	public void iniciarAplicacion() throws ErrorServiceException {
		
		try {
			
			//try {
				//usuarioService.buscarUsuarioPorNombre("Administrador");
			//} catch (ErrorServiceException e) {
				//usuarioService.crearUsuario("Administrador", "administrador@administrador", "1234567", "1234567", null);
			//}
			
        } catch (Exception ex){
            ex.printStackTrace();
            throw new ErrorServiceException("Error de Sistemas");
        }
	}
}

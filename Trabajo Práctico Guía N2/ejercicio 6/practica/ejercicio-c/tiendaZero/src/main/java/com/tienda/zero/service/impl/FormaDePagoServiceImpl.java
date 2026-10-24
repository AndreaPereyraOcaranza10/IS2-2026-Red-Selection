package com.tienda.zero.service.impl;

import com.tienda.zero.enums.TipoPago;
import com.tienda.zero.model.FormaDePago;
import com.tienda.zero.repository.FormaDePagoRepository;
import com.tienda.zero.service.FormaDePagoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FormaDePagoServiceImpl implements FormaDePagoService {

    private final FormaDePagoRepository formaDePagoRepository;

    public FormaDePagoServiceImpl(FormaDePagoRepository formaDePagoRepository) {
        this.formaDePagoRepository = formaDePagoRepository;
    }

    @Override
    @Transactional
    public FormaDePago crearFormaDePago(TipoPago tipo, String observacion) {
        validar(tipo);

        FormaDePago formaDePagoNuevo = new FormaDePago();
        formaDePagoNuevo.setTipoPago(tipo);
        formaDePagoNuevo.setObservacion(limpiarObservacion(observacion));
        formaDePagoNuevo.setEliminado(false);
        return formaDePagoRepository.save(formaDePagoNuevo);
    }

    @Override
    public void validar(TipoPago tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de pago es obligatorio");
        }
    }

    @Override
    public FormaDePago buscarFormaDePago(String id) {
        Optional<FormaDePago> resultado = formaDePagoRepository.findById(id);
        if (resultado.isEmpty()) {
            throw new IllegalArgumentException("No existe la forma de pago con id: " + id);
        }
        return resultado.get();
    }

    @Override
    @Transactional
    public FormaDePago modificarFormaDePago(String id, TipoPago tipo, String observacion) {
        validar(tipo);
        FormaDePago formaDePago = buscarFormaDePago(id);

        if (formaDePago.isEliminado()) {
            throw new IllegalArgumentException("No se puede modificar una forma de pago eliminada");
        }

        formaDePago.setTipoPago(tipo);
        formaDePago.setObservacion(limpiarObservacion(observacion));
        return formaDePagoRepository.save(formaDePago);
    }

    @Override
    @Transactional
    public void eliminarFormaDePago(String id) {
        FormaDePago formaDePago = buscarFormaDePago(id);
        formaDePago.setEliminado(true);
        formaDePagoRepository.save(formaDePago);
    }

    @Override
    public List<FormaDePago> listarFormaDePago() {
        return formaDePagoRepository.findAll();
    }

    @Override
    public List<FormaDePago> listarFormaDePagoActivo() {
        return formaDePagoRepository.findByEliminadoFalse();
    }

    private String limpiarObservacion(String observacion) {
        if (observacion == null || observacion.isBlank()) {
            return null;
        }
        return observacion.trim();
    }
}

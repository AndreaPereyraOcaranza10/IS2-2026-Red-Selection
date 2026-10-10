import React from 'react';

export const BotonConsulta = ({ obtenerClima }) => {
    return (
        <button className="btn btn-primary w-100 mb-3" onClick={obtenerClima}>
            Consultar
        </button>
    );
};
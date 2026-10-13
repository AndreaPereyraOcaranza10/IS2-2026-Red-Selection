import React from 'react';

export const InputCiudad = ({ ciudad, setCiudad }) => {
    return (
        <div className="mb-3">
            <input
                type="text"
                className="form-control"
                placeholder="Ingresa la ciudad (ej: Mendoza, Bogota)"
                value={ciudad}
                onChange={(e) => setCiudad(e.target.value)}
            />
        </div>
    );
};
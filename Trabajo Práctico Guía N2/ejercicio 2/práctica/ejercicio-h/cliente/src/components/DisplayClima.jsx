import React from 'react';
import { traducirClima } from './traduccion';

export const DisplayClima = ({ clima }) => {
    if (!clima || !clima.main) {
        return <p className="text-muted text-center mt-3">No hay información del clima para mostrar.</p>;
    }

    const tempCelsius = (clima.main.temp - 273.15).toFixed(2);
    const descripcion = clima.weather?.[0]?.description || "";

    return (
        <div className="table-responsive mt-3">
            <table className="table table-striped table-bordered text-center align-middle">
                <thead className="table-dark">
                    <tr>
                        <th>Ciudad</th>
                        <th>Temperatura</th>
                        <th>Descripción</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>{clima.name} ({clima.sys?.country})</td>
                        <td>{tempCelsius} °C</td>
                        <td>{traducirClima(descripcion)}</td>
                    </tr>
                </tbody>
            </table>
        </div>
    );
};
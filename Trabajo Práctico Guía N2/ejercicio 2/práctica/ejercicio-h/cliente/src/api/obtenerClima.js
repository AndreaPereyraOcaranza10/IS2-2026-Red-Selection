import axios from 'axios';

export const obtenerClima = async (ciudad, setClima) => {
    try {
        const respuesta = await axios.get(`http://localhost:8085/ciudad/clima/${ciudad}`);
        const data = typeof respuesta.data === 'string' ? JSON.parse(respuesta.data) : respuesta.data;
        setClima(data);
    } catch (error) {
        console.error("Error al obtener el clima:", error);
        setClima(null);
    }
};
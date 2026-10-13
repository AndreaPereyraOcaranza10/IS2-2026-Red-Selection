import { useState } from 'react';
import { InputCiudad } from './components/InputCiudad';
import { BotonConsulta } from './components/BotonConsulta';
import { DisplayClima } from './components/DisplayClima';
import { obtenerClima } from './api/obtenerClima';

function App() {
    const [ciudad, setCiudad] = useState('');
    const [clima, setClima] = useState(null);

    const handleConsultar = () => {
        if (ciudad.trim() !== '') {
            obtenerClima(ciudad, setClima);
        }
    };

    return (
        <div className="container mt-5" style={{ maxWidth: '600px' }}>
            <h2 className="text-center mb-4 fw-semibold text-dark">Consulta del Clima</h2>
            <div className="card p-4 shadow-sm">
                <InputCiudad ciudad={ciudad} setCiudad={setCiudad} />
                <BotonConsulta obtenerClima={handleConsultar} />
                <DisplayClima clima={clima} />
            </div>
        </div>
    );
}

export default App;
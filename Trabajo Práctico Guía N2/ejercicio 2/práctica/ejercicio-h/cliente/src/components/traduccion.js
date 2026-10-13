export const traducirClima = (descripcion) => {
    const traducciones = {
        "clear sky": "Cielo despejado",
        "few clouds": "Pocas nubes",
        "scattered clouds": "Nubes dispersas",
        "broken clouds": "Nubes rotas / Parcialmente nublado",
        "shower rain": "Lluvia intensa",
        "rain": "Lluvia",
        "thunderstorm": "Tormenta eléctrica",
        "snow": "Nieve",
        "mist": "Neblina",
        "overcast clouds": "Cielo cubierto",
        "light rain": "Lluvia ligera",
        "moderate rain": "Lluvia moderada",
        "heavy intensity rain": "Lluvia de alta intensidad",
    };

    return traducciones[descripcion?.toLowerCase()] || descripcion;
};
const express = require('express');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

// Datos de ejemplo (puedes reemplazar con DB real)
let aeropuertos = [
    { code:"JFK", name:"New York - JFK", coords:[40.6413, -73.7781], country:"USA", continent:"North America" },
    { code:"LAX", name:"Los Angeles - LAX", coords:[33.9416, -118.4085], country:"USA", continent:"North America" },
    { code:"ORD", name:"Chicago - O'Hare", coords:[41.9742, -87.9073], country:"USA", continent:"North America" },
    { code:"CDG", name:"Paris - Charles de Gaulle", coords:[49.0097, 2.5479], country:"France", continent:"Europe" },
    { code:"LHR", name:"London Heathrow", coords:[51.4700, -0.4543], country:"UK", continent:"Europe" }
];

// Ruta GET para obtener aeropuertos
app.get('/api/aeropuertos', (req, res) => {
    res.json(aeropuertos);
});

// Ruta POST para agregar aeropuertos (opcional)
app.post('/api/aeropuertos', (req, res) => {
    const nuevo = req.body;
    if (!nuevo.code || !nuevo.name || !nuevo.coords) {
        return res.status(400).json({ error: 'Faltan datos' });
    }
    aeropuertos.push(nuevo);
    res.status(201).json(nuevo);
});

const PORT = 3000;
app.listen(PORT, () => console.log(`Servidor corriendo en http://localhost:${PORT}`));

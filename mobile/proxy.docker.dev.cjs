// Angular corre dentro de Docker y alcanza al backend por el puerto de tu PC.
// /api/** incluye rutas anidadas como /api/v1/mobile/reservas.
module.exports = {
  '/api/**': {
    target: process.env.SKYWAY_DEV_BACKEND_URL || 'http://host.docker.internal:8080',
    secure: false,
    changeOrigin: true,
  },
};

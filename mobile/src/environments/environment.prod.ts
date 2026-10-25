// Web/Docker: ruta relativa. Para APK/IPA configura la URL HTTPS pública del monolito.
import { Capacitor } from '@capacitor/core';

export const environment = {
  production: true,
  apiUrl: Capacitor.isNativePlatform() ? 'http://192.168.100.7:8080/api/v1/mobile' : '/api/v1/mobile'
};

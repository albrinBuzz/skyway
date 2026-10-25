import { Component, inject } from '@angular/core';
import { AppLifecycleService } from './core/app-lifecycle.service';

@Component({
  selector: 'app-root',
  standalone: false,
  template: '<ion-app><ion-router-outlet></ion-router-outlet></ion-app>'
})
export class AppComponent {
  constructor() {
    // Botón/gesto "Atrás" de Android, confirmación de salida y expiración
    // de sesión tras 5 minutos con la app en segundo plano.
    inject(AppLifecycleService).init();
  }
}

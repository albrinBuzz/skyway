import { Component, inject } from '@angular/core';
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import {
  ActionSheetController,
  AlertController,
  ModalController,
  Platform,
  PopoverController
} from '@ionic/angular';

@Component({
  selector: 'app-root',
  standalone: false,
  template: '<ion-app><ion-router-outlet></ion-router-outlet></ion-app>'
})
export class AppComponent {
  private platform = inject(Platform);
  private modalCtrl = inject(ModalController);
  private popoverCtrl = inject(PopoverController);
  private actionSheetCtrl = inject(ActionSheetController);
  private alertCtrl = inject(AlertController);
  private router = inject(Router);
  private location = inject(Location);

  constructor() {
    this.platform.backButton.subscribeWithPriority(100, async () => {
      // 1. Cerrar cualquier overlay o modal activo primero
      const topModal = await this.modalCtrl.getTop();
      if (topModal) {
        await topModal.dismiss();
        return;
      }

      const topPopover = await this.popoverCtrl.getTop();
      if (topPopover) {
        await topPopover.dismiss();
        return;
      }

      const topActionSheet = await this.actionSheetCtrl.getTop();
      if (topActionSheet) {
        await topActionSheet.dismiss();
        return;
      }

      const topAlert = await this.alertCtrl.getTop();
      if (topAlert) {
        await topAlert.dismiss();
        return;
      }

      // Si el panel de configuración de servidor en login está abierto, cerrarlo primero
      const serverCloseBtn = document.querySelector('.server-close-btn') as HTMLElement | null;
      if (serverCloseBtn) {
        serverCloseBtn.click();
        return;
      }

      // Si existe algún backdrop o modal personalizado en el DOM (ej. selector avatar)
      const customModalBackdrop = document.querySelector('.avatar-modal-backdrop') as HTMLElement | null;
      if (customModalBackdrop) {
        customModalBackdrop.click();
        return;
      }

      const avatarModalClose = document.querySelector(
        '.avatar-modal-close, .btn-close-selector'
      ) as HTMLElement | null;
      if (avatarModalClose) {
        avatarModalClose.click();
        return;
      }

      // 2. Control de navegación hacia atrás en base a la ruta actual
      const url = this.router.url.split('?')[0];

      // Flujo de detalle de reserva: rutas hijas (asientos, pases, tickets) -> volver a detalle de reserva
      const matchChild = url.match(/^\/vuelos\/reservas\/(\d+)\/.+/);
      if (matchChild) {
        const idReserva = matchChild[1];
        void this.router.navigateByUrl(`/vuelos/reservas/${idReserva}`);
        return;
      }

      // Detalle de reserva (/vuelos/reservas/:id) -> volver a lista de vuelos
      const matchDetail = url.match(/^\/vuelos\/reservas\/\d+$/);
      if (matchDetail) {
        void this.router.navigateByUrl('/vuelos');
        return;
      }

      // Compatibilidad con rutas directas /reservas/:id/...
      const matchLegacyChild = url.match(/^\/reservas\/(\d+)\/.+/);
      if (matchLegacyChild) {
        void this.router.navigateByUrl(`/vuelos/reservas/${matchLegacyChild[1]}`);
        return;
      }
      if (/^\/reservas\/\d+$/.test(url)) {
        void this.router.navigateByUrl('/vuelos');
        return;
      }

      // 3. Pestañas secundarias vuelven siempre a la pestaña principal /inicio
      if (
        url === '/vuelos' ||
        url === '/historial' ||
        url === '/alertas' ||
        url === '/perfil' ||
        url === '/notificaciones' ||
        url === '/reservas'
      ) {
        void this.router.navigateByUrl('/inicio');
        return;
      }

      // 4. En /inicio o /login se ignora el gesto atrás para no salir accidentalmente de la app
      if (url === '/inicio' || url === '/login' || url === '/' || url === '') {
        return;
      }

      // Cualquier otra ruta: fallback a navegación nativa
      this.location.back();
    });
  }
}

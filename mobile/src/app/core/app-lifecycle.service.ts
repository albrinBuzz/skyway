import { Injectable, NgZone, inject } from '@angular/core';
import {
  NavigationCancel,
  NavigationEnd,
  NavigationError,
  Router
} from '@angular/router';
import { App } from '@capacitor/app';
import { Capacitor } from '@capacitor/core';
import {
  ActionSheetController,
  AlertController,
  ModalController,
  NavController,
  Platform,
  PopoverController
} from '@ionic/angular';
import { filter } from 'rxjs/operators';
import { AuthStore } from './auth.store';

/** Tiempo máximo que la sesión sobrevive con la app en segundo plano (5 minutos). */
const BACKGROUND_SESSION_MS = 5 * 60 * 1000;
const BACKGROUND_SINCE_KEY = 'skyway_bg_since';
const MAX_HISTORY = 50;

/**
 * Controla el botón/gesto "Atrás" del sistema Android y la expiración de la sesión
 * cuando la app pasa a segundo plano.
 *
 * - "Atrás" cierra overlays, luego vuelve a la pantalla anterior visitada y, si ya no
 *   queda nada a qué volver, pregunta si se desea salir de la aplicación.
 * - Al confirmar la salida la sesión se cierra inmediatamente.
 * - Con la app en segundo plano (botón Home, cambio de app) la sesión dura como máximo
 *   5 minutos; el tiempo solo corre mientras la app NO está visible.
 */
@Injectable({ providedIn: 'root' })
export class AppLifecycleService {
  private readonly platform = inject(Platform);
  private readonly router = inject(Router);
  private readonly nav = inject(NavController);
  private readonly zone = inject(NgZone);
  private readonly store = inject(AuthStore);
  private readonly modalCtrl = inject(ModalController);
  private readonly popoverCtrl = inject(PopoverController);
  private readonly actionSheetCtrl = inject(ActionSheetController);
  private readonly alertCtrl = inject(AlertController);

  private historial: string[] = [];
  private navegandoAtras = false;
  private ejecutandoAtras = false;
  private confirmandoSalida = false;
  private iniciado = false;

  init(): void {
    if (this.iniciado) return;
    this.iniciado = true;

    // Al iniciar (o reabrir la app), verificar si quedó una marca de segundo plano
    // y aplicar la regla de los 5 minutos antes de continuar.
    void this.alVolverAlPrimerPlano();

    this.registrarHistorial();
    this.registrarBotonAtras();
    this.registrarCicloDeVida();
  }

  // ---------------------------------------------------------------------------
  // Historial de navegación interno (para un "volver" real entre pantallas/pestañas)
  // ---------------------------------------------------------------------------
  private registrarHistorial(): void {
    this.router.events
      .pipe(
        filter(
          (e): e is NavigationEnd | NavigationCancel | NavigationError =>
            e instanceof NavigationEnd ||
            e instanceof NavigationCancel ||
            e instanceof NavigationError
        )
      )
      .subscribe(e => {
        // Si la navegación se canceló o falló, liberar la bandera navegandoAtras
        if (e instanceof NavigationCancel || e instanceof NavigationError) {
          this.navegandoAtras = false;
          return;
        }

        const url = this.limpiarUrl(e.urlAfterRedirects);

        // Login es siempre una raíz: no se puede "volver" desde ni hacia él.
        if (url === '/login') {
          this.historial = ['/login'];
          this.navegandoAtras = false;
          return;
        }
        if (this.historial[0] === '/login') {
          this.historial = [];
        }

        if (this.navegandoAtras) {
          this.navegandoAtras = false;
          return;
        }

        if (this.historial[this.historial.length - 1] !== url) {
          this.historial.push(url);
          if (this.historial.length > MAX_HISTORY) {
            this.historial.splice(0, this.historial.length - MAX_HISTORY);
          }
        }
      });
  }

  private limpiarUrl(url: string): string {
    const limpia = (url || '/').split('?')[0].split('#')[0];
    return limpia === '/' || limpia === '' ? '/inicio' : limpia;
  }

  // ---------------------------------------------------------------------------
  // Botón / gesto "Atrás" del sistema
  // ---------------------------------------------------------------------------
  private registrarBotonAtras(): void {
    if (Capacitor.isNativePlatform()) {
      // Registrar un listener del plugin App hace que Capacitor NO cierre la app por
      // defecto y que dispare el evento 'backbutton' que Ionic transforma en ionBackButton.
      void App.addListener('backButton', () => { /* manejado por Ionic Platform */ });
    }

    // Prioridad máxima: este manejador decide todo (overlays incluidos) y evita que
    // el manejador por defecto de Ionic (prioridad -1) llame a App.exitApp().
    // Retornar la promesa permite a Ionic esperar y mantener su bandera `busy = true`.
    this.platform.backButton.subscribeWithPriority(9999, () => {
      return this.zone.run(() => this.manejarAtras());
    });
  }

  private async manejarAtras(): Promise<void> {
    if (this.ejecutandoAtras) return;
    this.ejecutandoAtras = true;

    try {
      // 1. Overlays de Ionic (modales inline/controlados, popovers, action sheets, alerts)
      const overlay =
        (await this.alertCtrl.getTop()) ||
        (await this.actionSheetCtrl.getTop()) ||
        (await this.popoverCtrl.getTop()) ||
        (await this.modalCtrl.getTop());
      if (overlay) {
        // Las alertas con backdropDismiss=false (p. ej. la confirmación de salida) se
        // cierran igualmente con "Atrás", equivalente a responder "No".
        await overlay.dismiss(undefined, 'cancel');
        return;
      }

      // 2. Paneles personalizados visibles (panel secreto de servidor, personalizador de avatar)
      const panel = this.primerVisible('.server-close-btn, .btn-close-selector');
      if (panel) {
        panel.click();
        return;
      }

      // 3. Volver a la pantalla anterior realmente visitada
      const actual = this.limpiarUrl(this.router.url);

      // Si el tope del historial coincide con la ruta actual, limpiarlo para apuntar al anterior real
      while (this.historial.length > 1 && this.historial[this.historial.length - 1] === actual) {
        this.historial.pop();
      }

      if (actual !== '/login' && this.historial.length > 1) {
        this.historial.pop();
        const anterior = this.historial[this.historial.length - 1];
        if (anterior !== actual) {
          this.navegandoAtras = true;
          try {
            await this.nav.navigateBack(anterior);
          } finally {
            // Garantizar que navegandoAtras nunca quede congelada en true si
            // NavigationEnd no se disparara (ej. misma ruta, navegación bloqueada).
            this.navegandoAtras = false;
          }
          return;
        }
      }

      // 4. Sin historial: subir al padre lógico de la ruta actual
      const padre = this.rutaPadre(actual);
      if (padre && padre !== actual) {
        this.historial = [padre];
        this.navegandoAtras = true;
        try {
          await this.nav.navigateBack(padre);
        } finally {
          this.navegandoAtras = false;
        }
        return;
      }

      // 5. Ya no hay nada a qué volver: confirmar salida
      await this.confirmarSalida();
    } finally {
      this.ejecutandoAtras = false;
    }
  }

  private primerVisible(selector: string): HTMLElement | null {
    const elementos = Array.from(document.querySelectorAll<HTMLElement>(selector));
    return elementos.find(el => el.offsetParent !== null && el.getClientRects().length > 0) ?? null;
  }

  private rutaPadre(url: string): string | null {
    const hijo = url.match(/^\/vuelos\/reservas\/(\d+)\/.+/);
    if (hijo) return `/vuelos/reservas/${hijo[1]}`;
    if (/^\/vuelos\/reservas\/\d+$/.test(url)) return '/vuelos';
    if (url === '/inicio' || url === '/login') return null;
    return '/inicio';
  }

  private async confirmarSalida(): Promise<void> {
    if (this.confirmandoSalida) return;
    this.confirmandoSalida = true;
    try {
      const alerta = await this.alertCtrl.create({
        header: 'Salir de SkyWay',
        message: '¿Salir realmente de la aplicación?',
        backdropDismiss: false,
        cssClass: 'skyway-exit-alert',
        buttons: [
          { text: 'No', role: 'cancel' },
          { text: 'Sí, salir', role: 'confirm' }
        ]
      });
      await alerta.present();
      const { role } = await alerta.onDidDismiss();
      if (role === 'confirm') {
        this.salirYCerrarSesion();
      }
    } finally {
      this.confirmandoSalida = false;
    }
  }

  private salirYCerrarSesion(): void {
    // La sesión se cierra en el instante exacto en que se confirma la salida.
    this.store.clear();
    this.historial = ['/login'];
    this.limpiarMarcaSegundoPlano();
    void this.router.navigateByUrl('/login', { replaceUrl: true });
    if (Capacitor.isNativePlatform()) {
      void App.exitApp();
    }
  }

  // ---------------------------------------------------------------------------
  // Expiración de sesión en segundo plano (máx. 5 min fuera de la app)
  // ---------------------------------------------------------------------------
  private registrarCicloDeVida(): void {
    if (Capacitor.isNativePlatform()) {
      void App.addListener('appStateChange', ({ isActive }) => {
        this.zone.run(() => void (isActive ? this.alVolverAlPrimerPlano() : this.alIrASegundoPlano()));
      });
    } else {
      document.addEventListener('visibilitychange', () => {
        this.zone.run(() =>
          void (document.visibilityState === 'visible'
            ? this.alVolverAlPrimerPlano()
            : this.alIrASegundoPlano())
        );
      });
    }
  }

  private alIrASegundoPlano(): void {
    if (!this.store.authenticated) return;
    try {
      // Conservar la primera marca si llegan varios eventos seguidos.
      if (!localStorage.getItem(BACKGROUND_SINCE_KEY)) {
        localStorage.setItem(BACKGROUND_SINCE_KEY, String(Date.now()));
      }
    } catch {
      // Almacenamiento no disponible: la sesión simplemente no expira por segundo plano.
    }
  }

  private async alVolverAlPrimerPlano(): Promise<void> {
    let desde = 0;
    try {
      desde = Number(localStorage.getItem(BACKGROUND_SINCE_KEY) || 0);
    } catch {
      desde = 0;
    }
    this.limpiarMarcaSegundoPlano();

    if (!desde || !this.store.authenticated) return;
    if (Date.now() - desde < BACKGROUND_SESSION_MS) return;

    // Más de 5 minutos fuera de la app: cerrar sesión y pedir login nuevamente.
    const returnUrl = this.limpiarUrl(this.router.url);

    // Cerrar cualquier overlay abierto (modales, alertas, popovers, action sheets)
    try {
      let overlay =
        (await this.alertCtrl.getTop()) ||
        (await this.actionSheetCtrl.getTop()) ||
        (await this.popoverCtrl.getTop()) ||
        (await this.modalCtrl.getTop());
      while (overlay) {
        await overlay.dismiss(undefined, 'cancel');
        overlay =
          (await this.alertCtrl.getTop()) ||
          (await this.actionSheetCtrl.getTop()) ||
          (await this.popoverCtrl.getTop()) ||
          (await this.modalCtrl.getTop());
      }
    } catch {
      // Ignorar errores al cerrar overlays
    }

    this.store.clear();
    this.historial = ['/login'];
    void this.router.navigate(['/login'], {
      replaceUrl: true,
      queryParams: { expired: '1', returnUrl }
    });
  }

  private limpiarMarcaSegundoPlano(): void {
    try {
      localStorage.removeItem(BACKGROUND_SINCE_KEY);
    } catch {
      // Ignorar
    }
  }
}

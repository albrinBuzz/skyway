import { Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { addIcons } from 'ionicons';
import {
  airplane,
  airplaneOutline,
  alertCircle,
  alertCircleOutline,
  arrowForwardOutline,
  checkmarkCircle,
  closeOutline,
  cloudOutline,
  desktopOutline,
  eyeOffOutline,
  eyeOutline,
  globeOutline,
  homeOutline,
  informationCircle,
  informationCircleOutline,
  lockClosedOutline,
  mailOutline,
  refreshOutline,
  serverOutline,
  shieldCheckmarkOutline,
  timeOutline,
  wifiOutline
} from 'ionicons/icons';
import {
  ApiService,
  AZURE_CLOUD_HOST,
  DEFAULT_MIPC_HOST,
  HOME_MIPC_HOST
} from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { AuthStore } from '../../core/auth.store';
import { errorMessage } from '../../core/ui';

addIcons({
  airplane,
  airplaneOutline,
  alertCircle,
  alertCircleOutline,
  arrowForwardOutline,
  checkmarkCircle,
  closeOutline,
  cloudOutline,
  desktopOutline,
  eyeOffOutline,
  eyeOutline,
  globeOutline,
  homeOutline,
  informationCircle,
  informationCircleOutline,
  lockClosedOutline,
  mailOutline,
  refreshOutline,
  serverOutline,
  shieldCheckmarkOutline,
  timeOutline,
  wifiOutline
});

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss']
})
export class LoginPage {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private store = inject(AuthStore);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  readonly azureHost = AZURE_CLOUD_HOST;
  readonly defaultMipcHost = DEFAULT_MIPC_HOST;
  readonly homeMipcHost = HOME_MIPC_HOST;

  modoServidor: 'azure' | 'mipc' | 'otro' = this.api.getServerMode();
  probandoConexion = false;
  estadoConexion: { ok: boolean; mensaje: string } | null = null;

  form = inject(FormBuilder).nonNullable.group({
    servidor: [this.api.getServerHost()],
    correoElectronico: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
    contrasena: ['', [Validators.required, Validators.maxLength(72)]]
  });

  busy = false;
  error = '';
  mostrarServidor = false;
  mostrarContrasena = false;
  private dotClicks = 0;
  private dotTimer: ReturnType<typeof setTimeout> | null = null;

  get expired() {
    return this.route.snapshot.queryParamMap.get('expired') === '1';
  }

  get servidorActual(): string {
    if (this.modoServidor === 'otro' && !this.form.controls.servidor.value?.trim()) {
      return 'Sin configurar (ingresa una IP o URL)';
    }
    return this.api.getServerHost() || 'Proxy local (/api/v1/mobile)';
  }

  cerrarServidor(): void {
    this.mostrarServidor = false;
    this.estadoConexion = null;
  }

  seleccionarPresetServidor(modo: 'azure' | 'mipc' | 'otro'): void {
    this.modoServidor = modo;
    this.api.setServerMode(modo);
    this.estadoConexion = null;

    if (modo === 'azure') {
      this.form.controls.servidor.setValue(this.azureHost);
      this.api.setServerHost(this.azureHost);
    } else if (modo === 'mipc') {
      const saved = this.api.getSavedHostForMode('mipc') || this.defaultMipcHost;
      const normalized = this.api.normalizeOrigin(saved) || this.defaultMipcHost;
      this.form.controls.servidor.setValue(normalized);
      this.api.setServerHost(normalized);
    } else {
      const saved = this.api.getSavedHostForMode('otro');
      this.form.controls.servidor.setValue(saved);
      if (saved && saved.trim()) {
        const normalized = this.api.normalizeOrigin(saved.trim());
        if (normalized) {
          this.api.setServerHost(normalized);
        } else {
          this.api.setServerHost('');
        }
      } else {
        this.api.setServerHost('');
      }
    }
  }

  seleccionarSubPresetMipc(url: string): void {
    const normalized = this.api.normalizeOrigin(url) || url;
    this.form.controls.servidor.setValue(normalized);
    this.api.setSavedHostForMode('mipc', normalized);
    this.api.setServerHost(normalized);
    this.estadoConexion = null;
  }

  onServidorInput(): void {
    this.estadoConexion = null;
    const raw = (this.form.controls.servidor.value || '').trim();
    if (this.modoServidor === 'mipc' || this.modoServidor === 'otro') {
      this.api.setSavedHostForMode(this.modoServidor, raw);
      if (raw) {
        const normalized = this.api.normalizeOrigin(raw);
        if (normalized) {
          this.api.setServerHost(normalized);
        }
      }
    }
  }

  guardarServidor(): void {
    const raw = (this.form.controls.servidor.value || '').trim();
    if (this.modoServidor === 'azure') {
      this.api.setServerHost(this.azureHost);
      this.form.controls.servidor.setValue(this.azureHost);
      this.mostrarServidor = false;
    } else if (this.modoServidor === 'mipc') {
      const host = raw || this.defaultMipcHost;
      const normalized = this.api.normalizeOrigin(host) || this.defaultMipcHost;
      this.api.setSavedHostForMode('mipc', normalized);
      this.api.setServerHost(normalized);
      this.form.controls.servidor.setValue(normalized);
      this.mostrarServidor = false;
    } else {
      if (!raw) {
        this.api.setServerHost('');
        this.api.setSavedHostForMode('otro', '');
        this.estadoConexion = { ok: false, mensaje: 'Ingresa la IP o URL del servidor para guardar.' };
        return;
      }
      const normalized = this.api.normalizeOrigin(raw);
      if (!normalized) {
        this.estadoConexion = { ok: false, mensaje: 'URL o IP inválida. Usa formato 192.168.1.50:8080' };
        return;
      }
      this.api.setSavedHostForMode('otro', normalized);
      this.api.setServerHost(normalized);
      this.form.controls.servidor.setValue(normalized);
      this.mostrarServidor = false;
    }
  }

  async probarConexion(): Promise<void> {
    if (this.probandoConexion) return;
    this.probandoConexion = true;
    this.estadoConexion = null;
    const currentVal = (this.form.controls.servidor.value || '').trim();

    if (!currentVal && this.modoServidor === 'otro') {
      this.estadoConexion = { ok: false, mensaje: 'Ingresa una IP o URL antes de probar.' };
      this.probandoConexion = false;
      return;
    }

    const normalized = currentVal ? this.api.normalizeOrigin(currentVal) : this.api.getServerHost();
    if (!normalized) {
      this.estadoConexion = { ok: false, mensaje: 'URL o IP inválida. Usa formato 192.168.1.50:8080' };
      this.probandoConexion = false;
      return;
    }

    this.form.controls.servidor.setValue(normalized);
    this.api.setServerHost(normalized);
    if (this.modoServidor === 'mipc' || this.modoServidor === 'otro') {
      this.api.setSavedHostForMode(this.modoServidor, normalized);
    }

    try {
      const res = await firstValueFrom(this.api.probarConexion(normalized));
      this.estadoConexion = { ok: res.ok, mensaje: res.mensaje };
    } catch {
      this.estadoConexion = { ok: false, mensaje: 'Error al verificar conexión' };
    } finally {
      this.probandoConexion = false;
    }
  }

  private getSafeReturnUrl(): string {
    const raw = this.route.snapshot.queryParamMap.get('returnUrl');
    if (!raw || typeof raw !== 'string') return '/inicio';

    const trimmed = raw.trim();
    const isSafeInternal =
      /^\/(inicio|vuelos|historial|alertas|perfil|home|reservas|notificaciones)(?:[/?#]|$)/i.test(
        trimmed
      ) &&
      !trimmed.startsWith('//') &&
      !trimmed.startsWith('/\\') &&
      !trimmed.includes('//') &&
      !trimmed.includes('\\');

    return isSafeInternal ? trimmed : '/inicio';
  }

  ionViewWillEnter() {
    this.modoServidor = this.api.getServerMode();
    if (this.modoServidor === 'azure') {
      this.form.controls.servidor.setValue(this.azureHost);
    } else if (this.modoServidor === 'mipc') {
      this.form.controls.servidor.setValue(
        this.api.getSavedHostForMode('mipc') || this.defaultMipcHost
      );
    } else {
      this.form.controls.servidor.setValue(this.api.getSavedHostForMode('otro'));
    }

    this.mostrarServidor = false;
    this.mostrarContrasena = false;
    this.estadoConexion = null;
    this.dotClicks = 0;
    if (this.store.authenticated) {
      void this.router.navigateByUrl(this.getSafeReturnUrl(), { replaceUrl: true });
    }
  }

  toggleMostrarContrasena(): void {
    this.mostrarContrasena = !this.mostrarContrasena;
  }

  onBrandDotTap() {
    this.dotClicks++;
    if (this.dotTimer) clearTimeout(this.dotTimer);
    if (this.dotClicks >= 5) {
      this.dotClicks = 0;
      this.mostrarServidor = !this.mostrarServidor;
      if (this.mostrarServidor) {
        this.modoServidor = this.api.getServerMode();
        if (this.modoServidor === 'azure') {
          this.form.controls.servidor.setValue(this.azureHost);
        } else if (this.modoServidor === 'mipc') {
          this.form.controls.servidor.setValue(
            this.api.getSavedHostForMode('mipc') || this.defaultMipcHost
          );
        } else {
          this.form.controls.servidor.setValue(this.api.getSavedHostForMode('otro'));
        }
        this.estadoConexion = null;
      }
      return;
    }
    this.dotTimer = setTimeout(() => {
      this.dotClicks = 0;
    }, 2000);
  }

  async login() {
    if (this.form.invalid || this.busy) {
      this.form.markAllAsTouched();
      return;
    }

    const data = this.form.getRawValue();
    const serverVal = (data.servidor || '').trim();

    if (this.modoServidor === 'otro') {
      if (!serverVal) {
        this.error = 'Debes ingresar la IP o URL del servidor en el modo "IP Amigo / Otra".';
        this.mostrarServidor = true;
        return;
      }
      const normalized = this.api.normalizeOrigin(serverVal);
      if (!normalized) {
        this.error = 'La dirección del servidor no es válida. Usa el formato 192.168.1.50:8080 o https://...';
        this.mostrarServidor = true;
        return;
      }
      this.api.setServerHost(normalized);
      this.api.setSavedHostForMode('otro', normalized);
      this.form.controls.servidor.setValue(normalized);
    } else if (this.modoServidor === 'mipc') {
      const target = serverVal || this.defaultMipcHost;
      const normalized = this.api.normalizeOrigin(target) || this.defaultMipcHost;
      this.api.setServerHost(normalized);
      this.api.setSavedHostForMode('mipc', normalized);
      this.form.controls.servidor.setValue(normalized);
    } else if (this.modoServidor === 'azure') {
      this.api.setServerHost(this.azureHost);
      this.form.controls.servidor.setValue(this.azureHost);
    }

    this.busy = true;
    this.error = '';
    try {
      await firstValueFrom(this.auth.login(data.correoElectronico.trim(), data.contrasena));
      this.form.controls.contrasena.reset();
      const targetUrl = this.getSafeReturnUrl();
      await this.router.navigateByUrl(targetUrl, { replaceUrl: true });
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.busy = false;
    }
  }
}

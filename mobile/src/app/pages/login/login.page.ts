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
  cloudOutline,
  desktopOutline,
  eyeOffOutline,
  eyeOutline,
  globeOutline,
  informationCircle,
  informationCircleOutline,
  lockClosedOutline,
  mailOutline,
  serverOutline,
  shieldCheckmarkOutline,
  timeOutline
} from 'ionicons/icons';
import { ApiService, AZURE_CLOUD_HOST, DEFAULT_NATIVE_HOST } from '../../core/api.service';
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
  cloudOutline,
  desktopOutline,
  eyeOffOutline,
  eyeOutline,
  globeOutline,
  informationCircle,
  informationCircleOutline,
  lockClosedOutline,
  mailOutline,
  serverOutline,
  shieldCheckmarkOutline,
  timeOutline
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

  form = inject(FormBuilder).nonNullable.group({
    servidor: [this.api.getServerHost()],
    correoElectronico: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
    contrasena: ['', [Validators.required, Validators.maxLength(72)]]
  });

  busy = false;
  error = '';
  mostrarServidor = false;
  mostrarContrasena = false;
  readonly azureHost = AZURE_CLOUD_HOST;
  readonly myPcHost = DEFAULT_NATIVE_HOST;
  private dotClicks = 0;
  private dotTimer: ReturnType<typeof setTimeout> | null = null;

  get expired() { return this.route.snapshot.queryParamMap.get('expired') === '1'; }
  get servidorActual() { return this.api.getServerHost() || 'Proxy local (/api/v1/mobile)'; }
  get modoServidorActual(): 'azure' | 'mipc' | 'otro' {
    const val = (this.form.controls.servidor.value || this.api.getServerHost() || '').trim().toLowerCase();
    if (val.includes('azurewebsites.net')) return 'azure';
    if (val.includes('192.168.100.7')) return 'mipc';
    return 'otro';
  }

  seleccionarPresetServidor(modo: 'azure' | 'mipc' | 'otro'): void {
    if (modo === 'azure') {
      this.form.controls.servidor.setValue(this.azureHost);
      this.api.setServerHost(this.azureHost);
    } else if (modo === 'mipc') {
      this.form.controls.servidor.setValue(this.myPcHost);
      this.api.setServerHost(this.myPcHost);
    } else {
      const current = this.form.controls.servidor.value;
      if (current === this.azureHost || current === this.myPcHost) {
        this.form.controls.servidor.setValue('');
      }
    }
  }

  private getSafeReturnUrl(): string {
    const raw = this.route.snapshot.queryParamMap.get('returnUrl');
    if (!raw || typeof raw !== 'string') return '/inicio';

    const trimmed = raw.trim();
    // Protección estricta contra Open Redirect (OWASP MASVS / Angular):
    // 1) Debe iniciar con un solo '/' seguido de una ruta interna de la app.
    // 2) Rechaza URLs relativas a protocolo ('//evil.com'), barras invertidas ('/\\evil.com'), o esquemas externos.
    const isSafeInternal = /^\/(inicio|vuelos|historial|alertas|perfil|home|reservas|notificaciones)(?:[/?#]|$)/i.test(trimmed)
      && !trimmed.startsWith('//')
      && !trimmed.startsWith('/\\')
      && !trimmed.includes('//')
      && !trimmed.includes('\\');

    return isSafeInternal ? trimmed : '/inicio';
  }

  ionViewWillEnter() {
    this.form.controls.servidor.setValue(this.api.getServerHost());
    this.mostrarServidor = false;
    this.mostrarContrasena = false;
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
        this.form.controls.servidor.setValue(this.api.getServerHost());
      }
      return;
    }
    this.dotTimer = setTimeout(() => {
      this.dotClicks = 0;
    }, 2000);
  }

  guardarServidor() {
    this.api.setServerHost(this.form.controls.servidor.value);
    this.form.controls.servidor.setValue(this.api.getServerHost());
    this.mostrarServidor = false;
  }

  async login() {
    if (this.form.invalid || this.busy) {
      this.form.markAllAsTouched();
      return;
    }
    this.busy = true;
    this.error = '';
    try {
      const data = this.form.getRawValue();
      if (this.mostrarServidor) {
        this.api.setServerHost(data.servidor);
      }
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

import { Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { AuthStore } from '../../core/auth.store';
import { errorMessage } from '../../core/ui';

@Component({ selector: 'app-login', standalone: false, templateUrl: './login.page.html', styleUrls: ['./login.page.scss'] })
export class LoginPage {
  private api = inject(ApiService);
  private auth = inject(AuthService); private store = inject(AuthStore);
  private router = inject(Router); private route = inject(ActivatedRoute);
  form = inject(FormBuilder).nonNullable.group({
    servidor: [this.api.getServerHost()],
    correoElectronico: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
    contrasena: ['', [Validators.required, Validators.maxLength(72)]]
  });
  busy = false; error = ''; mostrarServidor = false;
  get expired() { return this.route.snapshot.queryParamMap.get('expired') === '1'; }
  get servidorActual() { return this.api.getServerHost() || 'Proxy local (/api/v1/mobile)'; }
  ionViewWillEnter() {
    this.form.controls.servidor.setValue(this.api.getServerHost());
    if (this.store.authenticated) void this.router.navigateByUrl('/home', { replaceUrl: true });
  }
  toggleServidor() { this.mostrarServidor = !this.mostrarServidor; }
  guardarServidor() {
    this.api.setServerHost(this.form.controls.servidor.value);
    this.form.controls.servidor.setValue(this.api.getServerHost());
    this.mostrarServidor = false;
  }
  async login() {
    if (this.form.invalid || this.busy) { this.form.markAllAsTouched(); return; }
    this.busy = true; this.error = '';
    try {
      const data = this.form.getRawValue();
      this.api.setServerHost(data.servidor);
      await firstValueFrom(this.auth.login(data.correoElectronico.trim(), data.contrasena));
      this.form.controls.contrasena.reset();
      const next = this.route.snapshot.queryParamMap.get('returnUrl');
      await this.router.navigateByUrl(next && /^\/(home|reservas|notificaciones)(\/|$|\?)/.test(next) ? next : '/home', { replaceUrl: true });
    } catch (e) { this.error = errorMessage(e); } finally { this.busy = false; }
  }
}

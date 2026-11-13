import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { ApiService } from './api.service';
import { AuthStore } from './auth.store';
import { RegistroRequest } from './models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private api = inject(ApiService);
  private store = inject(AuthStore);
  private router = inject(Router);
  login(correo: string, password: string, recordarSesion = false) {
    this.store.setKeepSession(recordarSesion);
    this.store.setLastEmail(correo);
    return this.api.login(correo, password, recordarSesion).pipe(tap(data => this.store.set(data)));
  }
  registro(data: RegistroRequest) { return this.api.registro(data).pipe(tap(res => this.store.set(res))); }
  logout() { this.store.clear(); void this.router.navigateByUrl('/login', { replaceUrl: true }); }
}

import { Injectable } from '@angular/core';
import { Perfil, LoginResponse } from './models';

/** Token solo en memoria: no persiste en localStorage ni en Preferences sin cifrar. */
@Injectable({ providedIn: 'root' })
export class AuthStore {
  private token: string | null = null;
  private expiresAt = 0;
  perfil: Perfil | null = null;
  set(data: LoginResponse) { this.token = data.accessToken; this.expiresAt = Date.now() + data.expiresIn * 1000; this.perfil = data.perfil; }
  get accessToken() { if (Date.now() >= this.expiresAt) this.clear(); return this.token; }
  get authenticated() { return this.accessToken !== null; }
  clear() { this.token = null; this.expiresAt = 0; this.perfil = null; }
}

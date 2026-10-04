import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { ApiService } from './api.service';
import { AuthStore } from './auth.store';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private api = inject(ApiService);
  private store = inject(AuthStore);
  private router = inject(Router);
  login(correo: string, password: string) { return this.api.login(correo, password).pipe(tap(data => this.store.set(data))); }
  logout() { this.store.clear(); void this.router.navigateByUrl('/login', { replaceUrl: true }); }
}

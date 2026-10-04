import { Component, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Inicio } from '../../core/models';
import { errorMessage } from '../../core/ui';
@Component({ selector: 'app-home', standalone: false, templateUrl: './home.page.html' })
export class HomePage {
  private api = inject(ApiService); auth = inject(AuthService);
  data: Inicio | null = null; busy = false; error = '';
  ionViewWillEnter() { void this.load(); }
  async load() {
    this.busy = true; this.error = ''; this.data = null;
    try { this.data = await firstValueFrom(this.api.inicio()); }
    catch(e) { this.error = errorMessage(e); } finally { this.busy = false; }
  }
}

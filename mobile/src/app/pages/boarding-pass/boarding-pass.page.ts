import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import QRCode from 'qrcode';
import { ApiService } from '../../core/api.service';
import { PaseAbordar, VistaTicket } from '../../core/models';
import { errorMessage } from '../../core/ui';

@Component({ selector: 'app-boarding-pass', standalone: false, templateUrl: './boarding-pass.page.html', styleUrls: ['./boarding-pass.page.scss'] })
export class BoardingPassPage {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  get id(): number { return Number(this.route.snapshot.paramMap.get('id')); }
  get vuelo(): number { return Number(this.route.snapshot.paramMap.get('vuelo')); }
  get rut(): string | null { return this.route.snapshot.paramMap.get('rut'); }
  data: VistaTicket | null = null;
  qr = '';
  error = '';
  busy = false;

  ionViewWillEnter() { void this.load(); }
  async load() {
    this.busy = true; this.error = ''; this.data = null; this.qr = '';
    try {
      if (this.rut) {
        this.data = await firstValueFrom(this.api.vistaTicket(this.id, this.vuelo, this.rut));
      } else {
        // Compatibilidad: enlaces antiguos al pase individual.
        const old: PaseAbordar = await firstValueFrom(this.api.pase(this.id, this.vuelo));
        this.data = { ...old, equipajes: [], codigoPnr: null };
      }
      if (this.data.qrContenido) {
        this.qr = await QRCode.toDataURL(this.data.qrContenido, { width: 256, margin: 3, errorCorrectionLevel: 'M' });
      }
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.busy = false;
    }
  }
}

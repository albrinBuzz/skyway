import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import QRCode from 'qrcode';
import { ApiService } from '../../core/api.service';
import { PaseAbordar } from '../../core/models';
import { errorMessage } from '../../core/ui';
@Component({ selector: 'app-boarding-pass', standalone: false, templateUrl: './boarding-pass.page.html', styleUrls: ['./boarding-pass.page.scss'] })
export class BoardingPassPage {
  private api = inject(ApiService); private route = inject(ActivatedRoute);
  id = Number(this.route.snapshot.paramMap.get('id')); vuelo = Number(this.route.snapshot.paramMap.get('vuelo'));
  data: PaseAbordar | null = null; qr = ''; error = ''; busy = false;
  ionViewWillEnter() { void this.load(); }
  async load() {
    this.busy = true; this.error = ''; this.data = null; this.qr = '';
    try { this.data = await firstValueFrom(this.api.pase(this.id,this.vuelo)); this.qr = await QRCode.toDataURL(this.data.qrContenido, { width: 256, margin: 3, errorCorrectionLevel: 'M' }); }
    catch(e) { this.error = errorMessage(e); } finally { this.busy = false; }
  }
}

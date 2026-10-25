import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ToastController } from '@ionic/angular';
import { firstValueFrom } from 'rxjs';
import QRCode from 'qrcode';
import { ApiService } from '../../core/api.service';
import { PaseAbordar, Segmento, VistaTicket } from '../../core/models';
import { errorMessage } from '../../core/ui';
import { addIcons } from 'ionicons';
import {
  airplane,
  airplaneOutline,
  alertCircle,
  arrowBack,
  briefcaseOutline,
  businessOutline,
  calendarOutline,
  checkmarkCircle,
  closeCircle,
  copyOutline,
  enterOutline,
  helpCircle,
  informationCircle,
  locationOutline,
  personCircleOutline,
  pricetagOutline,
  qrCodeOutline,
  refreshOutline,
  shieldCheckmarkOutline,
  ticketOutline,
  timeOutline
} from 'ionicons/icons';

// Registrar íconos utilizados por la tarjeta de embarque
addIcons({
  airplane,
  airplaneOutline,
  alertCircle,
  arrowBack,
  briefcaseOutline,
  businessOutline,
  calendarOutline,
  checkmarkCircle,
  closeCircle,
  copyOutline,
  enterOutline,
  helpCircle,
  informationCircle,
  locationOutline,
  personCircleOutline,
  pricetagOutline,
  qrCodeOutline,
  refreshOutline,
  shieldCheckmarkOutline,
  ticketOutline,
  timeOutline
});

@Component({
  selector: 'app-boarding-pass',
  standalone: false,
  templateUrl: './boarding-pass.page.html',
  styleUrls: ['./boarding-pass.page.scss']
})
export class BoardingPassPage {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly toasts = inject(ToastController);

  get id(): number { return Number(this.route.snapshot.paramMap.get('id')); }
  get vuelo(): number { return Number(this.route.snapshot.paramMap.get('vuelo')); }
  get rut(): string | null { return this.route.snapshot.paramMap.get('rut'); }

  data: VistaTicket | null = null;
  qr = '';
  error = '';
  busy = false;

  get primerSegmento(): Segmento | null {
    return this.data?.vuelo?.segmentos?.[0] ?? null;
  }

  get ultimoSegmento(): Segmento | null {
    const s = this.data?.vuelo?.segmentos;
    return s && s.length ? s[s.length - 1] : null;
  }

  get seatRoute(): (string | number)[] {
    return this.rut
      ? ['/vuelos/reservas', this.id, 'vuelos', this.vuelo, 'tickets', this.rut, 'asientos']
      : ['/vuelos/reservas', this.id, 'vuelos', this.vuelo, 'asientos'];
  }

  ionViewWillEnter(): void {
    void this.load();
  }

  async load(): Promise<void> {
    this.busy = true;
    this.error = '';
    this.data = null;
    this.qr = '';

    try {
      if (this.rut) {
        this.data = await firstValueFrom(this.api.vistaTicket(this.id, this.vuelo, this.rut));
      } else {
        // Compatibilidad: enlaces al pase individual de la reserva
        const old: PaseAbordar = await firstValueFrom(this.api.pase(this.id, this.vuelo));
        this.data = {
          ...old,
          equipajes: [],
          codigoPnr: null
        };
      }

      if (this.data.qrContenido) {
        this.qr = await QRCode.toDataURL(this.data.qrContenido, {
          width: 260,
          margin: 2,
          errorCorrectionLevel: 'M',
          color: {
            dark: '#101e3b',
            light: '#ffffff'
          }
        });
      }
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.busy = false;
    }
  }

  async copyPnr(pnr: string): Promise<void> {
    if (!pnr) return;
    try {
      if (typeof navigator !== 'undefined' && navigator.clipboard) {
        await navigator.clipboard.writeText(pnr);
      }
      const t = await this.toasts.create({
        message: `Localizador ${pnr} copiado al portapapeles`,
        duration: 2200,
        color: 'success'
      });
      await t.present();
    } catch {
      // Ignorar si clipboard no está disponible
    }
  }
}

import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ToastController } from '@ionic/angular';
import { firstValueFrom } from 'rxjs';
import QRCode from 'qrcode';
import { ApiService } from '../../core/api.service';
import { HapticsService } from '../../core/haptics.service';
import { OfflinePassService } from '../../core/offline-pass.service';
import { PaseAbordar, Segmento, VistaTicket } from '../../core/models';
import { errorMessage, saveOrSharePdfBlob } from '../../core/ui';
import { addIcons } from 'ionicons';
import {
  airplane,
  airplaneOutline,
  alertCircle,
  arrowBack,
  barcodeOutline,
  briefcaseOutline,
  businessOutline,
  calendarOutline,
  checkboxOutline,
  checkmarkCircle,
  chevronDownOutline,
  closeCircle,
  copyOutline,
  downloadOutline,
  enterOutline,
  helpCircle,
  informationCircle,
  locationOutline,
  lockClosedOutline,
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
  barcodeOutline,
  briefcaseOutline,
  businessOutline,
  calendarOutline,
  checkboxOutline,
  checkmarkCircle,
  chevronDownOutline,
  closeCircle,
  copyOutline,
  downloadOutline,
  enterOutline,
  helpCircle,
  informationCircle,
  locationOutline,
  lockClosedOutline,
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
  private readonly offline = inject(OfflinePassService);
  private readonly haptics = inject(HapticsService);

  get id(): number { return Number(this.route.snapshot.paramMap.get('id')); }
  get vuelo(): number { return Number(this.route.snapshot.paramMap.get('vuelo')); }
  get rut(): string | null { return this.route.snapshot.paramMap.get('rut'); }

  data: VistaTicket | null = null;
  qr = '';
  barcodeUrl = '';
  codigoIataBcbp = '';
  error = '';
  busy = false;
  checkinPendiente = false;
  descargandoPdf = false;
  modoOffline = false;
  offlineSavedAt: number | null = null;
  showSegments = false;
  showBaggage = false;
  showIataInfo = false;

  resumenEquipaje(): string {
    const list = this.data?.equipajes ?? [];
    if (!list.length) return 'Sin equipaje adicional registrado';
    const kg = list.reduce((acc, b) => acc + (b.peso || 0), 0);
    return `${list.length} ${list.length === 1 ? 'maleta' : 'maletas'} · ${Math.round(kg * 10) / 10} kg`;
  }

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
    this.checkinPendiente = false;
    this.modoOffline = false;
    this.offlineSavedAt = null;
    this.data = null;
    this.qr = '';
    this.barcodeUrl = '';
    this.codigoIataBcbp = '';

    try {
      if (this.rut) {
        this.data = await firstValueFrom(this.api.vistaTicket(this.id, this.vuelo, this.rut));
      } else {
        // Compatibilidad: enlaces al pase individual de la reserva
        const old: PaseAbordar = await firstValueFrom(this.api.pase(this.id, this.vuelo));
        this.data = {
          ...old,
          equipajes: [],
          codigoPnr: old.codigoPnr ?? null,
          codigoIataBcbp: old.codigoIataBcbp ?? old.qrContenido,
          barcodeBase64: old.barcodeBase64 ?? null
        };
      }

      if (this.data && !this.data.checkin) {
        this.checkinPendiente = true;
      }

      if (this.data.barcodeBase64) {
        this.barcodeUrl = this.data.barcodeBase64;
      }
      if (this.data.codigoIataBcbp) {
        this.codigoIataBcbp = this.data.codigoIataBcbp;
      } else if (this.data.qrContenido) {
        this.codigoIataBcbp = this.data.qrContenido;
      }

      // Fallback a QR si no hubiera imagen barcodeBase64
      if (!this.barcodeUrl && this.data.qrContenido) {
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

      if (this.data && this.data.checkin) {
        this.offline.save(this.id, this.vuelo, this.rut, this.data, this.qr, this.barcodeUrl);
      }
    } catch (e: any) {
      const esErrorLogico =
        e?.status === 401 ||
        e?.status === 403 ||
        e?.status === 409 ||
        e?.error?.codigo === 'CHECKIN_PENDIENTE' ||
        /check-?in/i.test(errorMessage(e));

      const cached = !esErrorLogico ? this.offline.get(this.id, this.vuelo, this.rut) : null;
      if (cached) {
        this.data = cached.data;
        this.qr = cached.qrDataUrl || '';
        this.barcodeUrl = cached.barcodeUrl || cached.data.barcodeBase64 || '';
        this.codigoIataBcbp = cached.data.codigoIataBcbp || cached.data.qrContenido || '';
        this.modoOffline = true;
        this.offlineSavedAt = cached.savedAt;
        this.error = '';
      } else {
        this.error = errorMessage(e);
        if (
          e?.status === 409 ||
          e?.error?.codigo === 'CHECKIN_PENDIENTE' ||
          /check-?in/i.test(this.error)
        ) {
          this.checkinPendiente = true;
        }
      }
    } finally {
      this.busy = false;
    }
  }

  async descargarImagenQr(): Promise<void> {
    const imgSrc = this.barcodeUrl || this.qr;
    if (!imgSrc) return;
    this.haptics.light();
    const a = document.createElement('a');
    a.href = imgSrc;
    a.download = `Pase_QR_SkyWay_${this.data?.codigoPnr || this.id}.png`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    const t = await this.toasts.create({
      message: 'Imagen del pase guardada para uso sin conexión.',
      duration: 2400,
      color: 'success'
    });
    await t.present();
  }

  descargarPdf(): void {
    if (this.descargandoPdf) return;
    this.descargandoPdf = true;
    this.api.descargarBoardingPassPdf(this.id).subscribe({
      next: async (blob: Blob) => {
        this.descargandoPdf = false;
        await saveOrSharePdfBlob(blob, `BoardingPass_${this.data?.codigoPnr || ('Reserva_' + this.id)}.pdf`);
      },
      error: (err) => {
        this.descargandoPdf = false;
        this.error = errorMessage(err);
      }
    });
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

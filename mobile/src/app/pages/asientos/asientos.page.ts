import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AlertController, ToastController } from '@ionic/angular';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Asiento, MapaAsientos } from '../../core/models';
import { errorMessage } from '../../core/ui';
interface SeatRow { label: string; asientos: Asiento[]; }
@Component({ selector: 'app-asientos', standalone: false, templateUrl: './asientos.page.html', styleUrls: ['./asientos.page.scss'] })
export class AsientosPage {
  private api = inject(ApiService); private route = inject(ActivatedRoute); private alerts = inject(AlertController); private toasts = inject(ToastController);
  id = Number(this.route.snapshot.paramMap.get('id')); vuelo = Number(this.route.snapshot.paramMap.get('vuelo'));
  data: MapaAsientos | null = null; selected: Asiento | null = null; rows: SeatRow[] = []; busy = false; saving = false; error = '';
  ionViewWillEnter() { void this.load(); }
  private setData(map: MapaAsientos) {
    this.data = map; this.selected = null; const groups = new Map<string, Asiento[]>();
    [...map.asientos].sort((a,b) => a.numeroAsiento.localeCompare(b.numeroAsiento, 'es', { numeric: true })).forEach(a => {
      const label = a.numeroAsiento.match(/^\d+/)?.[0] ?? 'Otros';
      groups.set(label, [...(groups.get(label) ?? []), a]);
    }); this.rows = [...groups].map(([label,asientos]) => ({ label, asientos }));
  }
  async load() {
    this.busy = true; this.error = ''; this.selected = null; this.data = null;
    try { this.setData(await firstValueFrom(this.api.asientos(this.id,this.vuelo))); }
    catch(e) { this.error = errorMessage(e); } finally { this.busy = false; }
  }
  choose(a: Asiento) { if(a.seleccionable && !this.saving) this.selected = a; }
  async save() {
    if(!this.selected || this.saving) return;
    const seat = this.selected;
    const prompt = await this.alerts.create({ header: `Cambiar al asiento ${seat.numeroAsiento}`, message: 'El cambio conserva la clase de tu asiento actual.', buttons: [{ text: 'Volver', role: 'cancel' }, { text: 'Confirmar', role: 'confirm' }] });
    await prompt.present(); if((await prompt.onDidDismiss()).role !== 'confirm') return;
    this.saving = true; this.error = '';
    try {
      this.setData(await firstValueFrom(this.api.cambiarAsiento(this.id,this.vuelo,seat.idAsiento)));
      const toast = await this.toasts.create({ message: 'Asiento actualizado.', duration: 2500, color: 'success' }); await toast.present();
    } catch(e) {
      const message = errorMessage(e);
      try { this.setData(await firstValueFrom(this.api.asientos(this.id,this.vuelo))); } catch { this.selected = null; }
      this.error = message;
    } finally { this.saving = false; }
  }
}

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { IonicModule } from '@ionic/angular';
import { VuelosPage } from './vuelos.page';
import { GloboMapaComponent } from './globo-mapa/globo-mapa.component';

@NgModule({
  declarations: [VuelosPage, GloboMapaComponent],
  imports: [CommonModule, FormsModule, IonicModule, RouterModule.forChild([{ path: '', component: VuelosPage }])]
})
export class VuelosPageModule {}

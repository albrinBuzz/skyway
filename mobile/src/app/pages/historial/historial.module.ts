import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { IonicModule } from '@ionic/angular';
import { HistorialPage } from './historial.page';

@NgModule({
  declarations: [HistorialPage],
  imports: [CommonModule, FormsModule, IonicModule, RouterModule.forChild([{ path: '', component: HistorialPage }])]
})
export class HistorialPageModule {}

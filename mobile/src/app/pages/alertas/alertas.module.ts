import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { IonicModule } from '@ionic/angular';
import { AlertasPage } from './alertas.page';

@NgModule({
  declarations: [AlertasPage],
  imports: [CommonModule, FormsModule, IonicModule, RouterModule.forChild([{ path: '', component: AlertasPage }])]
})
export class AlertasPageModule {}

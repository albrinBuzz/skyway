import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { IonicModule } from '@ionic/angular';
import { NotificacionesPage } from './notificaciones.page';
@NgModule({ declarations: [NotificacionesPage], imports: [CommonModule, FormsModule, ReactiveFormsModule, IonicModule, RouterModule.forChild([{ path: '', component: NotificacionesPage }])] })
export class NotificacionesPageModule {}

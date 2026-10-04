import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { IonicModule } from '@ionic/angular';
import { AsientosPage } from './asientos.page';
@NgModule({ declarations: [AsientosPage], imports: [CommonModule, FormsModule, ReactiveFormsModule, IonicModule, RouterModule.forChild([{ path: '', component: AsientosPage }])] })
export class AsientosPageModule {}

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { IonicModule } from '@ionic/angular';
import { BoardingPassPage } from './boarding-pass.page';
@NgModule({ declarations: [BoardingPassPage], imports: [CommonModule, FormsModule, ReactiveFormsModule, IonicModule, RouterModule.forChild([{ path: '', component: BoardingPassPage }])] })
export class BoardingPassPageModule {}

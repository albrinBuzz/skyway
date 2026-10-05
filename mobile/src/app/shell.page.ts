import { Component } from '@angular/core';

@Component({
  selector: 'app-shell',
  standalone: false,
  template: `
    <ion-tabs>
      <ion-tab-bar slot="bottom">
        <ion-tab-button tab="inicio" routerLink="/inicio" routerDirection="root">
          <ion-icon name="search-outline"></ion-icon>
          <ion-label>Inicio</ion-label>
        </ion-tab-button>

        <ion-tab-button tab="vuelos" routerLink="/vuelos" routerDirection="root">
          <ion-icon name="airplane-outline"></ion-icon>
          <ion-label>Vuelos</ion-label>
        </ion-tab-button>

        <ion-tab-button tab="historial" routerLink="/historial" routerDirection="root">
          <ion-icon name="time-outline"></ion-icon>
          <ion-label>Historial</ion-label>
        </ion-tab-button>

        <ion-tab-button tab="alertas" routerLink="/alertas" routerDirection="root">
          <ion-icon name="notifications-outline"></ion-icon>
          <ion-label>Alertas</ion-label>
        </ion-tab-button>

        <ion-tab-button tab="perfil" routerLink="/perfil" routerDirection="root">
          <ion-icon name="person-circle-outline"></ion-icon>
          <ion-label>Perfil</ion-label>
        </ion-tab-button>
      </ion-tab-bar>
    </ion-tabs>
  `
})
export class ShellPage {}

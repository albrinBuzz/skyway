import { Component } from '@angular/core';
@Component({ selector: 'app-shell', standalone: false, template: `
<ion-tabs><ion-tab-bar slot="bottom">
<ion-tab-button tab="home" href="/home"><ion-icon name="home-outline"></ion-icon><ion-label>Inicio</ion-label></ion-tab-button>
<ion-tab-button tab="reservas" href="/reservas"><ion-icon name="ticket-outline"></ion-icon><ion-label>Mis reservas</ion-label></ion-tab-button>
<ion-tab-button tab="notificaciones" href="/notificaciones"><ion-icon name="notifications-outline"></ion-icon><ion-label>Alertas</ion-label></ion-tab-button>
</ion-tab-bar></ion-tabs>` })
export class ShellPage {}

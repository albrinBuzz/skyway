import { Component } from '@angular/core';
import { addIcons } from 'ionicons';
import {
  airplane,
  airplaneOutline,
  notifications,
  notificationsOutline,
  personCircle,
  personCircleOutline,
  search,
  searchOutline,
  time,
  timeOutline
} from 'ionicons/icons';

addIcons({
  airplane,
  airplaneOutline,
  notifications,
  notificationsOutline,
  personCircle,
  personCircleOutline,
  search,
  searchOutline,
  time,
  timeOutline
});

@Component({
  selector: 'app-shell',
  standalone: false,
  template: `
    <ion-tabs>
      <ion-tab-bar slot="bottom" class="skyway-tab-bar">
        <ion-tab-button tab="inicio" routerLink="/inicio" routerDirection="root" aria-label="Ir a Inicio y búsqueda de vuelos">
          <ion-icon name="search-outline" aria-hidden="true"></ion-icon>
          <ion-label>Inicio</ion-label>
        </ion-tab-button>

        <ion-tab-button tab="vuelos" routerLink="/vuelos" routerDirection="root" aria-label="Ir a Mis Vuelos y reservas activas">
          <ion-icon name="airplane-outline" aria-hidden="true"></ion-icon>
          <ion-label>Vuelos</ion-label>
        </ion-tab-button>

        <ion-tab-button tab="historial" routerLink="/historial" routerDirection="root" aria-label="Ir al Historial de reservas y viajes">
          <ion-icon name="time-outline" aria-hidden="true"></ion-icon>
          <ion-label>Historial</ion-label>
        </ion-tab-button>

        <ion-tab-button tab="alertas" routerLink="/alertas" routerDirection="root" aria-label="Ir a Notificaciones y alertas de vuelo">
          <ion-icon name="notifications-outline" aria-hidden="true"></ion-icon>
          <ion-label>Alertas</ion-label>
        </ion-tab-button>

        <ion-tab-button tab="perfil" routerLink="/perfil" routerDirection="root" aria-label="Ir a Mi Perfil de pasajero">
          <ion-icon name="person-circle-outline" aria-hidden="true"></ion-icon>
          <ion-label>Perfil</ion-label>
        </ion-tab-button>
      </ion-tab-bar>
    </ion-tabs>
  `,
  styles: [`
    .skyway-tab-bar {
      --background: #ffffff;
      --border: 1px solid #e3e9f3;
      height: 60px;
      padding-bottom: env(safe-area-inset-bottom);
      box-shadow: 0 -3px 18px rgba(16, 30, 59, 0.08);
      border-top: 1px solid #e3e9f3;
      width: 100%;
      max-width: 100vw;
      box-sizing: border-box;
      overflow-x: hidden;
    }

    ion-tab-button {
      --color: #4a5a75;
      --color-selected: #1b38a6;
      --ripple-color: rgba(36, 73, 216, 0.12);
      --padding-start: 2px;
      --padding-end: 2px;
      min-height: 48px;
      min-width: 0;
      flex: 1 1 0%;
      padding: 4px 2px;
      position: relative;
      transition: all 0.2s ease;
      box-sizing: border-box;
      overflow: hidden;
    }

    ion-tab-button ion-icon {
      font-size: 20px;
      margin-bottom: 2px;
      transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1), color 0.15s ease;
    }

    ion-tab-button ion-label {
      font-size: 11.5px;
      font-weight: 700;
      letter-spacing: -0.2px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 100%;
      text-align: center;
      display: block;
      transition: color 0.15s ease;
    }

    /* Active pill indicator at the top of the selected tab */
    ion-tab-button::before {
      content: '';
      position: absolute;
      top: 0;
      left: 50%;
      transform: translateX(-50%) scaleX(0);
      width: 28px;
      height: 3px;
      border-radius: 0 0 3px 3px;
      background: #2449d8;
      transition: transform 0.24s cubic-bezier(0.34, 1.56, 0.64, 1);
    }

    ion-tab-button.tab-selected::before {
      transform: translateX(-50%) scaleX(1);
    }

    ion-tab-button.tab-selected ion-icon {
      transform: scale(1.1);
      color: #2449d8;
    }

    ion-tab-button.tab-selected ion-label {
      font-weight: 800;
      color: #1a349c;
    }

    @media (max-width: 360px) {
      .skyway-tab-bar {
        height: 56px;
      }
      ion-tab-button {
        padding: 3px 1px;
        min-width: 0;
        min-height: 44px;
      }
      ion-tab-button ion-icon {
        font-size: 18px;
        margin-bottom: 1px;
      }
      ion-tab-button ion-label {
        font-size: 10.5px;
        letter-spacing: -0.3px;
      }
      ion-tab-button::before {
        width: 22px;
        height: 2.5px;
      }
    }

    @media (min-width: 900px) {
      .skyway-tab-bar {
        justify-content: center;
      }
      ion-tab-button {
        max-width: 180px;
      }
    }
  `]
})
export class ShellPage {}

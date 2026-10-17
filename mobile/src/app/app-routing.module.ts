import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { ShellPage } from './shell.page';

const routes: Routes = [
  {
    path: 'login',
    loadChildren: () => import('./pages/login/login.module').then(m => m.LoginPageModule)
  },
  {
    path: '',
    component: ShellPage,
    canActivate: [authGuard],
    canActivateChild: [authGuard],
    children: [
      {
        path: 'inicio',
        loadChildren: () => import('./pages/inicio/inicio.module').then(m => m.InicioPageModule)
      },
      {
        path: 'vuelos',
        loadChildren: () => import('./pages/vuelos/vuelos.module').then(m => m.VuelosPageModule)
      },
      {
        path: 'historial',
        loadChildren: () => import('./pages/historial/historial.module').then(m => m.HistorialPageModule)
      },
      {
        path: 'alertas',
        loadChildren: () => import('./pages/alertas/alertas.module').then(m => m.AlertasPageModule)
      },
      {
        path: 'perfil',
        loadChildren: () => import('./pages/perfil/perfil.module').then(m => m.PerfilPageModule)
      },

      // Flujo de una reserva desde la sección Vuelos.
      {
        path: 'vuelos/reservas/:id',
        loadChildren: () => import('./pages/reserva-detalle/reserva-detalle.module').then(m => m.ReservaDetallePageModule)
      },
      {
        path: 'vuelos/reservas/:id/vuelos/:vuelo/asientos',
        loadChildren: () => import('./pages/asientos/asientos.module').then(m => m.AsientosPageModule)
      },
      // Ticket individual por pasajero. El backend comprueba la autorización del titular.
      // Ruta de asientos antes de la vista corta para evitar coincidencias parciales.
      {
        path: 'vuelos/reservas/:id/vuelos/:vuelo/tickets/:rut/asientos',
        loadChildren: () => import('./pages/asientos/asientos.module').then(m => m.AsientosPageModule)
      },
      {
        path: 'vuelos/reservas/:id/vuelos/:vuelo/tickets/:rut',
        loadChildren: () => import('./pages/boarding-pass/boarding-pass.module').then(m => m.BoardingPassPageModule)
      },
      {
        path: 'vuelos/reservas/:id/vuelos/:vuelo/pase',
        loadChildren: () => import('./pages/boarding-pass/boarding-pass.module').then(m => m.BoardingPassPageModule)
      },

      // Compatibilidad con enlaces antiguos del cliente móvil.
      { path: 'home', pathMatch: 'full', redirectTo: 'inicio' },
      { path: 'reservas', pathMatch: 'full', redirectTo: 'vuelos' },
      { path: 'reservas/:id', redirectTo: 'vuelos/reservas/:id' },
      { path: 'reservas/:id/vuelos/:vuelo/asientos', redirectTo: 'vuelos/reservas/:id/vuelos/:vuelo/asientos' },
      { path: 'reservas/:id/vuelos/:vuelo/pase', redirectTo: 'vuelos/reservas/:id/vuelos/:vuelo/pase' },
      { path: 'notificaciones', pathMatch: 'full', redirectTo: 'alertas' },
      { path: '', pathMatch: 'full', redirectTo: 'inicio' }
    ]
  },
  { path: '**', redirectTo: 'inicio' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}

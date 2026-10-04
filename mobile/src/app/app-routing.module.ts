import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { ShellPage } from './shell.page';
const routes: Routes = [
 { path: 'login', loadChildren: () => import('./pages/login/login.module').then(m => m.LoginPageModule) },
 { path: '', component: ShellPage, canActivate: [authGuard], canActivateChild: [authGuard], children: [
   { path: 'home', loadChildren: () => import('./pages/home/home.module').then(m => m.HomePageModule) },
   { path: 'reservas', loadChildren: () => import('./pages/reservas/reservas.module').then(m => m.ReservasPageModule) },
   { path: 'reservas/:id', loadChildren: () => import('./pages/reserva-detalle/reserva-detalle.module').then(m => m.ReservaDetallePageModule) },
   { path: 'reservas/:id/vuelos/:vuelo/asientos', loadChildren: () => import('./pages/asientos/asientos.module').then(m => m.AsientosPageModule) },
   { path: 'reservas/:id/vuelos/:vuelo/pase', loadChildren: () => import('./pages/boarding-pass/boarding-pass.module').then(m => m.BoardingPassPageModule) },
   { path: 'notificaciones', loadChildren: () => import('./pages/notificaciones/notificaciones.module').then(m => m.NotificacionesPageModule) },
   { path: '', pathMatch: 'full', redirectTo: 'home' }
 ] },
 { path: '**', redirectTo: 'home' }
];
@NgModule({ imports: [RouterModule.forRoot(routes)], exports: [RouterModule] })
export class AppRoutingModule {}

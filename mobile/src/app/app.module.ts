import { NgModule, LOCALE_ID } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouteReuseStrategy } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { registerLocaleData } from '@angular/common';
import localeEsCl from '@angular/common/locales/es-CL';
import { IonicModule, IonicRouteStrategy } from '@ionic/angular';
import { addIcons } from 'ionicons';
import { airplaneOutline, homeOutline, ticketOutline, notificationsOutline, logOutOutline, arrowForwardOutline, checkmarkCircleOutline, refreshOutline } from 'ionicons/icons';
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { ShellPage } from './shell.page';
import { authInterceptor } from './core/auth.interceptor';
registerLocaleData(localeEsCl);
addIcons({ airplaneOutline, homeOutline, ticketOutline, notificationsOutline, logOutOutline, arrowForwardOutline, checkmarkCircleOutline, refreshOutline });
@NgModule({ declarations: [AppComponent, ShellPage], imports: [BrowserModule, IonicModule.forRoot({ mode: 'md' }), AppRoutingModule],
providers: [{ provide: RouteReuseStrategy, useClass: IonicRouteStrategy }, { provide: LOCALE_ID, useValue: 'es-CL' }, provideHttpClient(withInterceptors([authInterceptor]))], bootstrap: [AppComponent] })
export class AppModule {}

import { Component, inject } from '@angular/core';
import { AppLifecycleService } from './core/app-lifecycle.service';
import { NotificationsLiveService } from './core/notifications-live.service';
import { ThemeService } from './core/theme.service';

@Component({
  selector: 'app-root',
  standalone: false,
  template: '<ion-app><ion-router-outlet></ion-router-outlet></ion-app>'
})
export class AppComponent {
  constructor() {
    inject(ThemeService).init();
    inject(AppLifecycleService).init();
    inject(NotificationsLiveService).start();
  }
}

import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ApiService } from './api.service';
import { AuthStore } from './auth.store';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const store = inject(AuthStore);
  const router = inject(Router);
  const api = inject(ApiService);
  const base = api.apiBaseUrl;
  const ourApi = req.url === base || req.url.startsWith(`${base}/`) || req.url.includes('/api/v1/mobile/');
  const login = req.url.endsWith('/auth/login');
  const token = ourApi && !login ? store.accessToken : null;
  const sent = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
  return next(sent).pipe(catchError((error: HttpErrorResponse) => {
    if (ourApi && !login && error.status === 401) {
      store.clear();
      void router.navigate(['/login'], { queryParams: { expired: '1' }, replaceUrl: true });
    }
    return throwError(() => error);
  }));
};

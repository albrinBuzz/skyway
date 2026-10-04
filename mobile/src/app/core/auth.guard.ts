import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from './auth.store';
export const authGuard: CanActivateFn = (_route, state) => inject(AuthStore).authenticated
  || inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });

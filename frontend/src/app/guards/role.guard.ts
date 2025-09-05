// src/app/core/guards/role.guard.ts
import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AutenticacaoService } from '../services/autenticacao.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
    const auth = inject(AutenticacaoService);
    const router = inject(Router);

    const user = auth.getCurrentUser();
    const required: string[] = route.data?.['roles'] ?? [];

    if (!auth.isAuthenticated()) return router.createUrlTree(['/login']);
    if (!required.length) return true;

    const has = user?.roles?.some(r => required.includes(r as any));
    return has ? true : router.createUrlTree(['/']);
};

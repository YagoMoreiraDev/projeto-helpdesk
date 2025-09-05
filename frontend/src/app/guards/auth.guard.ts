// src/app/core/guards/auth.guard.ts
import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { AutenticacaoService } from '../services/autenticacao.service';

export const authGuard: CanActivateFn = () => {
    const auth = inject(AutenticacaoService);
    const router = inject(Router);
    if (auth.isAuthenticated()) return true;
    return router.createUrlTree(['/login']);
};

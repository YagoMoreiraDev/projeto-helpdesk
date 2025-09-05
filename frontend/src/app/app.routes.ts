// src/app/app.routes.ts
import { Routes } from '@angular/router';
import { LayoutComponent } from './template/layout/layout.component';
import { authGuard } from '../app/guards/auth.guard';
import { roleGuard } from '../app/guards/role.guard';

// src/app/app.routes.ts
export const routes: Routes = [
    { path: 'login', loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent) },

    {
        path: '',
        canActivate: [authGuard],
        loadComponent: () => Promise.resolve(LayoutComponent),
        children: [
            // Em vez do redirect fixo:
            {
                path: '',
                pathMatch: 'full',
                loadComponent: () =>
                    import('./pages/role-redirect/role-redirect.component')
                        .then(m => m.RoleRedirectComponent)
            },

            // Cliente
            {
                path: 'cliente/chamados',
                canActivate: [roleGuard],
                data: { roles: ['USUARIO_COMUM', 'ADMIN'] },
                loadComponent: () => import('./pages/chamados-page/chamados-page.component')
                    .then(m => m.ChamadosPageComponent)
            },

            // Técnico
            {
                path: 'tecnico/chamados',
                canActivate: [roleGuard],
                data: { roles: ['TECNICO', 'ADMIN'] },
                loadComponent: () => import('./pages/chamados-tecnicos-page/chamados-tecnicos-page.component')
                    .then(m => m.ChamadosTecnicosPage)
            },
            {
                path: 'tecnico/dashboard',
                canActivate: [roleGuard],
                data: { roles: ['TECNICO', 'ADMIN'] },
                loadComponent: () => import('./pages/dashboard-ti/dashboard-ti.component')
                    .then(m => m.DashboardTiComponent)
            },

            // Admin
            {
                path: 'admin/chamados',
                canActivate: [roleGuard],
                data: { roles: ['ADMIN'] },
                loadComponent: () => import('./pages/chamados-admin-page/chamados-admin-page.component')
                    .then(m => m.ChamadosAdminPage) // << corrigido
            },
            {
                path: 'admin/dashboard',
                canActivate: [roleGuard],
                data: { roles: ['ADMIN'] },
                loadComponent: () => import('./pages/dashboard-ti/dashboard-ti.component')
                    .then(m => m.DashboardTiComponent)
            },
        ],
    },

    { path: '**', redirectTo: '' },
];

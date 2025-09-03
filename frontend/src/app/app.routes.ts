import { Routes } from '@angular/router';
import { LayoutComponent } from './template/layout/layout.component';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () => Promise.resolve(LayoutComponent),
        children: [
            { path: '', pathMatch: 'full', redirectTo: 'cliente/chamados' },

            // Cliente
            {
                path: 'cliente/chamados',
                loadComponent: () => import('./pages/chamados-page/chamados-page.component')
                    .then(m => m.ChamadosPageComponent)
            },

            // Técnico
            {
                path: 'tecnico/chamados',
                loadComponent: () => import('./pages/chamados-tecnicos-page/chamados-tecnicos-page.component')
                    .then(m => m.ChamadosTecnicosPage)
            },
            {
                path: 'tecnico/dashboard',
                loadComponent: () => import('./pages/dashboard-ti/dashboard-ti.component')
                    .then(m => m.DashboardTiComponent)
            },

            // Admin
            {
                path: 'admin/chamados',
                loadComponent: () => import('./pages/chamados-page/chamados-page.component')
                    .then(m => m.ChamadosPageComponent)
            },
            {
                path: 'admin/dashboard',
                loadComponent: () => import('./pages/dashboard-ti/dashboard-ti.component')
                    .then(m => m.DashboardTiComponent)
            },
        ],
    },

    { path: '**', redirectTo: '' },
];

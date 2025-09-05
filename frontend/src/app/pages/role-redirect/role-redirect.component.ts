// src/app/pages/role-redirect/role-redirect.component.ts
import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AutenticacaoService } from '../../services/autenticacao.service';
import { take } from 'rxjs/operators';

@Component({ standalone: true, template: '' })
export class RoleRedirectComponent implements OnInit {
    private auth = inject(AutenticacaoService);
    private router = inject(Router);

    ngOnInit(): void {
        this.auth.currentUser$()            // seu observable de usuário
            .pipe(take(1))                    // lê só uma vez
            .subscribe(u => {
                const roles: string[] = (u?.roles ?? []) as string[];

                if (roles.includes('ADMIN')) this.router.navigateByUrl('/admin/chamados');
                else if (roles.includes('TECNICO')) this.router.navigateByUrl('/tecnico/chamados');
                else this.router.navigateByUrl('/cliente/chamados');
            });
    }
}

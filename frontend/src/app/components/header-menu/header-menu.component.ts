import { CommonModule } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { AutenticacaoService } from '../../services/autenticacao.service';

type Role = 'USUARIO_COMUM' | 'TECNICO' | 'ADMIN';
type MenuItem = { label: string; route: string };

const MENU_BY_ROLE: Record<Role, MenuItem[]> = {
  USUARIO_COMUM: [
    { label: 'Meus Chamados', route: '/cliente/chamados' },
    { label: 'Criar Chamado', route: 'modal:criar-chamado' },
  ],
  TECNICO: [
    { label: 'Meus Chamados', route: '/tecnico/chamados' },
    { label: 'Dashboard',     route: '/tecnico/dashboard' },
  ],
  ADMIN: [
    { label: 'Dashboard',     route: '/admin/dashboard' },
    { label: 'Chamados',      route: '/admin/chamados' },
    { label: 'Técnicos',      route: '/admin/tecnicos' },
    { label: 'Serviços',      route: '/admin/servicos' },
  ],
};

@Component({
  selector: 'app-header-menu',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './header-menu.component.html',
  styleUrls: ['./header-menu.component.css'],
})
export class HeaderMenuComponent {
  private auth = inject(AutenticacaoService);
  private router = inject(Router);

  user = toSignal(this.auth.currentUser$(), { initialValue: null });
  isLoggingOut = false;

  role = computed<Role>(() => {
    const u = this.user();
    const roles = (u?.roles ?? []) as Role[];
    if (roles.includes('ADMIN'))   return 'ADMIN';
    if (roles.includes('TECNICO')) return 'TECNICO';
    return 'USUARIO_COMUM';
  });

  items = computed<MenuItem[]>(() => MENU_BY_ROLE[this.role()]);

  logout() {
    if (this.isLoggingOut) return;
    this.isLoggingOut = true;

    this.auth.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => {
        // Mesmo que falhe, a service já limpa a sessão (finalize).
        this.router.navigate(['/login']);
      },
      complete: () => (this.isLoggingOut = false),
    });
  }
}

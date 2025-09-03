import { CommonModule } from '@angular/common';
import { Component, computed, Input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

type Role = 'CLIENTE' | 'TECNICO' | 'ADMIN';

type MenuItem = { label: string; route: string };

const MENU_BY_ROLE: Record<Role, MenuItem[]> = {
  CLIENTE: [
    { label: 'Meus Chamados', route: '/cliente/chamados' },
    { label: 'Criar Chamado', route: 'modal:criar-chamado' }, // continua abrindo o modal
  ],
  TECNICO: [
    { label: 'Meus Chamados', route: '/tecnico/chamados' },
    { label: 'Dashboard',     route: '/tecnico/dashboard' },
  ],
  ADMIN: [
    { label: 'Dashboard',     route: '/admin/dashboard' },
    { label: 'Chamados',      route: '/admin/chamados' },
    { label: 'Técnicos',      route: '/admin/tecnicos' },   // só crie se existir a rota
    { label: 'Serviços',      route: '/admin/servicos' },   // idem
  ],
};

@Component({
  selector: 'app-header-menu',
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './header-menu.component.html',
  styleUrl: './header-menu.component.css'
})
export class HeaderMenuComponent {
  @Input() role: Role = 'CLIENTE'; 

  items = computed(() => MENU_BY_ROLE[this.role] ?? []);
}

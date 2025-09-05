import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';

import { ChamadoService } from '../../services/chamado.service';
import { UsuarioService } from '../../services/usuario.service';
import { NotificationClientService } from '../../services/notification-client.service';

import { ChamadosTableComponent, Chamado as UiChamado } from '../chamados-table/chamados-table.component';
import { toUiChamado, toApiStatus } from '../../core/adapters/chamado.adapter';
import { StatusChamado } from '../../core/models/status-chamado.enum';

// helpers
function upsertById<T extends { id: string }>(arr: T[], item: T): T[] {
  const i = arr.findIndex(a => a.id === item.id);
  if (i === -1) return [item, ...arr];
  const next = arr.slice();
  next[i] = { ...next[i], ...item };
  return next;
}
function removeById<T extends { id: string }>(arr: T[], id: string): T[] {
  return arr.filter(x => x.id !== id);
}

@Component({
  selector: 'app-chamados-admin-page',
  standalone: true,
  imports: [ChamadosTableComponent],
  templateUrl: './chamados-admin-page.component.html',
  styleUrls: ['./chamados-admin-page.component.css']
})
export class ChamadosAdminPage implements OnInit, OnDestroy {
  private readonly chamados = inject(ChamadoService);
  private readonly usuarios = inject(UsuarioService);
  private readonly notif    = inject(NotificationClientService);

  private readonly destroy$ = new Subject<void>();

  // expõe o enum pro template
  readonly ST = StatusChamado;

  lista: UiChamado[] = [];
  tecnicos: { id: string; nome: string }[] = [];

  // filtro usa enum da API
  filtroAtual: 'ALL' | StatusChamado = 'ALL';

  ngOnInit(): void {
    this.usuarios
      .tecnicos()
      .subscribe(ts => this.tecnicos = ts.map(t => ({ id: t.id, nome: t.nome })));

    this.load();

    // 🔔 SSE: reaja aos eventos do backend e mantenha a grid atualizada
    this.notif.stream$()
      .pipe(
        takeUntil(this.destroy$),
        filter(n => ['CHAMADO_CRIADO', 'CHAMADO_ATRIBUIDO', 'STATUS_ALTERADO'].includes(n.type))
      )
      .subscribe(n => {
        const payload: any = n.payload; // ChamadoResponse
        if (!payload) return;

        // se o evento combina com o filtro atual, upsert; senão remove da lista (caso esteja visível)
        if (this.filtroAtual === 'ALL' || payload.status === this.filtroAtual) {
          const ui = toUiChamado(payload);
          this.lista = upsertById(this.lista, ui);
        } else {
          this.lista = removeById(this.lista, payload.id);
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  load(): void {
    const src = this.filtroAtual === 'ALL'
      ? this.chamados.listarTodos()
      : this.chamados.listarPorStatus(this.filtroAtual);

    src.subscribe({
      next: arr => this.lista = arr.map(toUiChamado),
      error: () => this.lista = []
    });
  }

  filtrar(st: 'ALL' | StatusChamado) {
    this.filtroAtual = st;
    this.load(); // troca de filtro recarrega a fonte (deixa simples e consistente)
  }

  clearFiltro() {
    this.filtroAtual = 'ALL';
    this.load();
  }

  // ===== Ações =====
  onDesignar(e: { id: string; tecnicoId: string }) {
    this.chamados.designar(e.id as any, e.tecnicoId as any).subscribe({
      next: () => { /* SSE vai atualizar a grid automaticamente */ },
      error: () => this.load()
    });
  }

  onAlterarStatus(e: { id: string; novo: 'EM_ABERTO' | 'EM_ANDAMENTO' | 'CONCLUIDO' }) {
    this.chamados.alterarStatus(e.id as any, {
      novoStatus: toApiStatus(e.novo),
      detalhe: 'Alterado pelo ADMIN'
    }).subscribe({
      next: () => { /* SSE atualiza a grid */ },
      error: () => this.load()
    });
  }

  onCancelar(id: string) {
    this.chamados.cancelarChamado(id as any, 'Cancelado pelo ADMIN').subscribe({
      next: () => { /* SSE atualiza a grid */ },
      error: () => this.load()
    });
  }
}

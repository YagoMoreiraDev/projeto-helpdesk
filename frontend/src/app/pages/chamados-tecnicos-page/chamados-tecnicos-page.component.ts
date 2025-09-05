// src/app/pages/chamados-tecnicos-page/chamados-tecnicos-page.component.ts
import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { Subject, forkJoin } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';

import { ChamadoService } from '../../services/chamado.service';
import { NotificationClientService } from '../../services/notification-client.service';
import { AutenticacaoService } from '../../services/autenticacao.service';

import {
  ChamadosTecnicosComponent,
  Chamado as UiChamado,
} from '../chamados-tecnicos/chamados-tecnicos.component';

import { toUiChamado, toApiStatus } from '../../core/adapters/chamado.adapter';

// helpers
function upsertById<T extends { id: string }>(arr: T[], item: T): T[] {
  const i = arr.findIndex(a => a.id === item.id);
  if (i === -1) return [item, ...arr];
  const next = arr.slice();
  next[i] = { ...next[i], ...item };
  return next;
}
function dedup<T extends { id: string }>(arr: T[]): T[] {
  const seen = new Set<string>();
  return arr.filter(x => (seen.has(x.id) ? false : (seen.add(x.id), true)));
}

@Component({
  standalone: true,
  selector: 'app-chamados-tecnicos-page',
  imports: [ChamadosTecnicosComponent],
  template: `
<section class="container py-4">
  <h2 class="mb-3">Chamados (Técnico)</h2>

  <app-chamados-tecnicos
    [tecnicoAtual]="tecnicoNome"
    [chamados]="lista"
    (iniciar)="onIniciar($event)"
    (encerrar)="onEncerrar($event)"
    (repassar)="onRepassar($event)">
  </app-chamados-tecnicos>
</section>
  `
})
export class ChamadosTecnicosPage implements OnInit, OnDestroy {
  private readonly svc   = inject(ChamadoService);
  private readonly notif = inject(NotificationClientService);
  private readonly auth  = inject(AutenticacaoService);

  private readonly destroy$ = new Subject<void>();

  lista: UiChamado[] = [];
  tecnicoId?: string;
  tecnicoNome = '—';

  ngOnInit(): void {
    // pega o usuário atual do serviço de auth
    const u = this.auth.getCurrentUser();
    this.tecnicoId = u?.id;
    this.tecnicoNome = u?.nome ?? '—';

    this.reload();

    // 🔔 SSE: reaja aos eventos vindos do backend
    this.notif.stream$()
      .pipe(
        takeUntil(this.destroy$),
        filter(n => ['CHAMADO_CRIADO', 'CHAMADO_ATRIBUIDO', 'STATUS_ALTERADO'].includes(n.type))
      )
      .subscribe(n => {
        const payload = n.payload; // ChamadoResponse
        if (!payload) return;
        const ui = toUiChamado(payload);
        this.lista = upsertById(this.lista, ui);
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /** Carrega “Abertos” + “Meus” e faz merge sem duplicatas */
  private reload(): void {
    forkJoin([
      this.svc.listarEmAberto(),
      this.svc.listarPorTecnico()
    ]).subscribe({
      next: ([abertos, meus]) => {
        this.lista = dedup([...abertos, ...meus].map(toUiChamado));
      },
      error: () => { this.lista = []; }
    });
  }

  // ===== Handlers vindos do componente de UI =====

  onIniciar(e: { id: string; tecnico: string }) {
    this.svc.assumirChamado(e.id as any).subscribe({
      next: () => { /* SSE atualizará a lista */ },
      error: () => this.reload()
    });
  }

  onEncerrar(e: { id: string; tecnico: string }) {
    this.svc.alterarStatus(e.id as any, {
      novoStatus: toApiStatus('CONCLUIDO'),
      detalhe: 'Encerrado pelo técnico'
    }).subscribe({
      next: () => { /* SSE atualizará a lista */ },
      error: () => this.reload()
    });
  }

  onRepassar(e: { id: string }) {
    // TODO: quando criar endpoint para repassar/desatribuir:
    // this.svc.repassarChamado(e.id as any).subscribe({ next: ..., error: ... });
    this.reload();
  }
}

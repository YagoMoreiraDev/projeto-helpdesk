// src/app/pages/chamados-page/chamados-page.component.ts
import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChamadosTableComponent, Chamado, StatusChamado } from '../chamados-table/chamados-table.component';
import { ChamadoService } from '../../services/chamado.service';
import { NotificationClientService } from '../../services/notification-client.service';
import { toUiChamado, toApiStatus } from '../../core/adapters/chamado.adapter';
import { upsertById } from '../../core/utils/array.utils';
import { filter } from 'rxjs/operators';

@Component({
  standalone: true,
  selector: 'app-chamados-page',
  imports: [CommonModule, ChamadosTableComponent],
  templateUrl: './chamados-page.component.html',
})
export class ChamadosPageComponent implements OnInit {
  private svc = inject(ChamadoService);
  private notif = inject(NotificationClientService);

  listaChamados: Chamado[] = [];
  loading = false;

  ngOnInit(): void {
    this.load();

    this.notif.stream$()
      .pipe(
        // Cliente só precisa reagir a eventos que afetam sua lista
        filter(n => ['CHAMADO_CRIADO','STATUS_ALTERADO','CHAMADO_ATRIBUIDO'].includes(n.type))
      )
      .subscribe(n => {
        const payload = n.payload; // é ChamadoResponse
        if (!payload) return;

        // Converte pro UI e faz upsert
        const ui = toUiChamado(payload);
        this.listaChamados = upsertById(this.listaChamados, ui);
      });
  }

  load() {
    this.loading = true;
    this.svc.listarMeusChamados().subscribe({
      next: arr => { this.listaChamados = arr.map(toUiChamado); this.loading = false; },
      error: ()   => { this.listaChamados = []; this.loading = false; }
    });
  }

  handleAlterarStatus(e:{id:string; novo:StatusChamado}) {
    this.svc.alterarStatus(e.id as any, {
      novoStatus: toApiStatus(e.novo),
      detalhe: 'Alterado pela interface do cliente'
    }).subscribe();
    // não precisa atualizar aqui — o SSE empurrará a mudança
  }

  handleExcluir(id:string) {
    this.svc.excluirChamado(id as any).subscribe();
    // idem: o SSE/STATUS_ALTERADO para CANCELADO deve refletir
    // Se preferir UX otimista: remova localmente e o SSE confirmará
    this.listaChamados = this.listaChamados.filter(c => c.id !== id);
  }

  handleAtribuir(_: {id:string; tecnico:string}) {}
}

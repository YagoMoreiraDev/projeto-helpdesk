// src/app/pages/chamados-table/chamados-table.component.ts
import { CommonModule, DatePipe, NgClass } from '@angular/common';
import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';

export type Role = 'USUARIO_COMUM' | 'TECNICO' | 'ADMIN';
export type StatusChamado = 'CONCLUIDO' | 'EM_ABERTO' | 'EM_ANDAMENTO';

export interface Chamado {
  id: string;
  solicitante: string;
  tipo: string;
  descricao: string;
  dataHora: string;      // ISO string
  tecnico?: string | null;
  status: StatusChamado;
}

export interface TecnicoItem { id: string; nome: string; }

@Component({
  selector: 'app-chamados-table',
  standalone: true,
  imports: [CommonModule, DatePipe, NgClass],
  templateUrl: './chamados-table.component.html',
  styleUrls: ['./chamados-table.component.css']
})
export class ChamadosTableComponent implements OnInit {
  @Input() chamados: Chamado[] = [];
  @Input() tecnicos: TecnicoItem[] = [];

  // 🔹 papel do usuário atual
  @Input() role: Role = 'USUARIO_COMUM';

  // Eventos para o container
  @Output() alterarStatus = new EventEmitter<{ id: string; novo: StatusChamado }>();
  @Output() excluir = new EventEmitter<string>();
  @Output() atribuirPraMim = new EventEmitter<string>();
  @Output() comentar = new EventEmitter<{ id: string; mensagem: string }>();

  @Output() designar = new EventEmitter<{ id: string; tecnicoId: string }>();
  @Output() cancelar = new EventEmitter<string>();

  onDesignar(c: Chamado, tecnicoId: string) { this.designar.emit({ id: c.id, tecnicoId }); }
  onCancelar(c: Chamado) { this.cancelar.emit(c.id); }

  ngOnInit(): void {
    if (!this.chamados?.length) {
      // mocks só para visual
      this.chamados = [
        {
          id: '0001', solicitante: 'André Costa', tipo: 'Instalação de Rede',
          descricao: 'Rede lenta no setor A.', dataHora: new Date().toISOString(),
          tecnico: 'Carlos Silva', status: 'EM_ANDAMENTO'
        },
        {
          id: '0002', solicitante: 'Júlia Maria', tipo: 'Recuperação de Dados',
          descricao: 'HD externo não reconhece.', dataHora: new Date().toISOString(),
          tecnico: null, status: 'EM_ABERTO'
        },
        {
          id: '0003', solicitante: 'Carlos Silva', tipo: 'Suporte de Software',
          descricao: 'Erro ao abrir sistema XPTO.', dataHora: new Date().toISOString(),
          tecnico: 'Ana Oliveira', status: 'CONCLUIDO'
        },
      ];
    }
  }

  // badges (sem mudança)
  badgeClasses(st: StatusChamado) {
    switch (st) {
      case 'CONCLUIDO': return 'text-bg-success';
      case 'EM_ABERTO': return 'text-bg-danger';
      case 'EM_ANDAMENTO': return 'text-bg-warning';
    }
  }
  badgeLabel(st: StatusChamado) {
    switch (st) {
      case 'CONCLUIDO': return 'Concluído';
      case 'EM_ABERTO': return 'Em aberto';
      case 'EM_ANDAMENTO': return 'Em andamento';
    }
  }

  // 🔎 regras de visibilidade por papel
  isCliente() { return this.role === 'USUARIO_COMUM'; }
  isTecnico() { return this.role === 'TECNICO'; }
  isAdmin() { return this.role === 'ADMIN'; }

  // botões por linha
  canExcluir(c: Chamado) { return this.isCliente(); }
  canAtribuirPraMim(c: Chamado) { return this.isTecnico() && c.status === 'EM_ABERTO' && !c.tecnico; }
  canMarcarAndamento(c: Chamado) { return this.isTecnico() && c.status !== 'EM_ANDAMENTO'; }
  canMarcarConcluido(c: Chamado) { return this.isTecnico() && c.status !== 'CONCLUIDO'; }
  canComentar(c: Chamado) { return this.isTecnico(); } // se quiser, inclua ADMIN

  // emissores
  onExcluir(c: Chamado) { this.excluir.emit(c.id); }
  onAtribuirPraMim(c: Chamado) { this.atribuirPraMim.emit(c.id); }
  onSetStatus(c: Chamado, novo: StatusChamado) { this.alterarStatus.emit({ id: c.id, novo }); }
  onComentar(c: Chamado) {
    const mensagem = prompt('Comentário do técnico:');
    if (mensagem && mensagem.trim()) this.comentar.emit({ id: c.id, mensagem: mensagem.trim() });
  }
}

import { CommonModule, DatePipe, NgClass } from '@angular/common';
import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';

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

@Component({
  selector: 'app-chamados-table',
  standalone: true,
  imports: [CommonModule, DatePipe, NgClass],
  templateUrl: './chamados-table.component.html',
  styleUrls: ['./chamados-table.component.css']
})
export class ChamadosTableComponent implements OnInit {
  /** dados (pode vir da API ou do pai) */
  @Input() chamados: Chamado[] = [];

  /** lista de técnicos para atribuição */
  @Input() tecnicos: string[] = ['Ana Oliveira', 'Carlos Silva', 'Marcos Lima'];

  /** eventos de ação */
  @Output() alterarStatus = new EventEmitter<{ id: string; novo: StatusChamado }>();
  @Output() excluir = new EventEmitter<string>();
  @Output() atribuirTecnico = new EventEmitter<{ id: string; tecnico: string }>();

  ngOnInit(): void {
    if (!this.chamados || this.chamados.length === 0) {
      // cria mocks caso não seja passado nenhum chamado
      this.chamados = [
        {
          id: '0001',
          solicitante: 'André Costa',
          tipo: 'Instalação de Rede',
          descricao: 'Rede lenta no setor A.',
          dataHora: new Date().toISOString(),
          tecnico: 'Carlos Silva',
          status: 'EM_ANDAMENTO'
        },
        {
          id: '0002',
          solicitante: 'Júlia Maria',
          tipo: 'Recuperação de Dados',
          descricao: 'HD externo não reconhece.',
          dataHora: new Date().toISOString(),
          tecnico: null,
          status: 'EM_ABERTO'
        },
        {
          id: '0003',
          solicitante: 'Carlos Silva',
          tipo: 'Suporte de Software',
          descricao: 'Erro ao abrir sistema XPTO.',
          dataHora: new Date().toISOString(),
          tecnico: 'Ana Oliveira',
          status: 'CONCLUIDO'
        }
      ];
    }
  }

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

  proximoStatus(atual: StatusChamado): StatusChamado {
    if (atual === 'EM_ABERTO') return 'EM_ANDAMENTO';
    if (atual === 'EM_ANDAMENTO') return 'CONCLUIDO';
    return 'EM_ABERTO';
  }

  onMudarStatus(c: Chamado) {
    const novo = this.proximoStatus(c.status);
    this.alterarStatus.emit({ id: c.id, novo });
  }

  onExcluir(c: Chamado) { this.excluir.emit(c.id); }

  onAtribuir(c: Chamado, tecnico: string) {
    if (!tecnico) return;
    this.atribuirTecnico.emit({ id: c.id, tecnico });
  }
}

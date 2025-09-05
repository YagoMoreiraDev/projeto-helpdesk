import { CommonModule, DatePipe, NgClass } from '@angular/common';
import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';

export type StatusChamado = 'CONCLUIDO' | 'EM_ABERTO' | 'EM_ANDAMENTO';

export interface Chamado {
  id: string;
  solicitante: string;
  tipo: string;
  descricao: string;
  dataHora: string; // ISO
  tecnico?: string | null;
  status: StatusChamado;
}

function dedup<T extends { id: string }>(arr: T[]): T[] {
  const seen = new Set<string>();
  return arr.filter(x => (seen.has(x.id) ? false : (seen.add(x.id), true)));
}
function upsertById<T extends { id: string }>(arr: T[], item: T): T[] {
  const i = arr.findIndex(a => a.id === item.id);
  if (i === -1) return [item, ...arr];
  const clone = arr.slice(); clone[i] = { ...clone[i], ...item };
  return clone;
}

@Component({
  selector: 'app-chamados-tecnicos',
  standalone: true,
  imports: [CommonModule, DatePipe, NgClass],
  templateUrl: './chamados-tecnicos.component.html',
  styleUrls: ['./chamados-tecnicos.component.css']
})
export class ChamadosTecnicosComponent implements OnInit {
  /** Nome do técnico logado (usado nos filtros e nas ações) */
  @Input() tecnicoAtual = 'Carlos Silva';

  /** Fonte de dados (se não vier, mocka no ngOnInit) */
  @Input() chamados: Chamado[] = [];

  /** Eventos para integrar com API */
  @Output() iniciar = new EventEmitter<{ id: string; tecnico: string }>();
  @Output() encerrar = new EventEmitter<{ id: string; tecnico: string }>();
  @Output() repassar = new EventEmitter<{ id: string }>();

  ngOnInit(): void {
    if (!this.chamados.length) {
      const now = new Date();
      this.chamados = [
        { id:'0001', solicitante:'André Costa',  tipo:'Instalação de Rede', descricao:'Rede lenta no setor A.',
          dataHora: now.toISOString(), tecnico: null, status:'EM_ABERTO' },
        { id:'0002', solicitante:'Júlia Maria',  tipo:'Recuperação de Dados', descricao:'HD externo não reconhece.',
          dataHora: now.toISOString(), tecnico: null, status:'EM_ABERTO' },
        { id:'0003', solicitante:'Carlos Silva', tipo:'Suporte de Software', descricao:'Erro ao abrir XPTO.',
          dataHora: now.toISOString(), tecnico: 'Carlos Silva', status:'EM_ANDAMENTO' },
        { id:'0004', solicitante:'Aline Souza',  tipo:'Hardware', descricao:'Troca de memória.',
          dataHora: new Date(now.getTime()-86400000).toISOString(), tecnico: 'Carlos Silva', status:'CONCLUIDO' },
        { id:'0005', solicitante:'Marcelo Andrade', tipo:'Software', descricao:'Instalar Office.',
          dataHora: now.toISOString(), tecnico: 'Ana Oliveira', status:'EM_ANDAMENTO' },
      ];
    }
  }

  // ----- filtros mapeados ao layout -----
  get emAberto(): Chamado[] {
    // todos em aberto (independente de técnico) para o técnico poder “pegar”
    return this.chamados.filter(c => c.status === 'EM_ABERTO');
  }
  get emAndamentoDoTecnico(): Chamado[] {
    return this.chamados.filter(c => c.status === 'EM_ANDAMENTO' && c.tecnico === this.tecnicoAtual);
  }
  get concluidosDoTecnico(): Chamado[] {
    return this.chamados.filter(c => c.status === 'CONCLUIDO' && c.tecnico === this.tecnicoAtual);
  }

  // ----- ações de UI (emitem eventos para o container integrar com API) -----
  onIniciar(c: Chamado) {
    this.iniciar.emit({ id: c.id, tecnico: this.tecnicoAtual });
    // UI otimista:
    c.status = 'EM_ANDAMENTO';
    c.tecnico = this.tecnicoAtual;
  }
  onEncerrar(c: Chamado) {
    this.encerrar.emit({ id: c.id, tecnico: this.tecnicoAtual });
    c.status = 'CONCLUIDO';
  }
  onRepassar(c: Chamado) {
    this.repassar.emit({ id: c.id });
    c.status = 'EM_ABERTO';
    c.tecnico = null;
  }

  // util para badge
  badge(st: StatusChamado) {
    return st === 'EM_ABERTO' ? 'text-bg-danger'
         : st === 'EM_ANDAMENTO' ? 'text-bg-warning'
         : 'text-bg-success';
  }
}

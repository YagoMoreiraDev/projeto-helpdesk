import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

// seus componentes standalone
import { HeaderMenuComponent } from "../../components/header-menu/header-menu.component";
import { CriarChamadoComponent } from '../../components/criar-chamado/criar-chamado.component';
import { Chamado as UiChamado, StatusChamado as UiStatus } from '../../pages/chamados-table/chamados-table.component';

// models do backend
import { ChamadoResponse } from '../../core/models/chamado.model';
import { StatusChamado as BackStatus } from '../../core/models/status-chamado.enum';
import { UUID } from '../../core/uuid.type';

@Component({
  selector: 'app-layout',
  standalone: true, // ⬅️ necessário para usar "imports" no decorator
  imports: [HeaderMenuComponent, RouterOutlet, CriarChamadoComponent],
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.css'] // ⬅️ plural
})
export class LayoutComponent {

  // TODO: substituir pelo ID do usuário autenticado (ex.: vindo de um AuthService)
  usuarioLogadoId: UUID = '00000000-0000-0000-0000-000000000000';

  // sua lista para a tabela (mock inicial)
  listaChamados: UiChamado[] = [
    { id:'00001', solicitante:'André Costa', tipo:'Instalação de Rede',
      descricao:'Rede lenta no setor A.', dataHora: new Date().toISOString(),
      tecnico:'Carlos Silva', status:'EM_ANDAMENTO' as UiStatus
    },
    { id:'00002', solicitante:'Júlia Maria', tipo:'Recuperação de Dados',
      descricao:'HD externo não reconhece.', dataHora: new Date().toISOString(),
      tecnico:null, status:'EM_ABERTO' as UiStatus
    },
    { id:'00003', solicitante:'Carlos Silva', tipo:'Suporte de Software',
      descricao:'Erro ao abrir sistema XPTO.', dataHora: new Date().toISOString(),
      tecnico:'Ana Oliveira', status:'CONCLUIDO' as UiStatus
    },
  ];

  // (a) se você ainda quiser capturar o payload bruto do form
  abrirChamado(payload: { servicoId: string; prioridade?: string; descricao: string }) {
    console.log('Novo chamado (payload bruto do form):', payload);
  }

  // (b) handler para o evento do componente de criação, com o retorno do backend
  onChamadoCriado(resp: ChamadoResponse) {
    // converte o DTO do backend para o modelo que sua tabela usa
    const novo: UiChamado = this.mapToUiChamado(resp);
    // insere no topo da lista (update otimista)
    this.listaChamados = [novo, ...this.listaChamados];
  }

  // ----- auxiliares -----
  private mapToUiChamado(c: ChamadoResponse): UiChamado {
    return {
      id: c.id,
      solicitante: c.solicitanteNome ?? '—',
      tipo: this.extrairTipoDoTitulo(c.titulo), // seu título hoje é "Atendimento: X"
      descricao: c.descricao,
      dataHora: c.createdAt,
      tecnico: c.tecnicoNome ?? null,
      status: this.mapStatus(c.status),
    };
  }

  private extrairTipoDoTitulo(titulo: string): string {
    const prefixo = 'Atendimento: ';
    return titulo?.startsWith(prefixo) ? titulo.slice(prefixo.length) : titulo ?? '—';
    // quando você levar "servicoId" para o backend, pode eliminar esse parser
  }

  private mapStatus(s: BackStatus): UiStatus {
    // sua tabela usa rótulos diferentes dos do backend; mapeamos aqui
    switch (s) {
      case BackStatus.ABERTO:          return 'EM_ABERTO'    as UiStatus;
      case BackStatus.EM_ATENDIMENTO:  return 'EM_ANDAMENTO' as UiStatus;
      case BackStatus.CONCLUIDO:       return 'CONCLUIDO'    as UiStatus;
      case BackStatus.CANCELADO:       return 'CANCELADO'    as UiStatus;
    }
  }

  // handlers já existentes (mantidos)
  handleAlterarStatus(e:{id:string; novo:UiStatus}) {
    this.listaChamados = this.listaChamados.map(c =>
      c.id === e.id ? { ...c, status: e.novo } : c);
  }

  handleExcluir(id:string) {
    this.listaChamados = this.listaChamados.filter(c => c.id !== id);
  }

  handleAtribuir(e:{id:string; tecnico:string}) {
    this.listaChamados = this.listaChamados.map(c =>
      c.id === e.id ? { ...c, tecnico: e.tecnico } : c);
  }
}

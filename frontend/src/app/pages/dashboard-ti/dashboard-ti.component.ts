import { CommonModule, DatePipe, NgClass } from '@angular/common';
import { Component, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

type Status = 'CONCLUIDO' | 'EM_ANDAMENTO' | 'EM_ABERTO';

interface Atendimento {
  id: string;
  solicitante: string;
  tecnico: string;
  tipo: string;
  descricao: string;
  data: string;   // ISO string (ex.: new Date().toISOString())
  status: Status;
}

type Granularidade = 'DIA' | 'SEMANA' | 'MES';

@Component({
  selector: 'app-dashboard-ti',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe, NgClass],
  templateUrl: './dashboard-ti.component.html',
  styleUrls: ['./dashboard-ti.component.css']
})
export class DashboardTiComponent {

  // ------- MOCKS (troque por API) -------
  atendimentos = signal<Atendimento[]>([
    { id:'1', solicitante:'André',  tecnico:'Carlos Silva',  tipo:'Rede',      descricao:'Lenta no setor A', data: addDaysISO(0),  status:'EM_ANDAMENTO' },
    { id:'2', solicitante:'Júlia',  tecnico:'—',             tipo:'Dados',     descricao:'HD não reconhece', data: addDaysISO(0),  status:'EM_ABERTO' },
    { id:'3', solicitante:'Carlos', tecnico:'Ana Oliveira',   tipo:'Software',  descricao:'Erro XPTO',        data: addDaysISO(0),  status:'CONCLUIDO' },
    { id:'4', solicitante:'Aline',  tecnico:'Ana Oliveira',   tipo:'Hardware',  descricao:'Troca memória',    data: addDaysISO(-1), status:'CONCLUIDO' },
    { id:'5', solicitante:'Marcelo',tecnico:'Carlos Silva',   tipo:'Rede',      descricao:'Sem internet',     data: addDaysISO(-1), status:'EM_ANDAMENTO' },
    { id:'6', solicitante:'Suzane', tecnico:'Marcos Lima',    tipo:'Software',  descricao:'Instalação app',   data: addDaysISO(-2), status:'EM_ABERTO' },
    { id:'7', solicitante:'André',  tecnico:'Carlos Silva',   tipo:'Rede',      descricao:'Queda intermitente',data:addDaysISO(-3), status:'CONCLUIDO' },
    { id:'8', solicitante:'Júlia',  tecnico:'Marcos Lima',    tipo:'Dados',     descricao:'Backup falhou',    data: addDaysISO(-4), status:'CONCLUIDO' },
    { id:'9', solicitante:'Carlos', tecnico:'Ana Oliveira',   tipo:'Software',  descricao:'Licença',          data: addDaysISO(-5), status:'EM_ABERTO' },
    { id:'10',solicitante:'Aline',  tecnico:'Marcos Lima',    tipo:'Hardware',  descricao:'Fonte queimada',   data: addDaysISO(-6), status:'CONCLUIDO' },
  ]);

  // ------- Filtros / controles -------
  hoje = new Date();
  dataBase: string = '';
  filtroData() {
    return this.dataBase;
  }      // para o card "por período"
  
  granularidade = signal<Granularidade>('DIA');

  setFiltroData(value: string) {
    this.dataBase = value;
  }// ...qualquer lógica adicional...


  tecnicos = computed(() => {
    const set = new Set(this.atendimentos().map(a => a.tecnico).filter(Boolean));
    return Array.from(set);
  });
  tecnicoSelecionado = signal<string | null>(null);

  // ------- KPIs globais -------
  total = computed(() => this.atendimentos().length);
  concluidos = computed(() => this.atendimentos().filter(a => a.status === 'CONCLUIDO').length);
  andamento   = computed(() => this.atendimentos().filter(a => a.status === 'EM_ANDAMENTO').length);
  abertos     = computed(() => this.atendimentos().filter(a => a.status === 'EM_ABERTO').length);

  // ------- Total por dia (últimos 7 dias) -------
  ultimos7 = computed(() => {
    const days: {label: string; date: Date; total: number}[] = [];
    for (let i = 6; i >= 0; i--) {
      const d = addDays(this.hoje, -i);
      const key = d.toDateString();
      const total = this.atendimentos().filter(a => new Date(a.data).toDateString() === key).length;
      days.push({ label: d.toLocaleDateString('pt-BR', { day:'2-digit', month:'2-digit' }), date: d, total });
    }
    return days;
  });
  maxUltimos7 = computed(() => Math.max(1, ...this.ultimos7().map(d => d.total)));

  // ------- Concluídos por período -------
  concluidosPeriodo = computed(() => {
    const base = new Date(this.filtroData());
    const [ini, fim] = rangeFrom(base, this.granularidade());
    return this.atendimentos().filter(a => {
      const d = new Date(a.data);
      return a.status === 'CONCLUIDO' && d >= ini && d <= fim;
    }).length;
  });

  // ------- Resumo por técnico (no dia) -------
  resumoTecnicoHoje = computed(() => {
    const nome = this.tecnicoSelecionado();
    if (!nome) return { concluidos: 0, abertos: 0, andamento: 0 };
    const key = this.hoje.toDateString();
    const doDia = this.atendimentos().filter(a => new Date(a.data).toDateString() === key && a.tecnico === nome);
    return {
      concluidos: doDia.filter(a => a.status === 'CONCLUIDO').length,
      abertos:    doDia.filter(a => a.status === 'EM_ABERTO').length,
      andamento:  doDia.filter(a => a.status === 'EM_ANDAMENTO').length
    };
  });

  // handlers
  setGran(g: Granularidade) { this.granularidade.set(g); }
  setTecnico(nome: string)  { this.tecnicoSelecionado.set(nome || null); }
}

/* ---------- helpers ---------- */
function addDays(base: Date, n: number) {
  const d = new Date(base); d.setDate(d.getDate() + n); return d;
}
function addDaysISO(n: number) {
  return addDays(new Date(), n).toISOString();
}
function toDateInput(d: Date) {
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}
function rangeFrom(base: Date, g: Granularidade): [Date, Date] {
  const ini = new Date(base), fim = new Date(base);
  if (g === 'DIA') {
    ini.setHours(0,0,0,0); fim.setHours(23,59,59,999);
  } else if (g === 'SEMANA') {
    const day = (base.getDay() + 6) % 7; // segunda como início
    ini.setDate(base.getDate() - day); ini.setHours(0,0,0,0);
    fim.setDate(ini.getDate() + 6);     fim.setHours(23,59,59,999);
  } else { // MES
    ini.setDate(1); ini.setHours(0,0,0,0);
    fim.setMonth(base.getMonth() + 1, 0); fim.setHours(23,59,59,999);
  }
  return [ini, fim];
}

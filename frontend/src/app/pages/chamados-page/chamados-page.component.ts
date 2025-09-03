// pages/chamados-page/chamados-page.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chamado, ChamadosTableComponent, StatusChamado } from '../chamados-table/chamados-table.component';


@Component({
  standalone: true,
  selector: 'app-chamados-page',
  imports: [CommonModule, ChamadosTableComponent],
  templateUrl: './chamados-page.component.html',
})
export class ChamadosPageComponent {
  listaChamados: Chamado[] = [/* ... mocks ... */];

  handleAlterarStatus(e:{id:string; novo:StatusChamado}) { /* ... */ }
  handleExcluir(id:string) { /* ... */ }
  handleAtribuir(e:{id:string; tecnico:string}) { /* ... */ }
}






// pages/tecnico/chamados-tecnicos.page.ts (opcional)
import { Component } from '@angular/core';
import { ChamadosTecnicosComponent } from '../chamados-tecnicos/chamados-tecnicos.component';

@Component({
  standalone: true,
  imports: [ChamadosTecnicosComponent],
  template: `
    <section class="container py-4">
      <app-chamados-tecnicos
        [tecnicoAtual]="'Carlos Silva'"
        (iniciar)="onIniciar($event)"
        (encerrar)="onEncerrar($event)"
        (repassar)="onRepassar($event)">
      </app-chamados-tecnicos>
    </section>
  `
})
export class ChamadosTecnicosPage {
  onIniciar(e:{id:string; tecnico:string}) { console.log('iniciar', e); /* chama API */ }
  onEncerrar(e:{id:string; tecnico:string}) { console.log('encerrar', e); /* chama API */ }
  onRepassar(e:{id:string}) { console.log('repassar', e); /* chama API */ }
}

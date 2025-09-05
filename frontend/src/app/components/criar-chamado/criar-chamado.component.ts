import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { finalize } from 'rxjs/operators';

// imports do seu core
import { ChamadoService } from '../../services/chamado.service';
import { ChamadoCreateRequest, ChamadoResponse } from '../../core/models/chamado.model';
import { Prioridade } from '../../core/models/prioridade.enum';
import { UUID } from '../../core/uuid.type';

type Servico = { id: string; nome: string };

@Component({
  selector: 'app-criar-chamado',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './criar-chamado.component.html',
  styleUrls: ['./criar-chamado.component.css'],
})
export class CriarChamadoComponent {
  @Input() modalId = 'criarChamadoModal';

  // <- IMPORTANTÍSSIMO: quem usa o componente injeta o ID do usuário logado
  @Input({ required: true }) solicitanteId!: UUID;

  @Input() servicos: Servico[] = [
    { id: 'rede', nome: 'Instalação de Rede' },
    { id: 'dados', nome: 'Recuperação de Dados' },
    { id: 'hardware', nome: 'Manutenção de Hardware' },
    { id: 'software', nome: 'Suporte de Software' },
  ];

  prioridades = [
    { id: 'baixa', nome: 'Baixa' },
    { id: 'media', nome: 'Média' },
    { id: 'alta', nome: 'Alta' },
  ];

  /** Mantém sua saída original para o pai (se já usa em outro lugar) */
  @Output() submitted = new EventEmitter<{ servicoId: string; prioridade?: string; descricao: string }>();
  /** Novo: emite a resposta do backend quando o chamado é criado com sucesso */
  @Output() created = new EventEmitter<ChamadoResponse>();

  form: FormGroup;
  loading = false;
  errorMsg = '';

  constructor(private fb: FormBuilder, private chamadoService: ChamadoService) {
    this.form = this.fb.group({
      servicoId: ['', Validators.required],
      prioridade: ['media'],
      descricao: ['', [Validators.required, Validators.minLength(10)]],
    });
  }

  get f() {
    return this.form.controls;
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    // 1) Emite o payload “cru” para o pai (mantém compatibilidade)
    this.submitted.emit(this.form.value as any);

    // 2) Prepara o request do backend
    const { servicoId, prioridade, descricao } = this.form.value as {
      servicoId: string;
      prioridade: 'baixa' | 'media' | 'alta';
      descricao: string;
    };

    const servico = this.servicos.find((s) => s.id === servicoId);
    const req: ChamadoCreateRequest = {
      // título usa o nome do serviço para dar contexto no backend
      titulo: servico ? `Atendimento: ${servico.nome}` : 'Solicitação de atendimento',
      descricao,
      prioridade: this.mapPrioridade(prioridade),
    };

    this.loading = true;
    this.errorMsg = '';

    this.chamadoService
      this.chamadoService.abrirChamado(req)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (resp) => {
          // emite para quem precisa saber do chamado criado
          this.created.emit(resp);

          // limpa o formulário
          this.form.reset({ servicoId: '', prioridade: 'media', descricao: '' });

          // se estiver usando modal do Bootstrap, você pode fechá-lo via data-bs-dismiss no botão
          // ou programaticamente (opcional)
          // this.closeModalProgramaticamente();
        },
        error: (err) => {
          this.errorMsg =
            err?.error?.message ||
            err?.message ||
            'Não foi possível abrir o chamado. Tente novamente em instantes.';
        },
      });
  }

  private mapPrioridade(p: 'baixa' | 'media' | 'alta'): Prioridade {
    switch (p) {
      case 'baixa':
        return Prioridade.BAIXA;
      case 'alta':
        return Prioridade.ALTA;
      default:
        return Prioridade.MEDIA;
    }
  }

  // Exemplo opcional: fechar modal via Bootstrap (se estiver usando JS do Bootstrap)
  /*
  private closeModalProgramaticamente() {
    const modalEl = document.getElementById(this.modalId);
    if (!modalEl) return;
    // @ts-ignore
    const modalInstance = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
    modalInstance.hide();
  }
  */
}

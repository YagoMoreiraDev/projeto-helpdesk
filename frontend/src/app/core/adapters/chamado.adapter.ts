// src/app/core/adapters/chamado.adapter.ts
import { ChamadoResponse } from '../models/chamado.model';
import { StatusChamado as ApiStatus } from '../models/status-chamado.enum';
import { Chamado as UiChamado, StatusChamado as UiStatus } from '../../pages/chamados-table/chamados-table.component';

// API -> UI
export function toUiChamado(r: ChamadoResponse): UiChamado {
    return {
        id: String(r.id),
        solicitante: r.solicitanteNome ?? '—',
        tipo: r.titulo ?? 'Chamado',
        descricao: r.descricao ?? '',
        dataHora: r.createdAt ?? new Date().toISOString(),
        tecnico: r.tecnicoNome ?? null,
        status: fromApiStatus(r.status),
    };
}

// API(enum/string) -> UI('EM_ABERTO' | 'EM_ANDAMENTO' | 'CONCLUIDO')
export function fromApiStatus(api: ApiStatus | string): UiStatus {
    const v = typeof api === 'string' ? api : String(api);
    switch (v) {
        case ApiStatus.EM_ATENDIMENTO:
        case 'EM_ATENDIMENTO':
            return 'EM_ANDAMENTO';
        case ApiStatus.CONCLUIDO:
        case 'CONCLUIDO':
            return 'CONCLUIDO';
        // ABERTO e CANCELADO mapeiam para o rótulo 'EM_ABERTO' na UI
        // (se quiser exibir CANCELADO separadamente, amplie o tipo da UI)
        case ApiStatus.ABERTO:
        case 'ABERTO':
        case ApiStatus.CANCELADO:
        case 'CANCELADO':
        default:
            return 'EM_ABERTO';
    }
}

// UI -> API
export function toApiStatus(ui: UiStatus): ApiStatus {
    switch (ui) {
        case 'EM_ANDAMENTO':
            return ApiStatus.EM_ATENDIMENTO;
        case 'CONCLUIDO':
            return ApiStatus.CONCLUIDO;
        default:
            return ApiStatus.ABERTO;
    }
}

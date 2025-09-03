
import { UUID } from '../uuid.type';
import { Prioridade } from './prioridade.enum';
import { StatusChamado } from './status-chamado.enum';
import { TipoEvento } from './tipo-evento.enum';

export interface ChamadoEvento {
    id: UUID;
    quando: string; // ISO string (Instant)
    tipo: TipoEvento;
    autorNome?: string | null;
    detalhe?: string | null;
    de?: StatusChamado | null; // statusAnterior
    para?: StatusChamado | null; // statusNovo
}

export interface Chamado {
    id: UUID;
    titulo: string;
    descricao: string;
    status: StatusChamado;
    prioridade: Prioridade;
    solicitanteId?: UUID | null;
    solicitanteNome?: string | null;
    tecnicoId?: UUID | null;
    tecnicoNome?: string | null;
    createdAt: string; // ISO
    closedAt?: string | null; // ISO
    eventos: ChamadoEvento[];
}

export interface ChamadoCreateRequest {
    titulo: string;
    descricao: string;
    prioridade: Prioridade;
}

export interface ComentarioRequest {
    mensagem: string;
}

export interface StatusChangeRequest {
    novoStatus: StatusChamado;
    detalhe: string;
}

export type ChamadoResponse = Chamado;
import { UUID } from '../uuid.type';
import { Prioridade } from './prioridade.enum';
import { StatusChamado } from './status-chamado.enum';
import { TipoEvento } from './tipo-evento.enum';

export interface ChamadoEvento {
    id: UUID;
    quando: string;
    tipo: TipoEvento;
    autorNome?: string | null;
    detalhe?: string | null;
    de?: StatusChamado | null;
    para?: StatusChamado | null;
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
    createdAt: string;
    closedAt?: string | null;
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
    novoStatus: StatusChamado; // enum do core
    detalhe: string;           // é obrigatório no seu DTO
}

export type ChamadoResponse = Chamado;

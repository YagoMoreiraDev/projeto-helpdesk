// src/app/services/chamado.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
    Chamado as ChamadoApi,
    ChamadoCreateRequest,
    ChamadoResponse,
    ComentarioRequest,
    StatusChangeRequest,
} from '../core/models/chamado.model';
import { UUID } from '../core/uuid.type';
import { BaseHttpService } from './base-http.service';
import { StatusChamado } from '../core/models/status-chamado.enum';

@Injectable({ providedIn: 'root' })
export class ChamadoService extends BaseHttpService {
    private readonly http = inject(HttpClient);
    private readonly API = environment.apiUrl ?? 'http://localhost:8080';
    private readonly PATH = `${this.API}/api/chamados`;

    abrirChamado(req: ChamadoCreateRequest): Observable<ChamadoResponse> {
        return this.http.post<ChamadoResponse>(this.PATH, req);
    }

    assumirChamado(chamadoId: UUID): Observable<ChamadoResponse> {
        return this.http.post<ChamadoResponse>(`${this.PATH}/${chamadoId}/assumir`, null);
    }

    comentarChamado(chamadoId: UUID, req: ComentarioRequest): Observable<ChamadoResponse> {
        return this.http.post<ChamadoResponse>(`${this.PATH}/${chamadoId}/comentarios`, req);
    }

    alterarStatus(chamadoId: UUID, req: StatusChangeRequest): Observable<ChamadoResponse> {
        return this.http.post<ChamadoResponse>(`${this.PATH}/${chamadoId}/status`, req);
    }

    // VISÕES por papel
    listarMeusChamados(): Observable<ChamadoApi[]> {
        return this.http.get<ChamadoApi[]>(`${this.PATH}/meus`);
    }
    listarPorTecnico(): Observable<ChamadoApi[]> {
        return this.http.get<ChamadoApi[]>(`${this.PATH}/tecnico`);
    }
    listarEmAberto(): Observable<ChamadoApi[]> {
        return this.http.get<ChamadoApi[]>(`${this.PATH}/abertos`);
    }
    listarTodos(): Observable<ChamadoApi[]> {
        return this.http.get<ChamadoApi[]>(this.PATH);
    }

    listarSemTecnico(): Observable<ChamadoApi[]> {
        return this.http.get<ChamadoApi[]>(`${this.PATH}/sem-tecnico`);
    }

    listarPorStatus(status: StatusChamado) {
        return this.http.get<ChamadoApi[]>(
            this.PATH,
            { params: { status } } // envia ?status=ABERTO|EM_ATENDIMENTO|...
        );
    }

    designar(chamadoId: UUID, tecnicoId: UUID): Observable<ChamadoResponse> {
        return this.http.post<ChamadoResponse>(`${this.PATH}/${chamadoId}/designar`, null, {
            params: this.toParams({ tecnicoId })
        });
    }

    // cancelar (cliente ou admin)
    cancelarChamado(chamadoId: UUID, detalhe?: string): Observable<ChamadoResponse> {
        return this.http.post<ChamadoResponse>(`${this.PATH}/${chamadoId}/cancelar`, { detalhe });
    }

    // (se ainda tiver excluir físico no front, deixe como alias para cancelar)
    excluirChamado(chamadoId: UUID): Observable<ChamadoResponse> {
        return this.cancelarChamado(chamadoId, 'Cancelado pelo solicitante');
    }
}

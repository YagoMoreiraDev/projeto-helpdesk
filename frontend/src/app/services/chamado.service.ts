import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
    Chamado,
    ChamadoCreateRequest,
    ChamadoResponse,
    ComentarioRequest,
    StatusChangeRequest,
} from '../core/models/chamado.model';
import { UUID } from '../core/uuid.type';
import { BaseHttpService } from './base-http.service';


@Injectable({ providedIn: 'root' })
export class ChamadoService extends BaseHttpService {
    private readonly http = inject(HttpClient);
    private readonly API = environment.apiUrl ?? 'http://localhost:8080';
    private readonly PATH = `${this.API}/api/chamados`;


    abrirChamado(solicitanteId: UUID, req: ChamadoCreateRequest): Observable<ChamadoResponse> {
        const params = this.toParams({ solicitanteId });
        return this.http.post<ChamadoResponse>(this.PATH, req, { params });
    }


    assumirChamado(chamadoId: UUID, tecnicoId: UUID): Observable<ChamadoResponse> {
        const params = this.toParams({ tecnicoId });
        return this.http.post<ChamadoResponse>(`${this.PATH}/${chamadoId}/assumir`, null, { params });
    }


    comentarChamado(chamadoId: UUID, autorId: UUID, req: ComentarioRequest): Observable<ChamadoResponse> {
        const params = this.toParams({ autorId });
        return this.http.post<ChamadoResponse>(`${this.PATH}/${chamadoId}/comentarios`, req, { params });
    }


    alterarStatus(chamadoId: UUID, autorId: UUID, req: StatusChangeRequest): Observable<ChamadoResponse> {
        const params = this.toParams({ autorId });
        return this.http.post<ChamadoResponse>(`${this.PATH}/${chamadoId}/status`, req, { params });
    }


    listarMeusChamados(solicitanteId: UUID): Observable<Chamado[]> {
        const params = this.toParams({ solicitanteId });
        return this.http.get<Chamado[]>(`${this.PATH}/meus`, { params });
    }


    listarPorTecnico(tecnicoId: UUID): Observable<Chamado[]> {
        const params = this.toParams({ tecnicoId });
        return this.http.get<Chamado[]>(`${this.PATH}/tecnico`, { params });
    }


    listarTodos(): Observable<Chamado[]> {
        return this.http.get<Chamado[]>(this.PATH);
    }
}
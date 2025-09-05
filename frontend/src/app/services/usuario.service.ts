// src/app/services/usuario.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
    UsuarioCreateRequest,
    UsuarioResponse,
    UsuarioUpdateRequest
} from '../core/models/usuario.model';
import { UUID } from '../core/uuid.type';
import { BaseHttpService } from './base-http.service';

export interface TecnicoLookup {
    id: string;
    nome: string;
    email: string;
}

@Injectable({ providedIn: 'root' })
export class UsuarioService extends BaseHttpService {
    private readonly http = inject(HttpClient);
    private readonly API = environment.apiUrl ?? 'http://localhost:8080';
    private readonly PATH = `${this.API}/api/usuarios`;   // 👈 faltava isto

    // --- consultas auxiliares ---
    tecnicos(): Observable<TecnicoLookup[]> {
        return this.http.get<TecnicoLookup[]>(`${this.PATH}/tecnicos`);
    }

    // --- CRUD básico ---
    criar(req: UsuarioCreateRequest): Observable<UsuarioResponse> {
        return this.http.post<UsuarioResponse>(this.PATH, req);
    }

    atualizar(id: UUID, req: UsuarioUpdateRequest): Observable<UsuarioResponse> {
        return this.http.put<UsuarioResponse>(`${this.PATH}/${id}`, req);
    }

    deletarLogico(id: UUID): Observable<void> {
        return this.http.delete<void>(`${this.PATH}/${id}`);
    }

    buscar(id: UUID): Observable<UsuarioResponse> {
        return this.http.get<UsuarioResponse>(`${this.PATH}/${id}`);
    }

    listarAtivos(): Observable<UsuarioResponse[]> {
        return this.http.get<UsuarioResponse[]>(this.PATH);
    }
}

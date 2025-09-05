import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { finalize, tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { AuthResponse, UserPayload } from '../core/models/auth.model';

@Injectable({ providedIn: 'root' })
export class AutenticacaoService {
    private readonly http = inject(HttpClient);
    private readonly API = environment.apiUrl ?? 'http://localhost:8080';

    // estado em memória (mais seguro)
    private accessTokenSub = new BehaviorSubject<string | null>(null);
    private userSub = new BehaviorSubject<UserPayload | null>(null);
    private expiryEpochMs = 0;

    /** Login por e-mail + senha. Salva sessão em memória e devolve o AuthResponse. */
    autenticar(email: string, senha: string): Observable<AuthResponse> {
        return this.http
            .post<AuthResponse>(`${this.API}/auth/login`, { email, senha }, { withCredentials: true })
            .pipe(tap((resp) => this.setSession(resp)));
    }

    /** Atualiza o token de acesso usando o refresh cookie (HttpOnly). */
    refresh(): Observable<AuthResponse> {
        return this.http
            .post<AuthResponse>(`${this.API}/auth/refresh`, {}, { withCredentials: true })
            .pipe(tap((resp) => this.setSession(resp)));
    }

    /** Logout: apaga cookie HttpOnly no backend e limpa sessão em memória. */
    logout(): Observable<void> {
        return this.http
            .post<void>(`${this.API}/auth/logout`, {}, { withCredentials: true })
            .pipe(finalize(() => this.clearSession()));
    }

    /** Guarda token + usuário em memória e calcula expiração. */
    setSession(resp: AuthResponse): void {
        this.accessTokenSub.next(resp.accessToken);
        this.userSub.next(resp.user);
        this.expiryEpochMs = Date.now() + resp.expiresIn * 1000;
    }

    /** Limpa tudo da sessão em memória. */
    clearSession(): void {
        this.accessTokenSub.next(null);
        this.userSub.next(null);
        this.expiryEpochMs = 0;
    }

    /** Token atual (para o HttpInterceptor consultar). */
    getAccessToken(): string | null {
        return this.accessTokenSub.value;
    }

    /** Observable do token (se preferir reagir). */
    accessToken$(): Observable<string | null> {
        return this.accessTokenSub.asObservable();
    }

    /** Usuário atual como observable (útil para header/avatar, guards, etc.). */
    currentUser$(): Observable<UserPayload | null> {
        return this.userSub.asObservable();
    }

    /** Snapshot do usuário atual. */
    getCurrentUser(): UserPayload | null {
        return this.userSub.value;
    }

    /** Está autenticado e token não expirou. */
    isAuthenticated(): boolean {
        return !!this.accessTokenSub.value && Date.now() < this.expiryEpochMs;
    }

    /** Quantos ms faltam para o token expirar (p/ agendar refresh se quiser). */
    msToExpire(): number {
        return Math.max(0, this.expiryEpochMs - Date.now());
    }
}

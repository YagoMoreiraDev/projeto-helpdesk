// src/app/core/interceptors/jwt.interceptor.ts
import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AutenticacaoService } from '../services/autenticacao.service';
import { catchError, filter, first, switchMap, throwError, BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';

const API = environment.apiUrl ?? 'http://localhost:8080';
let isRefreshing = false;
const refreshSubject = new BehaviorSubject<string | null>(null);

function mustAttachAuth(url: string): boolean {
    try {
        // chamadas relativas ao mesmo host (ex.: "/api/...") → sim
        if (url.startsWith('/')) return url.startsWith('/api/');
        // chamadas absolutas → anexa se o host bate com o da API configurada
        const u = new URL(url);
        const api = new URL(API);
        return u.host === api.host;
    } catch {
        return false;
    }
}

function addAuth(req: HttpRequest<any>, token: string | null) {
    if (!token) return req;
    if (!mustAttachAuth(req.url)) return req;
    return req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
}

export const jwtInterceptor: HttpInterceptorFn = (req: HttpRequest<any>, next: HttpHandlerFn) => {
    const auth = inject(AutenticacaoService);
    // não mexe nos endpoints de auth; eles já usam withCredentials na service
    const isAuthEndpoint = req.url.startsWith(`${API}/auth/`);
    const token = auth.getAccessToken();
    const authReq = isAuthEndpoint ? req : addAuth(req, token);

    return next(authReq).pipe(
        catchError((err: any) => {
            if (err instanceof HttpErrorResponse && err.status === 401 && !isAuthEndpoint) {
                // tenta refresh
                if (!isRefreshing) {
                    isRefreshing = true;
                    refreshSubject.next(null);
                    return auth.refresh().pipe(
                        switchMap(resp => {
                            isRefreshing = false;
                            refreshSubject.next(resp.accessToken);
                            const retried = addAuth(req, resp.accessToken);
                            return next(retried);
                        }),
                        catchError(e => {
                            isRefreshing = false;
                            auth.clearSession();
                            return throwError(() => e);
                        })
                    );
                } else {
                    // aguarda refresh em andamento
                    return refreshSubject.pipe(
                        filter(t => t !== null),
                        first(),
                        switchMap(t => next(addAuth(req, t)))
                    );
                }
            }
            return throwError(() => err);
        })
    );
};

// src/app/services/notification-client.service.ts
import { Injectable, inject } from '@angular/core';
import { environment } from '../../environments/environment';
import { NotificationDto } from '../core/models/notification.model';
import { Subject, Observable, timer, firstValueFrom } from 'rxjs';
import { AutenticacaoService } from '../services/autenticacao.service';

const API = environment.apiUrl ?? 'http://localhost:8080';

@Injectable({ providedIn: 'root' })
export class NotificationClientService {
    private auth = inject(AutenticacaoService);

    private es?: EventSource;
    private out$ = new Subject<NotificationDto>();
    private connecting = false;

    /** Exponha como Observable para os componentes. */
    stream$(): Observable<NotificationDto> {
        // Abre ao primeiro subscribe, se ainda não abriu
        if (!this.es && !this.connecting) this.connect();
        return this.out$.asObservable();
    }

    /** Abre a conexão SSE (com access_token na query). */
    async connect(): Promise<void> {
        if (this.connecting) return;
        this.connecting = true;

        // garante um token fresco antes de abrir
        let token = this.auth.getAccessToken();
        if (!token) {
            try {
                const r = await firstValueFrom(this.auth.refresh());
                token = r?.accessToken ?? null;
            } catch {
                this.connecting = false;
                return;
            }
        }

        const url = `${API}/api/notifications/stream?access_token=${encodeURIComponent(token!)
            }`;

        // Fecha conexão antiga
        this.close();

        // Importante: EventSource não aceita headers custom → usamos query param
        this.es = new EventSource(url, { withCredentials: false });

        this.es.onopen = () => {
            this.connecting = false;
            // opcional: console.log('SSE conectado');
        };

        this.es.onmessage = (ev) => {
            try {
                const data = JSON.parse(ev.data) as NotificationDto;
                this.out$.next(data);
            } catch {
                // ignora payloads não-JSON
            }
        };

        // Auto-reconexão com pequeno backoff
        this.es.onerror = async () => {
            this.close();
            // tenta refresh e reconectar
            try {
                const r = await firstValueFrom(this.auth.refresh());
                // espera um pouco e reabre
                await firstValueFrom(timer(1000));
                this.connecting = false;
                this.connect(); // chama de novo; usa token novo
            } catch {
                this.connecting = false;
                // opcional: notificar user para relogar
            }
        };
    }

    /** Fecha a conexão. */
    close() {
        if (this.es) {
            this.es.close();
            this.es = undefined;
        }
    }
}

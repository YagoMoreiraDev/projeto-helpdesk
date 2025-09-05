// src/app/core/models/notification.model.ts
export type NotificationType =
    | 'CHAMADO_CRIADO'
    | 'CHAMADO_ATRIBUIDO'
    | 'STATUS_ALTERADO';

export interface NotificationDto<T = any> {
    type: NotificationType;
    title: string;
    payload?: T;          // no seu caso, é um ChamadoResponse
    timestamp?: string;   // opcional (Instant no backend)
}

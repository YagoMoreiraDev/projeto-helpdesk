import { UUID } from '../uuid.type';
import { Role } from '../models/role.enum';

export interface UserPayload {
    id: UUID;
    nome: string;
    email: string;
    roles: Role[];
}

export interface AuthResponse {
    tokenType: 'Bearer';
    accessToken: string;  // JWT de acesso
    expiresIn: number;    // em segundos
    user: UserPayload;
}

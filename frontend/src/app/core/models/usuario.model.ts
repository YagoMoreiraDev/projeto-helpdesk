import { UUID } from '../uuid.type';
import { Role } from './role.enum';


export interface Usuario {
    id: UUID;
    nome: string;
    email: string;
    ativo: boolean;
    roles: Role[]; // backend envia como array de strings
}


export interface UsuarioCreateRequest {
    nome: string;
    email: string;
    senha: string;
    roles: Role[];
}


export interface UsuarioUpdateRequest {
    nome: string;
    email: string;
    roles: Role[];
}


export type UsuarioResponse = Usuario;
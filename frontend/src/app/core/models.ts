export type UserRole = 'ADMIN' | 'USER';

export interface AuthResponse {
  token: string;
  username: string;
  role: UserRole;
  bearerType?: string;
}

export interface Employee {
  id?: number;
  username: string;
  email: string;
  age: number;
  mobile: string;
  firstName: string;
  lastName: string;
  department?: string | null;
  salary?: number | null;
  createdAt?: string;
}

export interface PageEmployee {
  content: Employee[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first?: boolean;
  last?: boolean;
}

export const VALIDATION = {
  usernamePattern: '^[a-zA-Z0-9_]{3,50}$',
  mobilePattern: '^[0-9]{10}$'
} as const;

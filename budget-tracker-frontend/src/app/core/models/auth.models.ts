export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  companyName: string;
  fullName: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tenantId: string;
  role: 'ADMIN' | 'MEMBER';
  mustChangePassword: boolean;
}

export interface Category {
  id: string;
  name: string;
}

export interface UserResponse {
  id: string;
  fullName: string;
  email: string;
  role: 'ADMIN' | 'MEMBER';
}

export interface InviteUserRequest {
  fullName: string;
  email: string;
}

export interface InviteUserResponse extends UserResponse {
  tenantId: string;
}

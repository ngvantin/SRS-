import { apiFetch } from "@/lib/base-api";

export interface Role {
  id: number;
  name: string;
  modules: Module[];
}

export interface RoleResponse {
  content: Role[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface RoleCreateRequest {
  name: string;
}

export interface RoleUpdateRequest {
  id: number;
  name: string;
}

export interface Module {
  id: number;
  name: string;
  path: string;
}

export interface RolePermissionsRequest {
  id: number;
  name: string;
  modules: Module[];
}

// Roles API
export async function fetchRoles(page = 0, size = 10, searchTerm = ""): Promise<RoleResponse> {
  return apiFetch(`/roles?page=${page}&size=${size}${searchTerm ? `&searchTerm=${searchTerm}` : ""}`);
}

export async function fetchRoleById(id: number): Promise<Role> {
  return apiFetch(`/roles/${id}`);
}

export async function createRole(data: RoleCreateRequest): Promise<Role> {
  return apiFetch('/roles', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export async function updateRole(id: number, data: RoleUpdateRequest): Promise<Role> {
  return apiFetch(`/roles/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

export async function deleteRole(id: number): Promise<boolean> {
  return apiFetch(`/roles/${id}`, {
    method: 'DELETE',
  });
}

export async function deleteMultipleRoles(ids: number[]): Promise<boolean> {
  return apiFetch('/roles/delete-all', {
    method: 'POST',
    body: JSON.stringify({ ids }),
  });
}

export async function getAllModules(): Promise<Module[]> {
  return apiFetch('/roles/modules');
}

export async function exportRolesToExcel(page = 0, size = 10): Promise<Blob> {
  return apiFetch(`/roles/export?page=${page}&size=${size}`);
}

export async function importRoles(formData: FormData): Promise<boolean> {
  return apiFetch('/roles/import', {
    method: 'POST',
    body: formData,
  });
}

export async function updateRolePermissions(id: number, data: RolePermissionsRequest): Promise<string> {
  return apiFetch(`/roles/permissions/${id}`, {
    method: 'POST',
    body: JSON.stringify(data),
  });
}
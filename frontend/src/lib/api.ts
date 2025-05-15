import axios from 'axios';
import type {
  ModuleGroup,
  Module,
  CreateModuleGroupDto,
  UpdateModuleGroupDto,
  CreateModuleDto,
  UpdateModuleDto,
} from '@/types';

const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

export const moduleGroupApi = {
  getAll: () => api.get<ModuleGroup[]>('/module-groups').then((res) => res.data),
  getById: (id: number) => api.get<ModuleGroup>(`/module-groups/${id}`).then((res) => res.data),
  create: (data: CreateModuleGroupDto) =>
    api.post<ModuleGroup>('/module-groups', data).then((res) => res.data),
  update: (id: number, data: UpdateModuleGroupDto) =>
    api.put<ModuleGroup>(`/module-groups/${id}`, data).then((res) => res.data),
  delete: (id: number) => api.delete(`/module-groups/${id}`),
};

export const moduleApi = {
  getAll: () => api.get<Module[]>('/modules').then((res) => res.data),
  getById: (id: number) => api.get<Module>(`/modules/${id}`).then((res) => res.data),
  getByGroupId: (moduleGroupId: number) =>
    api.get<Module[]>(`/modules/group/${moduleGroupId}`).then((res) => res.data),
  create: (data: CreateModuleDto) =>
    api.post<Module>('/modules', data).then((res) => res.data),
  update: (id: number, data: UpdateModuleDto) =>
    api.put<Module>(`/modules/${id}`, data).then((res) => res.data),
  delete: (id: number) => api.delete(`/modules/${id}`),
};

export default api; 
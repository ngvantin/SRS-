export interface ModuleGroup {
  id: number;
  name: string;
  description?: string;
}

export interface Module {
  id: number;
  name: string;
  description?: string;
  moduleGroupId: number;
  url: string; // URL for the module's function
}

export interface CreateModuleGroupDto {
  name: string;
  description?: string;
}

export interface UpdateModuleGroupDto extends CreateModuleGroupDto {}

export interface CreateModuleDto {
  name: string;
  description?: string;
  moduleGroupId: number;
  url: string; // URL for the module's function
}

export interface UpdateModuleDto extends CreateModuleDto {} 
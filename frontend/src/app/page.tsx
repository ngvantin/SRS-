'use client';

import { useState } from 'react';
import { useModuleGroups } from '@/hooks/useModuleGroups';
import { useModules } from '@/hooks/useModules';
import { ModuleGroupForm } from '@/components/ModuleGroupForm';
import { ModuleForm } from '@/components/ModuleForm';
import type { CreateModuleGroupDto, CreateModuleDto } from '@/types';

export default function Home() {
  const { moduleGroups, createModuleGroup, deleteModuleGroup } = useModuleGroups();
  const { modules, createModule, deleteModule } = useModules();
  const [selectedModuleGroup, setSelectedModuleGroup] = useState<number | null>(null);
  const [isCreatingModuleGroup, setIsCreatingModuleGroup] = useState(false);
  const [isCreatingModule, setIsCreatingModule] = useState(false);

  const handleCreateModuleGroup = async (data: CreateModuleGroupDto) => {
    await createModuleGroup(data);
    setIsCreatingModuleGroup(false);
  };

  const handleCreateModule = async (data: CreateModuleDto) => {
    await createModule(data);
    setIsCreatingModule(false);
  };

  if (!moduleGroups) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="p-6">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-4">Module Groups</h1>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {moduleGroups.map((group) => (
            <div
              key={group.id}
              className="bg-white p-4 rounded-lg shadow hover:shadow-md transition-shadow"
            >
              <div className="flex justify-between items-start">
                <div>
                  <h3 className="text-lg font-semibold text-gray-900">{group.name}</h3>
                  {group.description && (
                    <p className="mt-1 text-sm text-gray-600">{group.description}</p>
                  )}
                </div>
                <button
                  onClick={() => deleteModuleGroup(group.id)}
                  className="text-red-600 hover:text-red-800"
                >
                  <svg
                    className="w-5 h-5"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                    />
                  </svg>
                </button>
              </div>
              <div className="mt-4">
                <button
                  onClick={() => {
                    setSelectedModuleGroup(group.id);
                    setIsCreatingModule(true);
                  }}
                  className="text-sm text-primary-600 hover:text-primary-800"
                >
                  + Add Module
                </button>
              </div>
            </div>
          ))}
          <button
            onClick={() => setIsCreatingModuleGroup(true)}
            className="bg-white p-4 rounded-lg shadow hover:shadow-md transition-shadow border-2 border-dashed border-gray-300 flex items-center justify-center"
          >
            <div className="text-center">
              <svg
                className="mx-auto h-12 w-12 text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 6v6m0 0v6m0-6h6m-6 0H6"
                />
              </svg>
              <span className="mt-2 block text-sm font-medium text-gray-900">
                Create Module Group
              </span>
            </div>
          </button>
        </div>
      </div>

      {isCreatingModuleGroup && (
        <div className="fixed inset-0 bg-gray-500 bg-opacity-75 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg p-6 max-w-md w-full">
            <h2 className="text-xl font-semibold mb-4">Create Module Group</h2>
            <ModuleGroupForm
              onSubmit={handleCreateModuleGroup}
              submitLabel="Create Group"
            />
            <button
              onClick={() => setIsCreatingModuleGroup(false)}
              className="mt-4 text-gray-600 hover:text-gray-800"
            >
              Cancel
            </button>
          </div>
        </div>
      )}

      {isCreatingModule && (
        <div className="fixed inset-0 bg-gray-500 bg-opacity-75 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg p-6 max-w-md w-full">
            <h2 className="text-xl font-semibold mb-4">Create Module</h2>
            <ModuleForm
              onSubmit={handleCreateModule}
              submitLabel="Create Module"
              moduleGroups={moduleGroups.map(group => ({ id: group.id, name: group.name }))}
              initialData={selectedModuleGroup ? { moduleGroupId: selectedModuleGroup } : undefined}
            />
            <button
              onClick={() => setIsCreatingModule(false)}
              className="mt-4 text-gray-600 hover:text-gray-800"
            >
              Cancel
            </button>
          </div>
        </div>
      )}
    </div>
  );
} 
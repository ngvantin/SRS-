'use client';

import { useParams } from 'next/navigation';
import { useModules } from '@/hooks/useModules';

export default function ModuleFunctionPage() {
  const params = useParams();
  const { modules } = useModules();
  const moduleId = Number(params.id);
  const module = modules.find(m => m.id === moduleId);

  if (!module) {
    return (
      <div className="p-6">
        <h1 className="text-2xl font-bold text-red-600">Module not found</h1>
        <p className="mt-2 text-gray-600">The requested module could not be found.</p>
      </div>
    );
  }

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold text-gray-900">{module.name}</h1>
      {module.description && (
        <p className="mt-2 text-gray-600">{module.description}</p>
      )}
      <div className="mt-6">
        {/* Add your module-specific content here */}
        <div className="bg-white shadow rounded-lg p-6">
          <h2 className="text-lg font-semibold text-gray-900">Module Function</h2>
          <p className="mt-2 text-gray-600">
            This is the function page for module: {module.name}
          </p>
        </div>
      </div>
    </div>
  );
} 
'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import type { CreateModuleDto } from '@/types';

const moduleSchema = z.object({
  name: z.string().min(1, 'Name is required'),
  description: z.string().optional(),
  moduleGroupId: z.number().min(1, 'Module group is required'),
  url: z.string().min(1, 'URL is required'),
});

type ModuleFormData = z.infer<typeof moduleSchema>;

interface ModuleFormProps {
  onSubmit: (data: CreateModuleDto) => void;
  initialData?: CreateModuleDto;
  submitLabel?: string;
  moduleGroups: { id: number; name: string }[];
}

export function ModuleForm({
  onSubmit,
  initialData,
  submitLabel = 'Create',
  moduleGroups,
}: ModuleFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ModuleFormData>({
    resolver: zodResolver(moduleSchema),
    defaultValues: initialData,
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div>
        <label htmlFor="name" className="block text-sm font-medium text-gray-700">
          Name
        </label>
        <input
          type="text"
          id="name"
          {...register('name')}
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
        />
        {errors.name && (
          <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>
        )}
      </div>

      <div>
        <label htmlFor="description" className="block text-sm font-medium text-gray-700">
          Description
        </label>
        <textarea
          id="description"
          {...register('description')}
          rows={3}
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
        />
      </div>

      <div>
        <label htmlFor="url" className="block text-sm font-medium text-gray-700">
          Function URL
        </label>
        <input
          type="text"
          id="url"
          {...register('url')}
          placeholder="/modules/function-name"
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
        />
        <p className="mt-1 text-sm text-gray-500">
          Enter the function URL path (e.g., /modules/user-management)
        </p>
        {errors.url && (
          <p className="mt-1 text-sm text-red-600">{errors.url.message}</p>
        )}
      </div>

      <div>
        <label htmlFor="moduleGroupId" className="block text-sm font-medium text-gray-700">
          Module Group
        </label>
        <select
          id="moduleGroupId"
          {...register('moduleGroupId', { valueAsNumber: true })}
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
        >
          <option value="">Select a module group</option>
          {moduleGroups.map((group) => (
            <option key={group.id} value={group.id}>
              {group.name}
            </option>
          ))}
        </select>
        {errors.moduleGroupId && (
          <p className="mt-1 text-sm text-red-600">{errors.moduleGroupId.message}</p>
        )}
      </div>

      <button
        type="submit"
        className="inline-flex justify-center rounded-md border border-transparent bg-primary-600 py-2 px-4 text-sm font-medium text-white shadow-sm hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2"
      >
        {submitLabel}
      </button>
    </form>
  );
} 
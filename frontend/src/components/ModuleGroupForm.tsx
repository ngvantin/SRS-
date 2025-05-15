'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import type { CreateModuleGroupDto } from '@/types';

const moduleGroupSchema = z.object({
  name: z.string().min(1, 'Name is required'),
  description: z.string().optional(),
});

type ModuleGroupFormData = z.infer<typeof moduleGroupSchema>;

interface ModuleGroupFormProps {
  onSubmit: (data: CreateModuleGroupDto) => void;
  initialData?: CreateModuleGroupDto;
  submitLabel?: string;
}

export function ModuleGroupForm({
  onSubmit,
  initialData,
  submitLabel = 'Create',
}: ModuleGroupFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ModuleGroupFormData>({
    resolver: zodResolver(moduleGroupSchema),
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

      <button
        type="submit"
        className="inline-flex justify-center rounded-md border border-transparent bg-primary-600 py-2 px-4 text-sm font-medium text-white shadow-sm hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2"
      >
        {submitLabel}
      </button>
    </form>
  );
} 
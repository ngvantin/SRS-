import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { moduleApi } from '@/lib/api';
import type { CreateModuleDto, UpdateModuleDto } from '@/types';

export function useModules(moduleGroupId?: number) {
  const queryClient = useQueryClient();

  const { data: modules = [], isLoading } = useQuery({
    queryKey: ['modules', moduleGroupId],
    queryFn: moduleGroupId
      ? () => moduleApi.getByGroupId(moduleGroupId)
      : moduleApi.getAll,
    enabled: moduleGroupId !== undefined,
  });

  const createMutation = useMutation({
    mutationFn: moduleApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['modules'] });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateModuleDto }) =>
      moduleApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['modules'] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: moduleApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['modules'] });
    },
  });

  return {
    modules,
    isLoading,
    createModule: createMutation.mutate,
    updateModule: updateMutation.mutate,
    deleteModule: deleteMutation.mutate,
  };
} 
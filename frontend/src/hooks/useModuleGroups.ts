import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { moduleGroupApi } from '@/lib/api';
import type { CreateModuleGroupDto, UpdateModuleGroupDto } from '@/types';

export function useModuleGroups() {
  const queryClient = useQueryClient();

  const { data: moduleGroups = [], isLoading } = useQuery({
    queryKey: ['moduleGroups'],
    queryFn: moduleGroupApi.getAll,
  });

  const createMutation = useMutation({
    mutationFn: moduleGroupApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['moduleGroups'] });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateModuleGroupDto }) =>
      moduleGroupApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['moduleGroups'] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: moduleGroupApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['moduleGroups'] });
    },
  });

  return {
    moduleGroups,
    isLoading,
    createModuleGroup: createMutation.mutate,
    updateModuleGroup: updateMutation.mutate,
    deleteModuleGroup: deleteMutation.mutate,
  };
} 
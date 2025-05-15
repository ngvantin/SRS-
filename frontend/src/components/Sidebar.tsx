'use client';

import { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import type { ModuleGroup, Module } from '@/types';

interface SidebarProps {
  moduleGroups: ModuleGroup[];
  modules: Module[];
}

export function Sidebar({ moduleGroups, modules }: SidebarProps) {
  const [expandedGroups, setExpandedGroups] = useState<number[]>([]);
  const pathname = usePathname();

  const toggleGroup = (groupId: number) => {
    setExpandedGroups(prev =>
      prev.includes(groupId)
        ? prev.filter(id => id !== groupId)
        : [...prev, groupId]
    );
  };

  const getModulesForGroup = (groupId: number) => {
    return modules.filter(module => module.moduleGroupId === groupId);
  };

  const isModuleActive = (module: Module) => {
    return pathname === `/modules/${module.id}`;
  };

  return (
    <div className="w-64 bg-white h-screen shadow-lg fixed left-0 top-0 overflow-y-auto">
      <div className="p-4">
        <h2 className="text-xl font-bold text-gray-800 mb-4">Menu</h2>
        <nav className="space-y-2">
          {moduleGroups.map((group) => (
            <div key={group.id} className="space-y-1">
              <button
                onClick={() => toggleGroup(group.id)}
                className={`w-full flex items-center justify-between px-4 py-2 text-sm font-medium rounded-md ${
                  expandedGroups.includes(group.id)
                    ? 'bg-primary-50 text-primary-700'
                    : 'text-gray-700 hover:bg-gray-50'
                }`}
              >
                <span>{group.name}</span>
                <svg
                  className={`w-4 h-4 transform transition-transform ${
                    expandedGroups.includes(group.id) ? 'rotate-180' : ''
                  }`}
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M19 9l-7 7-7-7"
                  />
                </svg>
              </button>
              {expandedGroups.includes(group.id) && (
                <div className="ml-4 space-y-1">
                  {getModulesForGroup(group.id).map((module) => (
                    <Link
                      key={module.id}
                      href={`/modules/${module.id}`}
                      className={`block px-4 py-2 text-sm rounded-md ${
                        isModuleActive(module)
                          ? 'bg-primary-100 text-primary-700'
                          : 'text-gray-700 hover:bg-gray-50'
                      }`}
                    >
                      {module.name}
                    </Link>
                  ))}
                </div>
              )}
            </div>
          ))}
        </nav>
      </div>
    </div>
  );
} 
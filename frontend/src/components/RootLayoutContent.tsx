'use client';

import { Sidebar } from '@/components/Sidebar';
import { useModuleGroups } from '@/hooks/useModuleGroups';
import { useModules } from '@/hooks/useModules';
import { usePathname } from 'next/navigation';

export function RootLayoutContent({ children }: { children: React.ReactNode }) {
  const { moduleGroups } = useModuleGroups();
  const { modules } = useModules();
  const pathname = usePathname();
  
  const isAuthPage = pathname?.startsWith('/auth');

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex">
              <div className="flex-shrink-0 flex items-center">
                <h1 className="text-xl font-bold text-primary-600">LMS</h1>
              </div>
            </div>
          </div>
        </div>
      </nav>
      <div className="flex">
        {!isAuthPage && moduleGroups && modules && (
          <Sidebar moduleGroups={moduleGroups} modules={modules} />
        )}
        <main className={!isAuthPage ? "flex-1 ml-64 p-6" : "flex-1 p-6"}>
          {children}
        </main>
      </div>
    </div>
  );
}
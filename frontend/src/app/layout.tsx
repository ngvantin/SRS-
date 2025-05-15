import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import './globals.css';
import { QueryProvider } from '@/providers/QueryProvider';
import { RootLayoutContent } from '@/components/RootLayoutContent';

const inter = Inter({ subsets: ['latin'] });

export const metadata: Metadata = {
  title: 'Dynamic Menu System',
  description: 'A system for managing dynamic menu structures',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className={inter.className}>
        <QueryProvider>
          <RootLayoutContent>
            {children}
          </RootLayoutContent>
        </QueryProvider>
      </body>
    </html>
  );
} 
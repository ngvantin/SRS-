import { apiFetch } from '@/lib/base-api';
import { NextResponse } from 'next/server';

export async function POST(request: Request) {
  try {
    const { newRole } = await request.json();
    const data = await apiFetch('/auth/switch-role', {
      method: 'POST',
      body: JSON.stringify({ newRole }),
    });
    return NextResponse.json(data);
  } catch (error: any) {
    return NextResponse.json(
      { error: error.message },
      { status: 400 }
    );
  }
}
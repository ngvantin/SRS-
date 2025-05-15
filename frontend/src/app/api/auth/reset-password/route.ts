import { apiFetch } from '@/lib/base-api';
import { NextResponse } from 'next/server';

export async function POST(request: Request) {
  try {
    const { newPassword, confirmPassword } = await request.json();
    const data = await apiFetch('/auth/reset-password', {
      method: 'POST',
      body: JSON.stringify({ newPassword, confirmPassword }),
    });
    return NextResponse.json(data);
  } catch (error: any) {
    return NextResponse.json(
      { error: error.message },
      { status: 400 }
    );
  }
}
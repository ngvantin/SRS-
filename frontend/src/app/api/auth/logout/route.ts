import { apiFetch } from '@/lib/base-api';
import { NextResponse } from 'next/server';

export async function POST() {
  try {
    const data = await apiFetch('/auth/logout', {
      method: 'POST',
    });
    return NextResponse.json(data);
  } catch (error: any) {
    return NextResponse.json(
      { error: error.message },
      { status: 400 }
    );
  }
}
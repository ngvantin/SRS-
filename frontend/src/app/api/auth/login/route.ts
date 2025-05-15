import { apiFetch } from '@/lib/base-api';
import { NextResponse } from 'next/server';

export async function POST(username: string, password: string) {
  try {

    const data = await apiFetch('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username: username, password: password }),
    });

    return NextResponse.json(data);
  } catch (error: any) {
    return NextResponse.json(
      { error: error.message },
      { status: 400 }
    );
  }
}
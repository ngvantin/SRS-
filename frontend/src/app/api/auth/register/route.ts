import { apiFetch } from '@/lib/base-api';
import { NextResponse } from 'next/server';

export async function POST(request: {
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
}) {
  try {
    const body = request;
    const data = await apiFetch('/auth/register', {
      method: 'POST',
      body: JSON.stringify(body),
    });
    return NextResponse.json(data);
  } catch (error: any) {
    return NextResponse.json(
      { error: error.message },
      { status: 400 }
    );
  }
}
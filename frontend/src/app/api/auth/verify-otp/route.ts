import { apiFetch } from '@/lib/base-api';
import { NextResponse } from 'next/server';

export async function POST(request: {
  otp: string;
  action: string;
}) {
  try {
    const body = await request;

    if (!body.action) {
      body.action = 'verify';
    }
    
    const data = await apiFetch('/auth/verify-otp', {
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
import { getClient } from '@/lib/lh-client';
import { NextResponse } from 'next/server';

export async function GET() {
  try {
    console.log('Health check starting...');
    console.log('Environment variables:');
    console.log('LHC_API_HOST:', process.env.LHC_API_HOST || 'localhost');
    console.log('LHC_API_PORT:', process.env.LHC_API_PORT || '2023');
    console.log('LHC_API_PROTOCOL:', process.env.LHC_API_PROTOCOL || 'PLAINTEXT');
    
    const client = getClient();
    console.log('Client created, attempting connection...');
    
    // Try to make a simple call to verify connectivity
    // Use searchWfSpec instead which doesn't require parameters
    await client.searchWfSpec({ limit: 1 });
    console.log('Health check passed!');
    
    return NextResponse.json({ ok: true });
  } catch (error) {
    console.error('Health check failed:', error);
    return NextResponse.json({ ok: false }, { status: 500 });
  }
}

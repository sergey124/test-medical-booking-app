import type { AssessmentResponse, BookingResponse } from '../types';

const BASE_URL = 'http://localhost:8080';

export async function submitAssessment(score: number): Promise<AssessmentResponse> {
  const res = await fetch(`${BASE_URL}/assessment`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ score }),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `Assessment failed (${res.status})`);
  }
  return res.json();
}

export async function confirmBooking(
  slot: string,
  recommendation: string
): Promise<BookingResponse> {
  const res = await fetch(`${BASE_URL}/booking`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ slot, recommendation }),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `Booking failed (${res.status})`);
  }
  return res.json();
}

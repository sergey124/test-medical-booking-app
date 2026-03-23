import { useState } from 'react';
import type { AssessmentResponse, BookingResponse } from '../types';
import { confirmBooking } from '../api';

interface Props {
  result: AssessmentResponse;
  onConfirmed: (booking: BookingResponse) => void;
  onCancel: () => void;
}

const RECOMMENDATION_INFO: Record<string, { icon: string; color: string; description: string }> = {
  Chat: {
    icon: '💬',
    color: 'bg-blue-50 border-blue-200 text-blue-900',
    description:
      'Your symptoms appear mild. A chat consultation is recommended — a healthcare professional will answer your questions online.',
  },
  Nurse: {
    icon: '🩺',
    color: 'bg-yellow-50 border-yellow-200 text-yellow-900',
    description:
      'Based on your answers, a nurse consultation is recommended. A nurse will assess your symptoms and advise on next steps.',
  },
  Doctor: {
    icon: '👨‍⚕️',
    color: 'bg-red-50 border-red-200 text-red-900',
    description:
      'Your symptoms suggest you need to see a doctor. Please choose a slot below for a medical assessment.',
  },
};

function formatSlot(iso: string): string {
  return new Date(iso).toLocaleString(undefined, {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function Results({ result, onConfirmed, onCancel }: Props) {
  const [selectedSlot, setSelectedSlot] = useState<string | null>(null);
  const [isBooking, setIsBooking] = useState(false);
  const [error, setError] = useState('');

  const info = RECOMMENDATION_INFO[result.recommendation] ?? {
    icon: '🏥',
    color: 'bg-gray-50 border-gray-200 text-gray-900',
    description: 'Please choose a slot below to book your appointment.',
  };

  function handleBook() {
    if (!selectedSlot) return;
    setIsBooking(true);
    setError('');
    confirmBooking(selectedSlot, result.recommendation)
      .then(onConfirmed)
      .catch(err => {
        setError(err.message || 'Booking failed. Please try again.');
        setIsBooking(false);
      });
  }

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-blue-50 to-teal-50 px-4 py-8">
      <div className="w-full max-w-lg bg-white rounded-2xl shadow-lg p-6 sm:p-8">
        <h2 className="text-2xl font-bold text-gray-900 mb-4">Your Recommendation</h2>

        {/* Recommendation card */}
        <div className={`border-2 rounded-xl p-5 mb-6 ${info.color}`}>
          <div className="flex items-center gap-3 mb-2">
            <span className="text-3xl" aria-hidden="true">{info.icon}</span>
            <span className="text-xl font-bold">{result.recommendation}</span>
          </div>
          <p className="text-sm leading-relaxed">{info.description}</p>
        </div>

        {/* Slot selection */}
        <h3 className="text-base font-semibold text-gray-800 mb-3">
          Available appointment slots
        </h3>

        {result.availableSlots.length === 0 ? (
          <p className="text-sm text-gray-500 mb-6">No slots available at the moment. Please try again later.</p>
        ) : (
          <div
            className="grid grid-cols-1 gap-2 mb-6 max-h-64 overflow-y-auto pr-1"
            role="listbox"
            aria-label="Available appointment slots"
          >
            {result.availableSlots.slice(0, 20).map(slot => (
              <button
                key={slot}
                role="option"
                aria-selected={selectedSlot === slot}
                onClick={() => setSelectedSlot(slot)}
                className={`px-4 py-2.5 rounded-lg text-sm font-medium border-2 transition text-left focus:outline-none focus:ring-2 focus:ring-teal-400
                  ${selectedSlot === slot
                    ? 'border-teal-500 bg-teal-50 text-teal-900'
                    : 'border-gray-200 text-gray-700 hover:border-teal-300'}`}
              >
                {formatSlot(slot)}
              </button>
            ))}
          </div>
        )}

        {error && (
          <div role="alert" className="mb-4 text-sm text-red-600 bg-red-50 border border-red-200 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        <div className="flex gap-3">
          <button
            onClick={onCancel}
            disabled={isBooking}
            className="flex-1 py-2.5 border border-gray-300 text-gray-600 rounded-lg text-sm font-medium hover:bg-gray-50 transition disabled:opacity-50 focus:outline-none focus:ring-2 focus:ring-gray-300"
          >
            Cancel
          </button>
          <button
            onClick={handleBook}
            disabled={!selectedSlot || isBooking}
            className="flex-1 py-2.5 bg-teal-600 hover:bg-teal-700 text-white rounded-lg text-sm font-medium transition disabled:opacity-40 disabled:cursor-not-allowed focus:outline-none focus:ring-2 focus:ring-teal-400 focus:ring-offset-2"
            aria-label="Confirm appointment booking"
          >
            {isBooking ? (
              <span className="flex items-center justify-center gap-2">
                <svg className="animate-spin w-4 h-4" viewBox="0 0 24 24" fill="none">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
                </svg>
                Booking…
              </span>
            ) : 'Confirm Booking'}
          </button>
        </div>
      </div>
    </div>
  );
}

import type { BookingResponse } from '../types';

interface Props {
  booking: BookingResponse;
  onReturnHome: () => void;
}

function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString(undefined, {
    weekday: 'long',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function BookingConfirmation({ booking, onReturnHome }: Props) {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-blue-50 to-teal-50 px-4">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-lg p-8 text-center">
        {/* Success icon */}
        <div className="inline-flex items-center justify-center w-16 h-16 bg-teal-100 rounded-full mb-6">
          <svg className="w-8 h-8 text-teal-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
        </div>

        <h2 className="text-2xl font-bold text-gray-900 mb-2">Booking Confirmed!</h2>
        <p className="text-gray-500 text-sm mb-8">Your appointment has been successfully booked.</p>

        <div className="bg-gray-50 rounded-xl p-5 text-left space-y-3 mb-8">
          <div>
            <p className="text-xs uppercase tracking-wide text-gray-400 font-medium">Date &amp; Time</p>
            <p className="text-sm font-semibold text-gray-900 mt-0.5">{formatDateTime(booking.slot)}</p>
          </div>
          <div>
            <p className="text-xs uppercase tracking-wide text-gray-400 font-medium">Care Type</p>
            <p className="text-sm font-semibold text-gray-900 mt-0.5">{booking.recommendation}</p>
          </div>
          <div>
            <p className="text-xs uppercase tracking-wide text-gray-400 font-medium">Your Specialist</p>
            <p className="text-sm font-semibold text-gray-900 mt-0.5">
              {booking.specialistName}
              <span className="ml-2 text-xs font-normal text-gray-500">
                ({booking.specialistType.charAt(0) + booking.specialistType.slice(1).toLowerCase()})
              </span>
            </p>
          </div>
        </div>

        <button
          onClick={onReturnHome}
          className="w-full py-3 bg-teal-600 hover:bg-teal-700 text-white font-medium rounded-xl transition focus:outline-none focus:ring-2 focus:ring-teal-400 focus:ring-offset-2"
          aria-label="Return to home screen"
        >
          Return to Home
        </button>
      </div>
    </div>
  );
}

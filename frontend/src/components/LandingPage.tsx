import { formatDateTime, formatWaitTime } from '../utils/format';

interface Props {
  bookedSlot?: string;
  onBook: () => void;
}

export default function LandingPage({ bookedSlot, onBook }: Props) {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-blue-50 to-teal-50 px-4 text-center">
      <div className="max-w-lg w-full">
        {/* Logo / icon */}
        <div className="inline-flex items-center justify-center w-20 h-20 bg-teal-100 rounded-full mb-6">
          {bookedSlot ? (
            /* Checkmark — appointment confirmed */
            <svg aria-label="Appointment confirmed" className="w-10 h-10 text-teal-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4.5 12.75l6 6 9-13.5" />
            </svg>
          ) : (
            /* Stethoscope — default triage entry */
            <svg aria-label="Medical triage" className="w-10 h-10 text-teal-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                d="M9 3v5a3 3 0 006 0V3M6 8a6 6 0 0012 0M12 14v3m0 0a3 3 0 100 6 3 3 0 000-6z" />
            </svg>
          )}
        </div>

        <h1 className="text-4xl font-bold text-gray-900 mb-3">
          {bookedSlot ? 'Your appointment is booked' : 'See a doctor in 15 mins'}
        </h1>

        {bookedSlot ? (
          <>
            <p className="text-gray-600 mb-2 text-lg">
              Your appointment starts in{' '}
              <span className="font-semibold text-teal-700">{formatWaitTime(bookedSlot)}</span>
            </p>
            <p className="text-gray-500 text-sm mb-8">
              Scheduled for {formatDateTime(bookedSlot)}
            </p>
            <button
              onClick={onBook}
              className="px-8 py-3 bg-teal-600 hover:bg-teal-700 text-white font-semibold rounded-xl transition focus:outline-none focus:ring-2 focus:ring-teal-400 focus:ring-offset-2"
            >
              Book another appointment
            </button>
          </>
        ) : (
          <>
            <p className="text-gray-500 text-lg mb-10">
              Answer a few quick questions so we can connect you with the right care.
            </p>
            <button
              onClick={onBook}
              className="px-10 py-4 bg-teal-600 hover:bg-teal-700 text-white text-lg font-semibold rounded-xl shadow-md transition focus:outline-none focus:ring-2 focus:ring-teal-400 focus:ring-offset-2"
            >
              Book Meeting
            </button>
          </>
        )}
      </div>
    </div>
  );
}

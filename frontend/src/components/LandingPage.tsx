interface Props {
  bookedSlot?: string;
  onBook: () => void;
}

function formatWaitTime(slot: string): string {
  const slotDate = new Date(slot);
  const now = new Date();
  const diffMs = slotDate.getTime() - now.getTime();
  if (diffMs <= 0) return 'shortly';
  const diffMins = Math.floor(diffMs / 60000);
  if (diffMins < 60) return `${diffMins} min${diffMins !== 1 ? 's' : ''}`;
  const diffHours = Math.floor(diffMins / 60);
  if (diffHours < 24) return `${diffHours} hr${diffHours !== 1 ? 's' : ''}`;
  const diffDays = Math.floor(diffHours / 24);
  return `${diffDays} day${diffDays !== 1 ? 's' : ''}`;
}

function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString(undefined, {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function LandingPage({ bookedSlot, onBook }: Props) {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-blue-50 to-teal-50 px-4 text-center">
      <div className="max-w-lg w-full">
        {/* Logo / icon */}
        <div className="inline-flex items-center justify-center w-20 h-20 bg-teal-100 rounded-full mb-6">
          <svg className="w-10 h-10 text-teal-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
              d="M4.5 12.75l6 6 9-13.5" />
          </svg>
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
              aria-label="Book another appointment"
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
              aria-label="Start booking a meeting"
            >
              Book Meeting
            </button>
          </>
        )}
      </div>
    </div>
  );
}

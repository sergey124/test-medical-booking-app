import type { Question } from '../types';

interface Props {
  question: Question;
  selectedScore: number | null;
  onSelect: (score: number) => void;
  onBack: () => void;
  onCancel: () => void;
  onNext: () => void;
  isFirst: boolean;
  isLast: boolean;
  isSubmitting: boolean;
}

export default function QuestionCard({
  question,
  selectedScore,
  onSelect,
  onBack,
  onCancel,
  onNext,
  isFirst,
  isLast,
  isSubmitting,
}: Props) {
  return (
    <fieldset className="w-full" aria-labelledby={`q${question.id}-label`}>
      <legend id={`q${question.id}-label`} className="text-xl font-semibold text-gray-900 mb-6 block">
        {question.text}
      </legend>

      <div className="space-y-3 mb-8">
        {question.options.map(opt => {
          const selected = selectedScore === opt.score;
          return (
            <label
              key={opt.label}
              className={`flex items-center gap-4 p-4 rounded-xl border-2 cursor-pointer transition select-none
                ${selected
                  ? 'border-teal-500 bg-teal-50 text-teal-900'
                  : 'border-gray-200 hover:border-teal-300 text-gray-700'}`}
            >
              <input
                type="radio"
                name={`question-${question.id}`}
                value={opt.score}
                checked={selected}
                onChange={() => onSelect(opt.score)}
                className="sr-only"
                aria-label={opt.label}
              />
              <span className={`w-5 h-5 rounded-full border-2 flex-shrink-0 flex items-center justify-center
                ${selected ? 'border-teal-500 bg-teal-500' : 'border-gray-400'}`}>
                {selected && <span className="w-2 h-2 bg-white rounded-full" />}
              </span>
              <span className="text-base">{opt.label}</span>
            </label>
          );
        })}
      </div>

      <div className="flex items-center justify-between">
        <div className="flex gap-2">
          {!isFirst && (
            <button
              onClick={onBack}
              disabled={isSubmitting}
              className="px-4 py-2 text-sm text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50 transition disabled:opacity-50 focus:outline-none focus:ring-2 focus:ring-gray-300"
              aria-label="Go back to previous question"
            >
              ← Back
            </button>
          )}
          <button
            onClick={onCancel}
            disabled={isSubmitting}
            className="px-4 py-2 text-sm text-gray-500 hover:text-red-600 transition disabled:opacity-50 focus:outline-none focus:ring-2 focus:ring-gray-300 rounded-lg"
            aria-label="Cancel and restart"
          >
            Cancel
          </button>
        </div>

        <button
          onClick={onNext}
          disabled={selectedScore === null || isSubmitting}
          className="px-6 py-2.5 bg-teal-600 hover:bg-teal-700 text-white font-medium rounded-lg transition disabled:opacity-40 disabled:cursor-not-allowed focus:outline-none focus:ring-2 focus:ring-teal-400 focus:ring-offset-2"
          aria-label={isLast ? 'Submit answers' : 'Next question'}
        >
          {isSubmitting ? (
            <span className="flex items-center gap-2">
              <svg className="animate-spin w-4 h-4" viewBox="0 0 24 24" fill="none">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
              </svg>
              Submitting…
            </span>
          ) : isLast ? 'Submit' : 'Next →'}
        </button>
      </div>
    </fieldset>
  );
}

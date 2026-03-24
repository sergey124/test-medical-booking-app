import { useState } from 'react';
import { questions } from '../data/questions';
import { submitAssessment } from '../api';
import ProgressBar from './ProgressBar';
import QuestionCard from './QuestionCard';
import { useAppStore } from '../store/appStore';

export default function Questionnaire() {
  const completeAssessment = useAppStore((s) => s.completeAssessment);
  const cancel = useAppStore((s) => s.cancel);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [answers, setAnswers] = useState<(number | null)[]>(Array(questions.length).fill(null));
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [direction, setDirection] = useState<'forward' | 'backward'>('forward');
  const [animating, setAnimating] = useState(false);

  const currentQuestion = questions[currentIndex];
  const isFirst = currentIndex === 0;
  const isLast = currentIndex === questions.length - 1;

  function selectAnswer(score: number) {
    const updated = [...answers];
    updated[currentIndex] = score;
    setAnswers(updated);
  }

  function navigate(nextIndex: number, dir: 'forward' | 'backward') {
    setDirection(dir);
    setAnimating(true);
    setTimeout(() => {
      setCurrentIndex(nextIndex);
      setAnimating(false);
    }, 200);
  }

  function handleNext() {
    if (answers[currentIndex] === null) return;
    if (!isLast) {
      navigate(currentIndex + 1, 'forward');
      return;
    }
    // Submit
    const total = answers
      .filter((a): a is number => a !== null)
      .reduce((sum, s) => sum + s, 0);
    setIsSubmitting(true);
    setError('');
    submitAssessment(total)
      .then(result => completeAssessment(result))
      .catch(err => {
        setError(err.message || 'Something went wrong. Please try again.');
        setIsSubmitting(false);
      });
  }

  function handleBack() {
    if (!isFirst) navigate(currentIndex - 1, 'backward');
  }

  const slideClass = animating
    ? direction === 'forward'
      ? 'opacity-0 translate-x-4'
      : 'opacity-0 -translate-x-4'
    : 'opacity-100 translate-x-0';

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-blue-50 to-teal-50 px-4 py-8">
      <div className="w-full max-w-lg bg-white rounded-2xl shadow-lg p-6 sm:p-8">
        <div className="mb-8">
          <ProgressBar current={currentIndex + 1} total={questions.length} />
        </div>

        <div className={`transition-all duration-200 ${slideClass}`}>
          <QuestionCard
            key={currentQuestion.id}
            question={currentQuestion}
            selectedScore={answers[currentIndex]}
            onSelect={selectAnswer}
            onBack={handleBack}
            onCancel={cancel}
            onNext={handleNext}
            isFirst={isFirst}
            isLast={isLast}
            isSubmitting={isSubmitting}
          />
        </div>

        {error && (
          <div role="alert" className="mt-4 text-sm text-red-600 bg-red-50 border border-red-200 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}
      </div>
    </div>
  );
}

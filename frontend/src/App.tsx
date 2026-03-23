import { useState } from 'react';
import type { Screen, AssessmentResponse, BookingResponse } from './types';
import Login from './components/Login';
import LandingPage from './components/LandingPage';
import Questionnaire from './components/Questionnaire';
import Results from './components/Results';
import BookingConfirmation from './components/BookingConfirmation';

export default function App() {
  const [screen, setScreen] = useState<Screen>('login');
  const [assessmentResult, setAssessmentResult] = useState<AssessmentResponse | null>(null);
  const [booking, setBooking] = useState<BookingResponse | null>(null);

  function handleLogin() {
    setScreen('landing');
  }

  function handleStartBooking() {
    setAssessmentResult(null);
    setBooking(null);
    setScreen('questionnaire');
  }

  function handleAssessmentComplete(result: AssessmentResponse) {
    setAssessmentResult(result);
    setScreen('results');
  }

  function handleBookingConfirmed(b: BookingResponse) {
    setBooking(b);
    setScreen('confirmation');
  }

  function handleReturnHome() {
    setScreen('home-booked');
  }

  function handleCancel() {
    setScreen('landing');
  }

  switch (screen) {
    case 'login':
      return <Login onLogin={handleLogin} />;

    case 'landing':
      return <LandingPage onBook={handleStartBooking} />;

    case 'questionnaire':
      return (
        <Questionnaire
          onComplete={handleAssessmentComplete}
          onCancel={handleCancel}
        />
      );

    case 'results':
      return assessmentResult ? (
        <Results
          result={assessmentResult}
          onConfirmed={handleBookingConfirmed}
          onCancel={handleCancel}
        />
      ) : null;

    case 'confirmation':
      return booking ? (
        <BookingConfirmation
          booking={booking}
          onReturnHome={handleReturnHome}
        />
      ) : null;

    case 'home-booked':
      return (
        <LandingPage
          bookedSlot={booking?.slot}
          onBook={handleStartBooking}
        />
      );
  }
}

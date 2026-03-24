import { useAppStore } from './store/appStore';
import Login from './components/Login';
import LandingPage from './components/LandingPage';
import Questionnaire from './components/Questionnaire';
import Results from './components/Results';
import BookingConfirmation from './components/BookingConfirmation';

export default function App() {
  const { screen, assessmentResult, booking } = useAppStore();

  switch (screen) {
    case 'login':
      return <Login />;

    case 'landing':
    case 'home-booked':
      return <LandingPage />;

    case 'questionnaire':
      return <Questionnaire />;

    case 'results':
      return assessmentResult ? <Results /> : null;

    case 'confirmation':
      return booking ? <BookingConfirmation /> : null;
  }
}

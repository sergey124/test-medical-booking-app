export type Screen =
  | 'login'
  | 'landing'
  | 'questionnaire'
  | 'results'
  | 'confirmation'
  | 'home-booked';

export interface Question {
  id: number;
  text: string;
  options: { label: string; score: number }[];
}

export interface AssessmentResponse {
  recommendation: string;
  availableSlots: string[];
}

export interface BookingResponse {
  confirmationId: string;
  slot: string;
  recommendation: string;
  specialistName: string;
  specialistType: string;
}

export type Recommendation = 'Chat' | 'Nurse' | 'Doctor';

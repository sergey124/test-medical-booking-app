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

export type Recommendation = 'Chat' | 'Nurse' | 'Doctor';

export interface AssessmentResponse {
  recommendation: Recommendation;
  availableSlots: string[];
}

export interface BookingResponse {
  confirmationId: string;
  slot: string;
  recommendation: Recommendation;
  specialistName: string;
  specialistType: string;
}

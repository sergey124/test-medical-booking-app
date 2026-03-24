import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import type { Screen, AssessmentResponse, BookingResponse } from '../types';

interface AppStore {
  screen: Screen;
  assessmentResult: AssessmentResponse | null;
  booking: BookingResponse | null;
  login: () => void;
  startBooking: () => void;
  completeAssessment: (r: AssessmentResponse) => void;
  confirmBooking: (b: BookingResponse) => void;
  returnHome: () => void;
  cancel: () => void;
}

export const useAppStore = create<AppStore>()(
  persist(
    (set) => ({
      screen: 'login',
      assessmentResult: null,
      booking: null,
      login:              () => set({ screen: 'landing' }),
      startBooking:       () => set({ screen: 'questionnaire', assessmentResult: null, booking: null }),
      completeAssessment: (r) => set({ assessmentResult: r, screen: 'results' }),
      confirmBooking:     (b) => set({ booking: b, screen: 'confirmation' }),
      returnHome:         () => set({ screen: 'home-booked' }),
      cancel:             () => set({ screen: 'landing' }),
    }),
    {
      name: 'triage-app',
      storage: createJSONStorage(() => sessionStorage),
    }
  )
);

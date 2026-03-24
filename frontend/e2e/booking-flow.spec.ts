import { test, expect } from '@playwright/test';

// Shared helper: log in with the demo credentials
async function login(page: import('@playwright/test').Page) {
  await page.goto('/');
  await page.fill('#username', 'patient');
  await page.fill('#password', 'kry123');
  await page.click('button[type="submit"]');
  await expect(page.getByRole('heading', { name: /see a doctor/i })).toBeVisible();
}

// Shared helper: answer all 5 triage questions by always picking the first option
async function answerQuestionnaire(page: import('@playwright/test').Page) {
  for (let i = 0; i < 5; i++) {
    const options = page.locator('[role="radio"], label:has(input[type="radio"])');
    await options.first().click();
    const nextBtn = page.getByRole('button', { name: /next|submit/i });
    await nextBtn.click();
    if (i < 4) {
      // Wait for the next question to appear after transition
      await page.waitForTimeout(250);
    }
  }
}

// ── Login ────────────────────────────────────────────────────────────────────

test('shows error for wrong credentials', async ({ page }) => {
  await page.goto('/');
  await page.fill('#username', 'wrong');
  await page.fill('#password', 'wrong');
  await page.click('button[type="submit"]');
  await expect(page.getByRole('alert')).toBeVisible();
});

test('logs in with correct credentials and reaches landing page', async ({ page }) => {
  await login(page);
});

// ── Landing page ─────────────────────────────────────────────────────────────

test('landing page shows stethoscope icon (no booking yet)', async ({ page }) => {
  await login(page);
  await expect(page.locator('svg[aria-label="Medical triage"]')).toBeVisible();
  await expect(page.locator('svg[aria-label="Appointment confirmed"]')).not.toBeVisible();
});

// ── Questionnaire ────────────────────────────────────────────────────────────

test('questionnaire shows progress bar and 5 questions', async ({ page }) => {
  await login(page);
  await page.getByRole('button', { name: /book meeting/i }).click();

  await expect(page.getByRole('progressbar')).toBeVisible();
  await expect(page.getByText(/question 1 of 5/i)).toBeVisible();
});

test('next button is disabled until an option is selected', async ({ page }) => {
  await login(page);
  await page.getByRole('button', { name: /book meeting/i }).click();

  const nextBtn = page.getByRole('button', { name: /next/i });
  await expect(nextBtn).toBeDisabled();

  const firstOption = page.locator('label:has(input[type="radio"])').first();
  await firstOption.click();
  await expect(nextBtn).toBeEnabled();
});

test('back button is hidden on first question and visible on second', async ({ page }) => {
  await login(page);
  await page.getByRole('button', { name: /book meeting/i }).click();

  await expect(page.getByRole('button', { name: /back/i })).not.toBeVisible();

  const firstOption = page.locator('label:has(input[type="radio"])').first();
  await firstOption.click();
  await page.getByRole('button', { name: /next/i }).click();
  await page.waitForTimeout(250);

  await expect(page.getByRole('button', { name: /back/i })).toBeVisible();
});

test('cancel returns to landing page', async ({ page }) => {
  await login(page);
  await page.getByRole('button', { name: /book meeting/i }).click();
  await page.getByRole('button', { name: /cancel/i }).click();
  await expect(page.getByRole('heading', { name: /see a doctor/i })).toBeVisible();
});

// ── Results & slot selection ─────────────────────────────────────────────────

test('results page shows recommendation and day tabs after questionnaire', async ({ page }) => {
  await login(page);
  await page.getByRole('button', { name: /book meeting/i }).click();
  await answerQuestionnaire(page);

  await expect(page.getByRole('heading', { name: /your recommendation/i })).toBeVisible();
  // At least one day tab (Today or Tomorrow)
  await expect(page.getByRole('tab').first()).toBeVisible();
});

test('confirm booking button is disabled until a time slot is selected', async ({ page }) => {
  await login(page);
  await page.getByRole('button', { name: /book meeting/i }).click();
  await answerQuestionnaire(page);

  await expect(page.getByRole('button', { name: /confirm booking/i })).toBeDisabled();

  // Select first available time
  await page.getByRole('option').first().click();
  await expect(page.getByRole('button', { name: /confirm booking/i })).toBeEnabled();
});

test('switching day tab resets time selection', async ({ page }) => {
  await login(page);
  await page.getByRole('button', { name: /book meeting/i }).click();
  await answerQuestionnaire(page);

  const tabs = page.getByRole('tab');
  const tabCount = await tabs.count();

  if (tabCount >= 2) {
    // Select a time on day 1
    await page.getByRole('option').first().click();
    await expect(page.getByRole('button', { name: /confirm booking/i })).toBeEnabled();

    // Switch to day 2 — confirm button should be disabled again
    await tabs.nth(1).click();
    await expect(page.getByRole('button', { name: /confirm booking/i })).toBeDisabled();
  }
});

// ── Full booking flow ────────────────────────────────────────────────────────

test('completes full booking flow and shows confirmation', async ({ page }) => {
  await login(page);
  await page.getByRole('button', { name: /book meeting/i }).click();
  await answerQuestionnaire(page);

  // Pick the first available slot
  await page.getByRole('option').first().click();
  await page.getByRole('button', { name: /confirm booking/i }).click();

  await expect(page.getByRole('heading', { name: /booking confirmed/i })).toBeVisible();
  await expect(page.getByText(/your appointment has been successfully booked/i)).toBeVisible();
});

test('after booking, landing page shows checkmark icon and appointment time', async ({ page }) => {
  await login(page);
  await page.getByRole('button', { name: /book meeting/i }).click();
  await answerQuestionnaire(page);

  await page.getByRole('option').first().click();
  await page.getByRole('button', { name: /confirm booking/i }).click();
  await page.getByRole('button', { name: /return to home/i }).click();

  await expect(page.locator('svg[aria-label="Appointment confirmed"]')).toBeVisible();
  await expect(page.getByText(/your appointment is booked/i)).toBeVisible();
  await expect(page.getByText(/your appointment starts in/i)).toBeVisible();
});

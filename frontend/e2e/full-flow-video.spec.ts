import { test, expect } from '@playwright/test';

test('full booking flow', async ({ page }) => {
  // Login
  await page.goto('/');
  await page.fill('#username', 'patient');
  await page.fill('#password', 'kry123');
  await page.click('button[type="submit"]');
  await expect(page.getByRole('heading', { name: /see a doctor/i })).toBeVisible();

  // Start booking
  await page.getByRole('button', { name: /book meeting/i }).click();
  await expect(page.getByText(/question 1 of 5/i)).toBeVisible();

  // Answer all 5 questions
  for (let i = 0; i < 5; i++) {
    await page.locator('label:has(input[type="radio"])').first().click();
    await page.getByRole('button', { name: /next|submit/i }).click();
    if (i < 4) await page.waitForTimeout(300);
  }

  // Results — pick first slot and confirm
  await expect(page.getByRole('heading', { name: /your recommendation/i })).toBeVisible();
  await page.getByRole('option').first().click();
  await page.getByRole('button', { name: /confirm booking/i }).click();

  // Confirmation screen
  await expect(page.getByRole('heading', { name: /booking confirmed/i })).toBeVisible();
  await page.getByRole('button', { name: /return to home/i }).click();

  // Back on landing with booked state
  await expect(page.locator('svg[aria-label="Appointment confirmed"]')).toBeVisible();
  await expect(page.getByText(/your appointment is booked/i)).toBeVisible();
  await page.waitForTimeout(2000);
});

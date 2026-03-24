/** Formats an ISO datetime string as a human-readable date + time. */
export function formatDateTime(
  iso: string,
  options: Intl.DateTimeFormatOptions = {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }
): string {
  return new Date(iso).toLocaleString(undefined, options);
}

/** Returns a human-readable wait time until the given ISO datetime. */
export function formatWaitTime(iso: string): string {
  const diffMs = new Date(iso).getTime() - Date.now();
  if (diffMs <= 0) return 'shortly';
  const mins = Math.floor(diffMs / 60_000);
  if (mins < 60) return `${mins} min${mins !== 1 ? 's' : ''}`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours} hr${hours !== 1 ? 's' : ''}`;
  const days = Math.floor(hours / 24);
  return `${days} day${days !== 1 ? 's' : ''}`;
}

/**
 * Formats the time portion of an ISO datetime string (e.g. "2026-03-24T09:15:00")
 * as "09:15" without constructing a Date object, avoiding timezone-shift bugs.
 */
export function formatTime(iso: string): string {
  const timePart = iso.split('T')[1];
  const [hStr, mStr] = timePart.split(':');
  return `${hStr.padStart(2, '0')}:${mStr}`;
}

/** Formats just the date portion of an ISO datetime for day-tab labels. */
export function formatDayLabel(dateStr: string): string {
  const [y, m, d] = dateStr.split('-').map(Number);
  const date = new Date(y, m - 1, d);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const diff = Math.round((date.getTime() - today.getTime()) / 86_400_000);
  if (diff === 0) return 'Today';
  if (diff === 1) return 'Tomorrow';
  return date.toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric' });
}

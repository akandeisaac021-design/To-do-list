// A <input type="datetime-local"> gives "2026-09-30T18:00" (no seconds).
// The backend's LocalDateTime deserializes "yyyy-MM-ddTHH:mm:ss" - Jackson
// will actually accept the shorter form too, but this keeps the shape
// explicit rather than relying on that leniency.
export function toLocalDateTimeString(value) {
  return value.length === 16 ? `${value}:00` : value;
}

export function formatDeadline(deadline) {
  return new Date(deadline).toLocaleString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
}

// The backend doesn't send an "expired" flag - CreateTaskResponse only has
// id/title/deadline/createdAt/status - so this is computed client-side from
// the same two fields the backend itself would use for that logic.
export function isExpired(task) {
  return task.status !== 'COMPLETED' && new Date(task.deadline).getTime() < Date.now();
}

import { useState } from 'react';
import { toLocalDateTimeString } from '../dateUtils.js';

export default function QuickAddForm({ onAdd }) {
  const [title, setTitle] = useState('');
  const [deadline, setDeadline] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    if (!title.trim() || !deadline) return;

    setSubmitting(true);
    try {
      await onAdd({ title: title.trim(), deadline: toLocalDateTimeString(deadline) });
      setTitle('');
      setDeadline('');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form className="quick-add" onSubmit={handleSubmit}>
      <input
        type="text"
        placeholder="What needs doing?"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        aria-label="Task title"
      />
      <input
        type="datetime-local"
        value={deadline}
        onChange={(e) => setDeadline(e.target.value)}
        aria-label="Deadline"
        required
      />
      <button type="submit" disabled={submitting}>
        Add
      </button>
    </form>
  );
}

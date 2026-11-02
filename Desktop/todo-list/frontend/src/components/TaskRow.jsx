import { formatDeadline, isExpired } from '../dateUtils.js';

export default function TaskRow({ task, onComplete, onDelete }) {
  const expired = isExpired(task);
  const rowClass = ['task-row', expired ? 'expired' : ''].filter(Boolean).join(' ');

  return (
    <li className={rowClass}>
      <button
        type="button"
        className="task-checkbox"
        onClick={() => onComplete(task.id)}
        aria-label={`Mark "${task.title}" as done`}
      />
      <span className="task-title">{task.title}</span>
      <span className="task-deadline">{formatDeadline(task.deadline)}</span>
      <button
        type="button"
        className="task-delete"
        onClick={() => onDelete(task.id)}
        aria-label={`Delete "${task.title}"`}
      >
        ✕
      </button>
    </li>
  );
}

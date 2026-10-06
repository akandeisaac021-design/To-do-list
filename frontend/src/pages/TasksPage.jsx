import { useCallback, useEffect, useState } from 'react';
import { taskApi } from '../api.js';
import { useAuth } from '../context/AuthContext.jsx';
import QuickAddForm from '../components/QuickAddForm.jsx';
import TaskRow from '../components/TaskRow.jsx';

export default function TasksPage() {
  const { user, logout } = useAuth();
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadTasks = useCallback(async () => {
    try {
      const data = await taskApi.list();
      setTasks(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadTasks();
  }, [loadTasks]);

  async function handleAdd(newTask) {
    const created = await taskApi.create(newTask);
    // Backend already sorts by deadline; simplest correct approach for a
    // single insert is to re-sort client-side rather than guess position.
    setTasks((current) => [...current, created].sort((a, b) => a.deadline.localeCompare(b.deadline)));
  }

  async function handleComplete(id) {
    setTasks((current) => current.filter((task) => task.id !== id));
    try {
      await taskApi.complete(id);
    } catch (err) {
      setError(err.message);
      loadTasks(); // resync on failure - the optimistic removal above may be wrong
    }
  }

  async function handleDelete(id) {
    setTasks((current) => current.filter((task) => task.id !== id));
    try {
      await taskApi.remove(id);
    } catch (err) {
      setError(err.message);
      loadTasks();
    }
  }

  return (
    <div className="app-shell">
      <div className="top-bar">
        <h1>Your list</h1>
        <div className="top-bar-right">
          <span className="session-user">{user?.username}</span>
          <button className="logout-link" onClick={logout}>
            Log out
          </button>
        </div>
      </div>

      <QuickAddForm onAdd={handleAdd} />

      {error && (
        <div className="form-error" role="alert">
          {error}
        </div>
      )}

      {!loading && tasks.length === 0 && (
        <div className="empty-state">
          <p>Nothing on the list. Add something above.</p>
        </div>
      )}

      <ul className="task-list">
        {tasks.map((task) => (
          <TaskRow key={task.id} task={task} onComplete={handleComplete} onDelete={handleDelete} />
        ))}
      </ul>
    </div>
  );
}

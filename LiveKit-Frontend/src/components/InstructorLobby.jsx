import { useState } from 'react';
import { startSession } from '../api/liveApi';

/**
 * Instructor lobby — enter course details and start a live class.
 * On success, passes token + roomName up to parent via onSessionStarted.
 */
export default function InstructorLobby({ onSessionStarted }) {
  const [courseId, setCourseId] = useState('physics-101');
  const [instructorId, setInstructorId] = useState(() => 'instructor-' + Math.random().toString(36).substring(2, 8));
  const [instructorName, setInstructorName] = useState('Prof. Sharma');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function handleStart() {
    setLoading(true);
    setError('');
    try {
      const data = await startSession(courseId, instructorId, instructorName);
      // Extract roomName from token payload (base64 decode middle part)
      const payload = JSON.parse(atob(data.token.split('.')[1]));
      const roomName = payload.video?.room;
      onSessionStarted({ token: data.token, serverUrl: data.serverUrl, roomName });
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h2 style={styles.title}>🎓 Start Live Class</h2>

        <label style={styles.label}>Course ID</label>
        <input style={styles.input} value={courseId} onChange={e => setCourseId(e.target.value)} />

        <label style={styles.label}>Instructor ID</label>
        <input style={styles.input} value={instructorId} onChange={e => setInstructorId(e.target.value)} />

        <label style={styles.label}>Instructor Name</label>
        <input style={styles.input} value={instructorName} onChange={e => setInstructorName(e.target.value)} />

        {error && <p style={styles.error}>{error}</p>}

        <button style={styles.button} onClick={handleStart} disabled={loading}>
          {loading ? 'Starting...' : 'Start Class'}
        </button>
      </div>
    </div>
  );
}

const styles = {
  container: { minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#1a1a2e' },
  card: { background: '#16213e', padding: '40px', borderRadius: '12px', width: '360px', display: 'flex', flexDirection: 'column', gap: '12px' },
  title: { color: 'white', margin: 0, marginBottom: '8px' },
  label: { color: '#aaa', fontSize: '13px' },
  input: { padding: '10px', borderRadius: '8px', border: '1px solid #333', background: '#0f3460', color: 'white', fontSize: '14px' },
  button: { marginTop: '8px', padding: '12px', background: '#e94560', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: 'bold', fontSize: '15px' },
  error: { color: '#e74c3c', fontSize: '13px', margin: 0 },
};

import { useState } from 'react';
import { joinSession } from '../api/liveApi';

/**
 * Student lobby — enter room details and join an active live class.
 * On success, passes token + serverUrl up to parent via onJoined.
 */
export default function StudentLobby({ onJoined }) {
  const [roomName, setRoomName] = useState('');
  // Generate unique ID per session — prevents LiveKit identity conflict
  // when same user opens multiple tabs or two students join simultaneously
  const [studentId, setStudentId] = useState(() => 'student-' + Math.random().toString(36).substring(2, 8));
  const [studentName, setStudentName] = useState('Rahul');
  const [courseId, setCourseId] = useState('physics-101');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function handleJoin() {
    setLoading(true);
    setError('');
    try {
      const data = await joinSession(roomName, studentId, studentName, courseId);
      onJoined({ token: data.token, serverUrl: data.serverUrl, roomName });
    } catch (e) {
      setError('Class not started yet or room not found.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h2 style={styles.title}>📚 Join Live Class</h2>

        <label style={styles.label}>Room Name (from instructor)</label>
        <input style={styles.input} value={roomName} onChange={e => setRoomName(e.target.value)} placeholder="physics-101-xxxxxxxx" />

        <label style={styles.label}>Course ID</label>
        <input style={styles.input} value={courseId} onChange={e => setCourseId(e.target.value)} />

        <label style={styles.label}>Student ID</label>
        <input style={styles.input} value={studentId} onChange={e => setStudentId(e.target.value)} />

        <label style={styles.label}>Your Name</label>
        <input style={styles.input} value={studentName} onChange={e => setStudentName(e.target.value)} />

        {error && <p style={styles.error}>{error}</p>}

        <button style={styles.button} onClick={handleJoin} disabled={loading || !roomName}>
          {loading ? 'Joining...' : 'Join Class'}
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
  button: { marginTop: '8px', padding: '12px', background: '#0f9b58', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: 'bold', fontSize: '15px' },
  error: { color: '#e74c3c', fontSize: '13px', margin: 0 },
};

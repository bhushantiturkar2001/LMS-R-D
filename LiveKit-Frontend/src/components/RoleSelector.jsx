/**
 * Landing screen — choose between Instructor or Student role.
 */
export default function RoleSelector({ onSelect }) {
  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>🎥 LMS Live</h1>
        <p style={styles.subtitle}>Select your role to continue</p>

        <button style={{ ...styles.button, background: '#e94560' }} onClick={() => onSelect('instructor')}>
          🎓 I am an Instructor
        </button>

        <button style={{ ...styles.button, background: '#0f9b58' }} onClick={() => onSelect('student')}>
          📚 I am a Student
        </button>
      </div>
    </div>
  );
}

const styles = {
  container: { minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#1a1a2e' },
  card: { background: '#16213e', padding: '48px', borderRadius: '16px', width: '360px', display: 'flex', flexDirection: 'column', gap: '16px', alignItems: 'center' },
  title: { color: 'white', margin: 0 },
  subtitle: { color: '#aaa', margin: 0, fontSize: '14px' },
  button: { width: '100%', padding: '14px', color: 'white', border: 'none', borderRadius: '10px', cursor: 'pointer', fontWeight: 'bold', fontSize: '15px' },
};

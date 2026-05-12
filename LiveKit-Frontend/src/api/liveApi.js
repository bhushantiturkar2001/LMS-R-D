const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

/**
 * Instructor starts a live session.
 * Calls POST /api/live/start and returns token + serverUrl.
 */
export async function startSession(courseId, instructorId, instructorName) {
  const res = await fetch(`${BACKEND_URL}/api/live/start`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ courseId, instructorId, instructorName }),
  });
  if (!res.ok) throw new Error('Failed to start session');
  return res.json(); // { token, serverUrl }
}

/**
 * Student joins an active live session.
 * Calls POST /api/live/join and returns token + serverUrl.
 */
export async function joinSession(roomName, studentId, studentName, courseId) {
  const res = await fetch(`${BACKEND_URL}/api/live/join`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ roomName, studentId, studentName, courseId }),
  });
  if (!res.ok) throw new Error('Session not active or not found');
  return res.json(); // { token, serverUrl }
}

/**
 * Instructor ends the live session.
 * Calls POST /api/live/end with roomName as query param.
 */
export async function endSession(roomName) {
  const res = await fetch(`${BACKEND_URL}/api/live/end?roomName=${roomName}`, {
    method: 'POST',
  });
  if (!res.ok) throw new Error('Failed to end session');
}

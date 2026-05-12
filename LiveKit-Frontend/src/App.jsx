import { useState } from 'react';
import { LiveKitRoom } from '@livekit/components-react';
import '@livekit/components-styles';

import RoleSelector from './components/RoleSelector';
import InstructorLobby from './components/InstructorLobby';
import StudentLobby from './components/StudentLobby';
import VideoConference from './components/VideoConference';
import { endSession } from './api/liveApi';

/**
 * Root app component — manages navigation between screens.
 *
 * Screens:
 *  1. role     → choose instructor or student
 *  2. lobby    → enter details and get token from backend
 *  3. room     → live video conference via LiveKit
 */
export default function App() {
  const [screen, setScreen] = useState('role');   // 'role' | 'lobby' | 'room'
  const [role, setRole] = useState(null);          // 'instructor' | 'student'
  const [session, setSession] = useState(null);    // { token, serverUrl, roomName }

  function handleRoleSelect(selectedRole) {
    setRole(selectedRole);
    setScreen('lobby');
  }

  function handleSessionReady(sessionData) {
    setSession(sessionData);
    setScreen('room');
  }

  async function handleLeave() {
    // Instructor ends the room — students get disconnected automatically
    if (role === 'instructor' && session?.roomName) {
      try {
        await endSession(session.roomName);
      } catch (e) {
        console.warn('End session failed:', e.message);
      }
    }
    setSession(null);
    setScreen('role');
  }

  // Screen 1 — Role selection
  if (screen === 'role') {
    return <RoleSelector onSelect={handleRoleSelect} />;
  }

  // Screen 2 — Lobby (instructor or student)
  if (screen === 'lobby') {
    return role === 'instructor'
      ? <InstructorLobby onSessionStarted={handleSessionReady} />
      : <StudentLobby onJoined={handleSessionReady} />;
  }

  // Screen 3 — Live room with camera, mic, screen share
  return (
    <LiveKitRoom
      serverUrl={session.serverUrl}
      token={session.token}
      connect={true}
      video={true}
      audio={true}
      onDisconnected={handleLeave}
    >
      <VideoConference onLeave={handleLeave} />
    </LiveKitRoom>
  );
}

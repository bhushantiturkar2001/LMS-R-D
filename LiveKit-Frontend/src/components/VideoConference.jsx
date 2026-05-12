import {
  GridLayout,
  ParticipantTile,
  RoomAudioRenderer,
  ControlBar,
  useTracks,
  useRoomContext,
} from '@livekit/components-react';
import { Track } from 'livekit-client';
import '@livekit/components-styles';

/**
 * Main video conference layout.
 * Shows all participant video tiles in a grid.
 * ControlBar handles mic, camera, screen share, leave buttons.
 */
export default function VideoConference({ onLeave }) {
  const room = useRoomContext();

  const tracks = useTracks(
    [
      { source: Track.Source.Camera, withPlaceholder: true },
      { source: Track.Source.ScreenShare, withPlaceholder: false },
    ],
    { onlySubscribed: false }
  );

  async function handleLeave() {
    await room.disconnect();
    onLeave();
  }

  return (
    <div style={{ height: '100vh', display: 'flex', flexDirection: 'column', background: '#1a1a2e' }}>

      {/* Participant grid */}
      <div style={{ flex: 1, overflow: 'hidden' }}>
        <GridLayout tracks={tracks} style={{ height: '100%' }}>
          <ParticipantTile />
        </GridLayout>
      </div>

      {/* Audio renderer — plays all remote audio tracks */}
      <RoomAudioRenderer />

      {/* Bottom control bar — mic, camera, screen share, leave */}
      <div style={{ padding: '12px', background: '#16213e', display: 'flex', justifyContent: 'center', gap: '12px', alignItems: 'center' }}>
        <ControlBar variation="minimal" />
        <button
          onClick={handleLeave}
          style={{
            background: '#e74c3c',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            padding: '10px 20px',
            cursor: 'pointer',
            fontWeight: 'bold',
          }}
        >
          Leave
        </button>
      </div>
    </div>
  );
}

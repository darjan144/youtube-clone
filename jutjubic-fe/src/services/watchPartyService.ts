import api from './api';
import type { WatchPartyRoom } from '../types/WatchParty';

export const watchPartyService = {
  createRoom: async (): Promise<WatchPartyRoom> => {
    const response = await api.post('/watchparty/create');
    return response.data;
  },

  joinRoom: async (roomId: string): Promise<WatchPartyRoom> => {
    const response = await api.post(`/watchparty/join/${roomId}`);
    return response.data;
  },

  getRoom: async (roomId: string): Promise<WatchPartyRoom> => {
    const response = await api.get(`/watchparty/${roomId}`);
    return response.data;
  },

  leaveRoom: async (roomId: string): Promise<void> => {
    await api.post(`/watchparty/leave/${roomId}`);
  },
};

export default watchPartyService;

import type { User } from './User';

export interface WatchPartyRoom {
  roomId: string;
  owner: User;
  members: User[];
}

export interface WatchPartyPlay {
  roomId: string;
  videoId: number;
}

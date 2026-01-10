import type { User } from './User';

export interface Video {
  id: number;
  title: string;
  description: string;
  thumbnailPath: string;
  videoPath: string;
  videoSizeMb: number;
  viewCount: number;
  uploader: User;
  createdAt: string;  // ISO date string
}

export interface VideoListResponse {
  videos: Video[];
  totalCount: number;
}
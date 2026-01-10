import type { User } from './User';

export interface Comment {
  id: number;
  text: string;
  author: User;
  videoId: number;
  createdAt: string;  // ISO date string
}

export interface CommentListResponse {
  comments: Comment[];
  totalCount: number;
}

export interface CreateCommentRequest {
  text: string;
  videoId: number;
}
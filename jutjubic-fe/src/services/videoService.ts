import api from './api';
import type { Video } from '../types/Video';
import type { Comment } from '../types/Comment';

export const videoService = {
  // Get all videos (for homepage - 3.1)
  getAllVideos: async (): Promise<Video[]> => {
    const response = await api.get('/videos');
    return response.data;
  },

  // Get single video by ID
  getVideoById: async (id: number): Promise<Video> => {
    const response = await api.get(`/videos/${id}`);
    return response.data;
  },

  // Get comments for a video
  getVideoComments: async (videoId: number): Promise<Comment[]> => {
    const response = await api.get(`/videos/${videoId}/comments`);
    return response.data;
  },

  // Increment view count
  incrementViewCount: async (videoId: number): Promise<void> => {
    await api.post(`/videos/${videoId}/view`);
  },

  // Create a comment (requires authentication)
  createComment: async (videoId: number, text: string): Promise<Comment> => {
    const response = await api.post(`/videos/${videoId}/comments`, { text });
    return response.data;
  },
};

export default videoService;
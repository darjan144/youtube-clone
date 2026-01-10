import api from './api';
import type { Video } from '../types/Video';
import type { Comment } from '../types/Comment';

export const videoService = {
  // Get all videos (for homepage - 3.1)
  getAllVideos: async (): Promise<Video[]> => {
    const response = await api.get('/videos', {
      params: { page: 0, size: 50 }
    });
    return response.data.content || [];
  },

  // Get single video by ID
  getVideoById: async (id: number): Promise<Video> => {
    const response = await api.get(`/videos/${id}`);
    return response.data;
  },

  // Get comments for a video (returns paginated response)
  getVideoComments: async (videoId: number, page: number = 0, size: number = 50): Promise<Comment[]> => {
    const response = await api.get(`/videos/${videoId}/comments`, {
      params: { page, size }
    });
    // Backend returns Page object with content array
    return response.data.content || [];
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
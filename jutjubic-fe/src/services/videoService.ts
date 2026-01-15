import api from './api';
import type { Video } from '../types/Video';
import type { Comment } from '../types/Comment';
import type { AxiosProgressEvent } from 'axios';

export interface VideoUploadData {
  title: string;
  description?: string;
  tags?: string[];
  location?: string;
}

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

  // Upload video with files (NEW for 3.3) - WITH EXTENSIVE LOGGING
  uploadVideo: async (
    data: VideoUploadData,
    videoFile: File,
    thumbnailFile: File,
    onProgress?: (progress: number) => void
  ): Promise<Video> => {
    console.log('=== VIDEO UPLOAD STARTED ===');
    console.log('Upload data:', data);
    console.log('Video file:', videoFile);
    console.log('Video file name:', videoFile?.name);
    console.log('Video file size:', videoFile?.size, 'bytes');
    console.log('Video file type:', videoFile?.type);
    console.log('Thumbnail file:', thumbnailFile);
    console.log('Thumbnail file name:', thumbnailFile?.name);
    console.log('Thumbnail file size:', thumbnailFile?.size, 'bytes');
    console.log('Thumbnail file type:', thumbnailFile?.type);
    console.log('onProgress callback provided?', !!onProgress);

    try {
      console.log('Creating FormData...');
      const formData = new FormData();

      console.log('Appending video file...');
      formData.append('video', videoFile);
      console.log('Video file appended!');

      console.log('Appending thumbnail file...');
      formData.append('thumbnail', thumbnailFile);
      console.log('Thumbnail file appended!');

      console.log('Appending title:', data.title);
      formData.append('title', data.title);

      if (data.description) {
        console.log('Appending description:', data.description);
        formData.append('description', data.description);
      } else {
        console.log('No description provided');
      }

      if (data.tags && data.tags.length > 0) {
        console.log('Appending tags:', data.tags);
        data.tags.forEach((tag: string, index: number) => {
          console.log(`Appending tag ${index + 1}:`, tag);
          formData.append('tags', tag);
        });
      } else {
        console.log('No tags provided');
      }

      if (data.location) {
        console.log('Appending location:', data.location);
        formData.append('location', data.location);
      } else {
        console.log('No location provided');
      }

      console.log('FormData created successfully!');
      console.log('FormData contents:');
      for (const [key, value] of formData.entries()) {
        if (value instanceof File) {
          console.log(`  ${key}: [File] ${value.name} (${value.size} bytes)`);
        } else {
          console.log(`  ${key}:`, value);
        }
      }

      console.log('Checking if FormData is instanceof FormData:', formData instanceof FormData);
      
      console.log('Getting JWT token...');
      const token = localStorage.getItem('token');
      console.log('Token exists?', !!token);
      if (token) {
        console.log('Token preview:', token.substring(0, 20) + '...');
      }

      console.log('Preparing axios POST request...');
      console.log('URL: /videos/upload');
      console.log('Method: POST');
      console.log('Body: FormData');

      console.log('🚀 CALLING api.post NOW...');
      
      const response = await api.post('/videos/upload', formData, {
        onUploadProgress: (progressEvent: AxiosProgressEvent) => {
          if (progressEvent.total && onProgress) {
            const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
            console.log(`📤 Upload progress: ${percentCompleted}% (${progressEvent.loaded}/${progressEvent.total} bytes)`);
            onProgress(percentCompleted);
          }
        },
      });

      console.log('✅ Upload request completed!');
      console.log('Response status:', response.status);
      console.log('Response data:', response.data);

      return response.data;

    } catch (error: any) {
      console.error('❌ UPLOAD ERROR!');
      console.error('Error object:', error);
      console.error('Error message:', error.message);
      console.error('Error response:', error.response);
      console.error('Error response status:', error.response?.status);
      console.error('Error response data:', error.response?.data);
      console.error('Error config:', error.config);
      
      // Re-throw the error so it can be caught by the component
      throw error;
    }
  },
};

export default videoService;
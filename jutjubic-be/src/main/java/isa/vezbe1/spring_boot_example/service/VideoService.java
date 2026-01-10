package isa.vezbe1.spring_boot_example.service;

import isa.vezbe1.spring_boot_example.dto.CreateVideoDTO;
import isa.vezbe1.spring_boot_example.dto.VideoDTO;
import isa.vezbe1.spring_boot_example.model.User;
import isa.vezbe1.spring_boot_example.model.Video;
import isa.vezbe1.spring_boot_example.model.VideoTag;
import isa.vezbe1.spring_boot_example.repository.VideoRepository;
import isa.vezbe1.spring_boot_example.repository.VideoTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VideoService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private VideoTagRepository videoTagRepository;

    @Transactional(readOnly = true)
    public List<VideoDTO> getAllVideos() {
        List<Video> videos = videoRepository.findAllByOrderByCreatedAtDesc();
        return videos.stream()
                .map(VideoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<VideoDTO> getAllVideos(Pageable pageable) {
        Page<Video> videos = videoRepository.findAllByOrderByCreatedAtDesc(pageable);
        return videos.map(VideoDTO::new);
    }

    @Transactional(readOnly = true)
    public VideoDTO getVideoById(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found with id: " + id));
        return new VideoDTO(video);
    }

    @Transactional
    public VideoDTO createVideo(CreateVideoDTO createVideoDTO, User uploader) {

        if (createVideoDTO.getVideoSizeMb() > 200) {
            throw new RuntimeException("Video size cannot exceed 200MB");
        }

        Video video = new Video();
        video.setTitle(createVideoDTO.getTitle());
        video.setDescription(createVideoDTO.getDescription());
        video.setThumbnailPath(createVideoDTO.getThumbnailPath());
        video.setVideoPath(createVideoDTO.getVideoPath());
        video.setVideoSizeMb(createVideoDTO.getVideoSizeMb());
        video.setUploader(uploader);
        video.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        Set<VideoTag> tags = new HashSet<>();
        if (createVideoDTO.getTags() != null && !createVideoDTO.getTags().isEmpty()) {
            for (String tagName : createVideoDTO.getTags()) {
                VideoTag tag = videoTagRepository.findByNameIgnoreCase(tagName)
                        .orElseGet(() -> {
                            VideoTag newTag = new VideoTag(tagName);
                            return videoTagRepository.save(newTag);
                        });
                tags.add(tag);
            }
        }
        video.setTags(tags);

        Video savedVideo = videoRepository.save(video);

        return new VideoDTO(savedVideo);
    }

    @Transactional
    public void incrementViewCount(Long videoId) {
        videoRepository.incrementViewCount(videoId);
    }

    @Transactional(readOnly = true)
    public List<VideoDTO> getVideosByUploader(User uploader) {
        List<Video> videos = videoRepository.findByUploaderOrderByCreatedAtDesc(uploader);
        return videos.stream()
                .map(VideoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VideoDTO> searchVideosByTitle(String title) {
        List<Video> videos = videoRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(title);
        return videos.stream()
                .map(VideoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteVideo(Long videoId, User user) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        if (!video.getUploader().getId().equals(user.getId()) &&
                !user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("You don't have permission to delete this video");
        }

        videoRepository.delete(video);
    }
}
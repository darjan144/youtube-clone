package isa.vezbe1.spring_boot_example.repository;

import isa.vezbe1.spring_boot_example.model.Video;
import isa.vezbe1.spring_boot_example.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    Page<Video> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Video> findAllByOrderByCreatedAtDesc();

    List<Video> findByUploaderOrderByCreatedAtDesc(User uploader);

    @Modifying
    @Query("UPDATE Video v SET v.viewCount = v.viewCount + 1 WHERE v.id = :videoId")
    void incrementViewCount(@Param("videoId") Long videoId);

    List<Video> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title);

    Long countByUploader(User uploader);
}
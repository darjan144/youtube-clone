package isa.vezbe1.spring_boot_example.repository;

import isa.vezbe1.spring_boot_example.model.Comment;
import isa.vezbe1.spring_boot_example.model.Video;
import isa.vezbe1.spring_boot_example.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByVideoOrderByCreatedAtDesc(Video video);

    Page<Comment> findByVideoOrderByCreatedAtDesc(Video video, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.author = :author AND c.createdAt > :since")
    Long countByAuthorSince(@Param("author") User author, @Param("since") Timestamp since);

    Long countByVideo(Video video);

    List<Comment> findByAuthorOrderByCreatedAtDesc(User author);
}
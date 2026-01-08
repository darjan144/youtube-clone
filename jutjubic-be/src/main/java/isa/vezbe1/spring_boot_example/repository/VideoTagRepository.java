package isa.vezbe1.spring_boot_example.repository;

import isa.vezbe1.spring_boot_example.model.VideoTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface VideoTagRepository extends JpaRepository<VideoTag, Long> {

    Optional<VideoTag> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<VideoTag> findByNameInIgnoreCase(List<String> names);
}
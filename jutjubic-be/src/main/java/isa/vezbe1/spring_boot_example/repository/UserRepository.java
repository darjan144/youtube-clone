package isa.vezbe1.spring_boot_example.repository;

import isa.vezbe1.spring_boot_example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}

package isa.vezbe1.spring_boot_example.repository;

import isa.vezbe1.spring_boot_example.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByName(String name);
}

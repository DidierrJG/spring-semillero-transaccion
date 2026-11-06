package com.example.demo.repository;

import com.example.demo.entity.Director;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DirectorRepository extends JpaRepository<Director, Long> {
    Optional<Director> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
}

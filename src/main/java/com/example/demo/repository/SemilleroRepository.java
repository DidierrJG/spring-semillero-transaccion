package com.example.demo.repository;

import com.example.demo.entity.Semillero;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SemilleroRepository extends JpaRepository<Semillero, Long> {
    Optional<Semillero> findBySigla(String sigla);
    boolean existsBySigla(String sigla);
}

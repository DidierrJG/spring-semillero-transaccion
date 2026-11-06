package com.example.demo.repository;

import com.example.demo.entity.Linea;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LineaRepository extends JpaRepository<Linea, Long> {
    Optional<Linea> findByNombreIgnoreCase(String nombre);
    Optional<Linea> findByNombre(String nombre);
}

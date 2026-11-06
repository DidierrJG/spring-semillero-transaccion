package com.example.demo.repository;

import com.example.demo.entity.Integrante;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntegranteRepository extends JpaRepository<Integrante, Long> {
    List<Integrante> findBySemilleroId(Long semilleroId);
    boolean existsBySemilleroIdAndCodigo(Long semilleroId, String codigo);
}

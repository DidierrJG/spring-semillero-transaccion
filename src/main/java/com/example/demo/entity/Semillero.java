package com.example.demo.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "semilleros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Semillero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String nombre;

    @Column(nullable = false, unique = true, length = 30)
    private String sigla;

    @Column(name = "grupo_investigacion", nullable = false, length = 180)
    private String grupoInvestigacion;

    @Column(columnDefinition = "TEXT")
    private String mision;

    @Column(columnDefinition = "TEXT")
    private String vision;

    @Column(name = "objetivo_general", columnDefinition = "TEXT")
    private String objetivoGeneral;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "director_id", nullable = false)
    private Director director;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "semillero_lineas",
        joinColumns = @JoinColumn(name = "semillero_id"),
        inverseJoinColumns = @JoinColumn(name = "linea_id")
    )
    @Builder.Default
    private Set<Linea> lineas = new HashSet<>();

    @OneToMany(mappedBy = "semillero", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Integrante> integrantes = new ArrayList<>();

    public void addIntegrante(Integrante integrante) {
        integrantes.add(integrante);
        integrante.setSemillero(this);
    }
}

package com.futureprograms.MyIkea.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "municipios")
public class Municipality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_municipio")
    private Integer municipalityId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_provincia", referencedColumnName = "id_provincia", nullable = false)
    private Province province;

    @Column(nullable = false, name = "cod_municipio")
    private Integer municipalityCode;

    @Column(nullable = false, name = "DC")
    private Integer dc;

    @Column(nullable = false, length = 100, name = "nombre")
    private String name;
}

package com.futureprograms.MyIkea.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.futureprograms.MyIkea.Models.Province;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Integer> {
}

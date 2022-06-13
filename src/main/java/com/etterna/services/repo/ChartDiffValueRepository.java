package com.etterna.services.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.etterna.services.datamodel.ChartDiffValue;
import com.etterna.services.datamodel.pk.ChartDiffValuePk;

@Repository
public interface ChartDiffValueRepository extends JpaRepository<ChartDiffValue, ChartDiffValuePk> {

}

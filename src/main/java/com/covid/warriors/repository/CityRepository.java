package com.covid.warriors.repository;

import com.covid.warriors.entity.model.CityEntity;
import com.covid.warriors.entity.model.ContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<CityEntity, Long> {

	List<CityEntity> findAll();

}

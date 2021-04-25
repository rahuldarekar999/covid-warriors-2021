package com.covid.warriors.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.covid.warriors.entity.model.ContactEntity;

@Repository
public interface ContactRepository extends JpaRepository<ContactEntity, Long> {

	List<ContactEntity> findByCityAndCategory(String city, String category);

}

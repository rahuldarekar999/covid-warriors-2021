package com.covid.warriors.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.covid.warriors.entity.model.ContactEntity;

@Repository
public interface ContactRepository extends JpaRepository<ContactEntity, Long> {

	List<ContactEntity> findByCityAndCategory(String city, String category);
	
	List<String> findMobileNumberByCityAndCategory(String city, String category);

	ContactEntity findByMobileNumberAndCityAndCategory(String chatIdMobileNumber, String city, String category);

	@Query(value="select distinct(c.city) as city from ContactEntity c")
	List<String> findAllDistinctCity();

}

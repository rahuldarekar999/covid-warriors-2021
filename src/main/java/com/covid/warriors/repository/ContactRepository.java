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

	@Query(value="select distinct(c.city) as city from ContactEntity c where c.city is not null order by city")
	List<String> findAllDistinctCity();

	List<ContactEntity> findByCityAndCategoryAndValid(String city, String category, boolean b);

	List<ContactEntity> findByCityAndValid(String city, boolean b);

	List<ContactEntity> findByValid(boolean b);

	ContactEntity findByMobileNumberAndCityAndCategoryAndValid(String chatIdMobileNumber, String city, String category,
			boolean valid);

	int countByCityAndCategoryAndValid(String city, String category, boolean b);

	//@Query(value="select c from ContactEntity c where c.city = :city and c.category = :category and c.valid = :b limit 250")
	List<ContactEntity> findTop250ByCityAndCategoryAndValidOrderByLastMessageReceivedTimeDesc(String city, String category, boolean b);
	
	@Query(value="select c.mobileNumber as mobile from ContactEntity c where c.city = :city and c.category = :category and c.valid = :b")
	List<String> findTop250MobileByCityAndCategoryAndValidOrderByLastMessageReceivedTimeDesc(String city, String category, boolean b);

	List<String> findMobileByCityAndCategoryAndValidOrderByLastMessageReceivedTimeDesc(String city, String category,
			boolean b);

}

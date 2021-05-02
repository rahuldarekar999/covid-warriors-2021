package com.covid.warriors.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.covid.warriors.entity.model.CategoryMessage;

@Repository
public interface CategoryMessageRepository extends JpaRepository<CategoryMessage, Long> {

	CategoryMessage findByCategory(String category);

	@Query(value="select distinct(c.category) as category from CategoryMessage c")
	List<String> findAllDistinctCategory();

	@Query(value="select message from CategoryMessage c where c.category=:category")
	String findMessageByCategory(@Param("category") String category);

}

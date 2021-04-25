package com.covid.warriors.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.covid.warriors.entity.model.CategoryMessage;

@Repository
public interface CategoryMessageRepository extends JpaRepository<CategoryMessage, Long> {

	CategoryMessage findByCategory(String category);

}

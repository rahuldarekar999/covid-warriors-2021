package com.covid.warriors.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.covid.warriors.entity.model.UrlEntity;

@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, Long> {

}

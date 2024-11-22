package com.covid.warriors.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.covid.warriors.entity.model.SentMessageMetadataEntity;

@Repository
public interface SentMessageMetadataRepository extends JpaRepository<SentMessageMetadataEntity, Long> {


	@Query(value = "select smm from SentMessageMetadataEntity smm where smm.isForward = 1 and smm.sentOn is null")
	List<SentMessageMetadataEntity> findAllWhereSentOnIsNotNullAndForwardIsTrue();

	@Query(value = "select smm from SentMessageMetadataEntity smm where smm.isForward = 1")
	List<SentMessageMetadataEntity> findAllWhereForwardIsTrue();

	SentMessageMetadataEntity findByFrom(String from);

	SentMessageMetadataEntity findByFromAndCityAndCategory(String from, String city, String category);

	@Query(value = "select smm.from as mobile from SentMessageMetadataEntity smm where smm.subscribed = 1 and "
			+ " smm.city = :city and smm.category = :category")
	List<String> findOnlyMobileByCityAndCategory(String city, String category);
}

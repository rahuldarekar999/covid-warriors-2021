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

}

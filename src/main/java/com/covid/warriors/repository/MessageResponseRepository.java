package com.covid.warriors.repository;

import com.covid.warriors.entity.model.FeedbackEntity;
import com.covid.warriors.entity.model.MessageResponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageResponseRepository extends JpaRepository<MessageResponseEntity, Long> {

}

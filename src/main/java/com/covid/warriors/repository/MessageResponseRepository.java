package com.covid.warriors.repository;

import com.covid.warriors.entity.model.FeedbackEntity;
import com.covid.warriors.entity.model.MessageResponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageResponseRepository extends JpaRepository<MessageResponseEntity, Long> {
    List<MessageResponseEntity> findByCreatedAtAfterAndMessageAndCityAndCategory(LocalDateTime createdAt,
                                                                                 String message, String city,
                                                                                 String category);
}

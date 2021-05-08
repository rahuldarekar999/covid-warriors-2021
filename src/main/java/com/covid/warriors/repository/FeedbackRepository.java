package com.covid.warriors.repository;

import com.covid.warriors.entity.model.CityEntity;
import com.covid.warriors.entity.model.FeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {

}

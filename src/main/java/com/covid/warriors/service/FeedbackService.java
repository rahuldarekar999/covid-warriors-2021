package com.covid.warriors.service;

import com.covid.warriors.entity.model.FeedbackEntity;
import com.covid.warriors.repository.FeedbackRepository;
import com.covid.warriors.request.model.FeedbackRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class FeedbackService {
    @Autowired
    private FeedbackRepository feedbackRepository;

    public String saveFeedback(FeedbackRequest feedbackRequest) {
        try {
            if(Objects.nonNull(feedbackRequest)) {
                feedbackRepository.save(getFeedbackEntity(feedbackRequest));
                System.out.println("FEEDBACK SAVED");
                return "success";
            }
        } catch (Exception e)  {
            e.printStackTrace();
        }
        return "failed";
    }

    private FeedbackEntity getFeedbackEntity(FeedbackRequest feedbackRequest) {
        FeedbackEntity feedbackEntity = new FeedbackEntity();
        feedbackEntity.setName(feedbackRequest.getName());
        feedbackEntity.setMessage(feedbackRequest.getMessage());
        feedbackEntity.setCreatedDate(LocalDateTime.now());
        return feedbackEntity;
    }
}

package com.covid.warriors.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.covid.warriors.response.model.instagram.InstagramScraperResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class InstagramScraperService {

    private static final String instagramUrl = "https://instagram47.p.rapidapi.com/hashtag_post?hashtag=%s";

    private static final String rapidApiKey = "8396e3720cmsh83d0c4ae187994dp1797dejsn5be9e80c2006";

    private static final String rapidApiHost = "instagram47.p.rapidapi.com";

    public Set<String> getContactsFromInstagram(String city, String category) {
        try {
        	List<String> hashtags = new ArrayList<>();
    		
    		hashtags.add(city.concat(category));
    		hashtags.add(category.concat("in").concat(city));
    		hashtags.add(city.concat(category).concat("supplier"));
    		hashtags.add(category.concat("supplierin").concat(city));
    		Set<String> set = new HashSet<>();
    		hashtags.forEach(hashtag -> {
    			 InstagramScraperResponse scraperResponse = getPostsFromInstagram(hashtag);
    	            if(Objects.nonNull(scraperResponse) && Objects.nonNull(scraperResponse.getBody())
    	                    && Objects.nonNull(scraperResponse.getBody().getEdgeHashtagToMedia())
    	                    && Objects.nonNull(scraperResponse.getBody().getEdgeHashtagToMedia().getEdges())) {
    	                extractPhoneNumber(scraperResponse, set);
    	            }
    		});
    		return set;
            /*InstagramScraperResponse scraperResponse = getPostsFromInstagram(city, category);
            if(Objects.nonNull(scraperResponse) && Objects.nonNull(scraperResponse.getBody())
                    && Objects.nonNull(scraperResponse.getBody().getEdgeHashtagToMedia())
                    && Objects.nonNull(scraperResponse.getBody().getEdgeHashtagToMedia().getEdges())) {
                Set<String> set = new HashSet<>();
                extractPhoneNumber(scraperResponse, set);
                return set;
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptySet();
    }

    public InstagramScraperResponse getPostsFromInstagram(String hashtag) {
    	try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(String.format(instagramUrl, hashtag))
                    .get()
                    .addHeader("x-rapidapi-key", rapidApiKey)
                    .addHeader("x-rapidapi-host", rapidApiHost)
                    .build();

            Response res = client.newCall(request).execute();
            return new ObjectMapper().readValue(res.body().string(), InstagramScraperResponse.class);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void extractPhoneNumber(InstagramScraperResponse ocrResponse, Set<String> phoneNumbers) {
        if(!ocrResponse.getBody().getEdgeHashtagToMedia().getEdges().isEmpty()) {
            ocrResponse.getBody().getEdgeHashtagToMedia().getEdges().forEach(edge -> {
                Iterator<PhoneNumberMatch> existsPhone= PhoneNumberUtil.getInstance().findNumbers(edge.getNode().getAccessibilityCaption(),
                        "IN").iterator();
                while (existsPhone.hasNext()){
                    String number = String.valueOf(existsPhone.next().number().getNationalNumber());
                    if(StringUtils.isNotBlank(number) && number.length() == 10) {
                        phoneNumbers.add("91".concat(number));
                    }

                }
            });
        }
    //    return Collections.emptySet();
    }
    
    public static void main(String[] args) {
		String city="CHENNAI".toLowerCase();
		String category="OXYGEN".toLowerCase();
		List<String> hashtags = new ArrayList<>();
		
		hashtags.add(city.concat(category));
		hashtags.add(category.concat("in").concat(city));
		hashtags.add(city.concat(category).concat("supplier"));
		hashtags.add(category.concat("supplierin").concat(city));
		
		System.out.println(hashtags);
	}

}

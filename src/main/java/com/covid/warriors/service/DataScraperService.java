package com.covid.warriors.service;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.util.UriEncoder;

import com.covid.warriors.request.model.TwitterMetadataResponse;
import com.covid.warriors.utils.ContactUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class DataScraperService {

    private static final String baseApiUrl = "https://api.proxycrawl.com/?token=l_Uy3iQMarvqv4nhx9GfCQ&format=json&url=";

   /* private static final String twitterUrl = "https%3A%2F%2Ftwitter.com%2Fsearch%3Fq%3Dverified%_city_%20(_category_)%20-%22not%20" +
            "verified%22%20-%22un%20verified%22%20-%22urgent%22%20-%22unverified%22%20-%22needed%22%20-%22" +
            "required%22%20-%22need%22%20-%22needs%22%20-%22requirement%22%20-%22Any%20verified%20lead%22%20" +
            "since%3A2021-5-7%26f%3Dlive";**/
    
    @Value("${proxycrawl.api}")
	private String proxyCrawlApi;	
    
    @Value("${proxycrawl.token}")
	private String proxyCrawlToken;	
    
    @Value("${twitter.url}")
	private String twitterUrl;	
    
    @Value("${twitter.query}")
	private String twitterQuery;	

    
    @Value("#{${twitter.delta.days.map}}")  
    private Map<String,Integer> twitterDeltaDaysMap;
    
    @Value("${twitter.delta.default.days}")
   	private int twitterDefaultDaysDelta;	
    
    public Set<String> scrapeDataFromTwitterUrl(String city, String category, String searchText) {
        try {
            HttpClient httpclient = HttpClients.createDefault();
            String url = proxyCrawlApi + "?token=" + proxyCrawlToken + "&format=json&url=";
            System.out.println("Final URL -> " + url);
            URIBuilder builder = new URIBuilder(url.concat(UriEncoder.encode(getTwitterUrlToScrap(city, category, searchText))));
            URI uri = builder.build();
            HttpGet request = new HttpGet(uri);
            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);
            
        //    System.out.println("---------------------- \n " + result + "\n------------------");
            Iterator<PhoneNumberMatch> existsPhone= PhoneNumberUtil.getInstance().findNumbers(result,
                    "IN").iterator();
            Set<String> phoneNumbers = new HashSet<>();
            while (existsPhone.hasNext()){
                String contact = ContactUtil.validateContact(String.valueOf(existsPhone.next().number().getNationalNumber()));
                if(StringUtils.isNotBlank(contact)) {
                    phoneNumbers.add("91".concat(contact));
                }
            }
            System.out.println(phoneNumbers);
            return phoneNumbers;
        } catch (Exception e ) {
            e.printStackTrace();
        }
        return Collections.emptySet();
    }
    
    public TwitterMetadataResponse scrapeDataFromTwitterUrlMetadata(String city, String category, String searchText) {
    	TwitterMetadataResponse twitterMetadataResponse = new TwitterMetadataResponse();
        try {
        	
            HttpClient httpclient = HttpClients.createDefault();
            String url = proxyCrawlApi + "?token=" + proxyCrawlToken + "&format=json&url=";
            System.out.println("Final URL -> " + url);
            URIBuilder builder = new URIBuilder(url.concat(UriEncoder.encode(getTwitterUrlToScrap(city, category, searchText))));
            URI uri = builder.build();
            HttpGet request = new HttpGet(uri);
            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode jsonNode = objectMapper.readTree(result);
            if(jsonNode.has("body")) {
            	twitterMetadataResponse.setHtml(jsonNode.get("body").asText());
            }
        //    System.out.println("---------------------- \n " + result + "\n------------------");
            Iterator<PhoneNumberMatch> existsPhone= PhoneNumberUtil.getInstance().findNumbers(result,
                    "IN").iterator();
            Set<String> phoneNumbers = new HashSet<>();
            while (existsPhone.hasNext()){
                String contact = ContactUtil.validateContact(String.valueOf(existsPhone.next().number().getNationalNumber()));
                if(StringUtils.isNotBlank(contact)) {
                    phoneNumbers.add("91".concat(contact));
                }
            }
            System.out.println(phoneNumbers);
            twitterMetadataResponse.setMobileList(phoneNumbers);
            
        } catch (Exception e ) {
            e.printStackTrace();
        }
        return twitterMetadataResponse;
    }

    private String getTwitterUrlToScrap(String city, String category, String searchText) {
    	LocalDate date = LocalDate.now();
    	if(twitterDeltaDaysMap.get(category) != null) {
    		date = date.minusDays(twitterDeltaDaysMap.get(category));
    	} else {
    		date = date.minusDays(twitterDefaultDaysDelta);
    	}
    	String dateStr = date.getYear() + "-" + date.getMonthValue() + "-" + date.getDayOfMonth();
    	String url = twitterUrl + twitterQuery.replace("!city!", city).replace("!category!" , searchText).replace("!date!", dateStr);
    	System.out.println("twitter url -> " + url);
    	return url;
    }
    
    public Map<String, String> buildLoginCookieString() {

		String csrf_cookie_name = null;
		String ci_session = null;
		CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);
		List<HttpCookie> cookieList = cookieManager.getCookieStore().getCookies();
		Map<String, String> cookieMap= new HashMap<>();
		for (HttpCookie cookie : cookieList) {
			String cookieName = cookie.getName();
			String cookieValue = cookie.getValue();
			if ("csrf_cookie_name".equals(cookieName)) {
				cookieMap.put("csrf_cookie_name", cookieValue);
				csrf_cookie_name = cookieName + "=" + cookieValue + "; ";
			} else if ("ci_session".equals(cookieName)) {
				cookieMap.put("ci_session", cookieValue);
				ci_session = cookieName + "=" + cookieValue;
			}
		}

		String cookie = csrf_cookie_name + ci_session;

		return cookieMap;
	}
    public static void main(String[] args) throws IOException {
    	
	}

}

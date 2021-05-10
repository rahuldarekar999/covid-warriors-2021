package com.covid.warriors.service;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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

import com.covid.warriors.utils.ContactUtil;
import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

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

    public Set<String> scrapeDataFromTwitterUrl(String city, String category) {
        try {
            HttpClient httpclient = HttpClients.createDefault();
            String url = proxyCrawlApi + "?token=" + proxyCrawlToken + "&format=json&url=";
            System.out.println("Final URL -> " + url);
            URIBuilder builder = new URIBuilder(url.concat(UriEncoder.encode(getTwitterUrlToScrap(city, category))));
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

    private String getTwitterUrlToScrap(String city, String category) {
    	LocalDate date = LocalDate.now();
		date = date.minusDays(3);
    	String dateStr = date.getYear() + "-" + date.getMonthValue() + "-" + date.getDayOfMonth();
    	String url = twitterUrl + twitterQuery.replace("!city!", city).replace("!category!" , category).replace("!date!", dateStr);
    	System.out.println("twitter url -> " + url);
    	return url;
    }
}

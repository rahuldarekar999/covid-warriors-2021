package com.covid.warriors.service.impl;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;

import com.covid.warriors.entity.model.UrlEntity;
import com.covid.warriors.repository.UrlRepository;
import com.covid.warriors.utils.BaseConversion;

@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final BaseConversion conversion;

    public UrlService(UrlRepository urlRepository, BaseConversion baseConversion) {
        this.urlRepository = urlRepository;
        this.conversion = baseConversion;
    }

    public String convertToShortUrl(String urlStr) {
        UrlEntity url = new UrlEntity();
        url.setLongUrl(urlStr);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 6);
        url.setExpiresDate(cal.getTime());
        url.setCreatedDate(new Date());
        UrlEntity entity = urlRepository.save(url);

        return conversion.encode(entity.getId());
    }

    public String getOriginalUrl(String shortUrl) {
        long id = conversion.decode(shortUrl);
        UrlEntity entity = urlRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no entity with " + shortUrl));

        if (entity.getExpiresDate() != null && entity.getExpiresDate().before(new Date())){
            urlRepository.delete(entity);
            throw new EntityNotFoundException("Link expired!");
        }

        return entity.getLongUrl();
    }
    
    public String getOriginalUrlWithoutDelete(String shortUrl) {
        long id = conversion.decode(shortUrl);
        UrlEntity entity = urlRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no entity with " + shortUrl));
        return entity.getLongUrl();
    }
    
    public static void main(String[] args) {
    	String url = "a=n&m=919700258333&c=MEDICINE&ct=HYDERABAD";
    	BaseConversion consersion1 = new BaseConversion();
    	System.out.println(consersion1.encode(3696));
	}
}

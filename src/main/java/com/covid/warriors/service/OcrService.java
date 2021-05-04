package com.covid.warriors.service;

import com.covid.warriors.response.model.ocr.OcrResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.*;

@Service
public class OcrService {
    private String key = "18c3e55fcc4c498591ebb2dbddf7f55f";

    private ObjectMapper objectMapper = new ObjectMapper();

    public String uploadImaageForProcessing(MultipartFile file) {
        try
        {
            HttpClient httpclient = HttpClients.createDefault();
            URIBuilder builder = new URIBuilder("https://covidwarriors.cognitiveservices.azure.com/vision/v3.0/read/analyze");
            builder.setParameter("language", "en");
            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/octet-stream");
            request.setHeader("Ocp-Apim-Subscription-Key", "18c3e55fcc4c498591ebb2dbddf7f55f");
            request.setEntity(new InputStreamEntity(file.getInputStream()));

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() == 202)
            {
                return response.getHeaders("Operation-Location")[0].getValue();
            }
            return "";
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return "";
    }

    public Set<String> getOcrParsedContents(String location) {
        try
        {
            String status = "running";
            OcrResponse ocrResponse = null;
            while("running".equals(status)) {
                HttpClient httpclient = HttpClients.createDefault();
                URIBuilder builder = new URIBuilder(location);
                URI uri = builder.build();
                HttpGet request = new HttpGet(uri);
                request.setHeader("Ocp-Apim-Subscription-Key", "18c3e55fcc4c498591ebb2dbddf7f55f");
                HttpResponse response = httpclient.execute(request);
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                ocrResponse = objectMapper.readValue(result, OcrResponse.class);
                status = ocrResponse.getStatus();
            }
            if("succeeded".equals(ocrResponse.getStatus())) {
                Set<String> phoneNumbers = new HashSet<>();
                extractPhoneNumber(ocrResponse, phoneNumbers);
                return phoneNumbers;
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return Collections.emptySet();
    }

    private Set<String> extractPhoneNumber(OcrResponse ocrResponse, Set<String> phoneNumbers) {
        if(Objects.nonNull(ocrResponse) &&
                !ocrResponse.getAnalyzeResult().getReadResults().get(0).getLines().isEmpty()) {
            ocrResponse.getAnalyzeResult().getReadResults().get(0).getLines().forEach(line -> {
                Iterator<PhoneNumberMatch> existsPhone= PhoneNumberUtil.getInstance().findNumbers(line.getText(),
                        "IN").iterator();
                while (existsPhone.hasNext()){
                    phoneNumbers.add(String.valueOf(existsPhone.next().number().getNationalNumber()));
                }
            });
        }
        return Collections.emptySet();
    }



}

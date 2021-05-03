package com.covid.warriors.service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@Service
public class OcrService {
    private String key = "18c3e55fcc4c498591ebb2dbddf7f55f";

    public void uploadImaageForProcessing(MultipartFile file) {
        HttpClient httpclient = HttpClients.createDefault();

        try
        {
            URIBuilder builder = new URIBuilder("https://covidwarriors.cognitiveservices.azure.com/vision/v3.0/read/analyze");
            builder.setParameter("language", "en");
            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", "18c3e55fcc4c498591ebb2dbddf7f55f");
            StringEntity reqEntity = new StringEntity(new String(file.getBytes()));
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null)
            {
                System.out.println(EntityUtils.toString(entity));
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}

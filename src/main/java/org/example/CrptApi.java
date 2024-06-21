package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    private final TimeUnit timeUnit;
    private final int requestLimit;
    private int requestCount = 0;
    private long lastRequestTime = System.currentTimeMillis();

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
    }

    public synchronized void createDocument(Object document, String signature) throws IOException {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastRequestTime;

        if (elapsedTime < timeUnit.toMillis(1)) {
            if (requestCount >= requestLimit) {
                try {
                    Thread.sleep(timeUnit.toMillis(1) - elapsedTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        String url = "https://ismp.crpt.ru/api/v3/lk/documents/create";
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer().with(SerializationFeature.WRAP_ROOT_VALUE);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json");

        String json = writer.writeValueAsString(document);
        StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);

        CloseableHttpResponse response = httpClient.execute(httpPost);

        lastRequestTime = System.currentTimeMillis();
        requestCount++;
    }

}
package xao.develop.toolbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;

public class LibreTranslateService {
    private static final String API_URL = "https://libretranslate.com/translate";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String translate(String text, String targetLang, String sourceLang) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(API_URL);

            Map<String, String> requestBody = new HashMap<>();

            requestBody.put("q", text);
            requestBody.put("source", sourceLang);
            requestBody.put("target", targetLang);
            requestBody.put("format", "text");

            StringEntity entity = new StringEntity(objectMapper.writeValueAsString(requestBody));

            post.setEntity(entity);

            post.setHeader("Content-Type", "application/json");

            try(CloseableHttpResponse response = httpClient.execute(post)) {
                String responseBody = EntityUtils.toString(response.getEntity());

                Map<String, Object> jsonResponse = objectMapper.readValue(responseBody, HashMap.class);

                return (String) jsonResponse.get("translatedText");
            }
        }
    }
}

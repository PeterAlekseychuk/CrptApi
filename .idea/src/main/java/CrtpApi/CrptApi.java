package fd;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CrptApi {

    private final long REQUEST_INTERVAL_MILLIS;
    private long lastRequestTime;
    private final String API_URL;
    private final int REQUEST_LIMIT;
    private int requestCount;

    public CrptApi(TimeUnit timeUnit, int REQUEST_LIMIT, String API_URL) throws IOException {
        this.REQUEST_INTERVAL_MILLIS = timeUnit.toMillis(1);
        this.lastRequestTime = System.currentTimeMillis();
        this.API_URL = API_URL;
        this.REQUEST_LIMIT = REQUEST_LIMIT;
        this.requestCount = 0;
    }

    public void createDocument(Document document, String signature) {

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRequestTime >= REQUEST_INTERVAL_MILLIS) {
            // Сбрасываем счетчик запросов
            requestCount = 0;
            lastRequestTime = currentTime;
        }

        if (requestCount < REQUEST_LIMIT) {
            try {
                // Проще было бы использовать RestTemplate, однако из тз не понятно можно ли использовать Spring
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // тело запроса
                ObjectMapper objectMapper = new ObjectMapper();
                String requestBody = objectMapper.writeValueAsString(document);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                System.out.println("HTTP status: " + responseCode);
                requestCount++;

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Превышен лимит запросов.");
        }
    }


    @Getter
    @Setter
    public static class Document {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private Boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_date;
        private String production_type;
        private List<Product> products;
        private String reg_date;
        private String reg_number;

        @Getter
        @Setter
        public static class Description {
            private String participantInn;

        }

        @Getter
        @Setter
        public static class Product {
            private String certificate_document;
            private String certificate_document_date;
            private String certificate_document_number;
            private String owner_inn;
            private String producer_inn;
            private String production_date;
            private String tnved_code;
            private String uit_code;
            private String uitu_code;

        }
    }


    public static void main(String[] args) throws IOException {

        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 10, "https://ismp.crpt.ru/api/v3/lk/documents/create");
        Document documentJson = new Document();
        String signature = "some_signature_here";
        crptApi.createDocument(documentJson, signature);
    }
}

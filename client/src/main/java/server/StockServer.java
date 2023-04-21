package server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.Company;
import dto.PurchaseResult;
import dto.SellResult;
import rx.Observable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class StockServer {

    private final URI stocksServerURI = URI.create("http://localhost:8080/");
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Observable<Company> getCompany(int id) {
        URI requestURI = stocksServerURI.resolve("getCompany?id=" + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(requestURI)
                .header("Accept", "application/json")
                .build();
        CompletableFuture<HttpResponse<String>> future = client.sendAsync(request,
                HttpResponse.BodyHandlers.ofString());
        return Observable.from(future).map(HttpResponse::body)
                .flatMap(body -> {
                    try {
                        return Observable.just(objectMapper.readValue(body, Company.class));
                    } catch (JsonProcessingException e) {
                        return Observable.error(e);
                    }
                });
    }

    public Observable<PurchaseResult> buyStocks(int companyId, int amount, double moneyAvailable) {
        URI requestURI = stocksServerURI.resolve("buyStocks?id=" + companyId + "&amount=" + amount +
                "&moneyAvailable=" + moneyAvailable);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(requestURI)
                .header("Accept", "application/json")
                .build();
        CompletableFuture<HttpResponse<String>> future = client.sendAsync(request,
                HttpResponse.BodyHandlers.ofString());
        return Observable.from(future).map(HttpResponse::body)
                .flatMap(body -> {
                    try {
                        return Observable.just(objectMapper.readValue(body, PurchaseResult.class));
                    } catch (JsonProcessingException e) {
                        return Observable.error(e);
                    }
                });
    }

    public Observable<SellResult> sellStocks(int companyId, int amount) {
        URI requestURI = stocksServerURI.resolve("sellStocks?id=" + companyId + "&amount=" + amount);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(requestURI)
                .header("Accept", "application/json")
                .build();
        CompletableFuture<HttpResponse<String>> future = client.sendAsync(request,
                HttpResponse.BodyHandlers.ofString());
        return Observable.from(future).map(HttpResponse::body)
                .flatMap(body -> {
                    try {
                        return Observable.just(objectMapper.readValue(body, SellResult.class));
                    } catch (JsonProcessingException e) {
                        return Observable.error(e);
                    }
                });
    }

}

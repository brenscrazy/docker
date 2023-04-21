package server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.rx.client.MongoClient;
import com.mongodb.rx.client.MongoClients;
import com.mongodb.rx.client.MongoDatabase;
import dto.Company;
import dao.InMemoryStockDao;
import dao.StockDao;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import rx.Observable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static utils.Utils.*;

public class NettyServer {

    private final StockDao dao;
    private final ObjectMapper mapper = new ObjectMapper();

    public NettyServer() {
        dao = new InMemoryStockDao();
    }

    public <T> Observable<String> handleRequest(HttpServerRequest<T> request) {
        String requestName = request.getDecodedPath().substring(1);
        Map<String, String> queries = request.getQueryParameters().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().get(0)));
        try {
            switch (requestName) {
                case "addCompany":
                    return dao.addCompany(Company.validateQueriesAndGet(queries));
                case "deleteCompany":
                    validateMapContainsFields(List.of("id"), queries);
                    return dao.deleteCompany(validateInt(queries.get("id")));
                case "addStocks":
                    validateMapContainsFields(List.of("id", "amount"), queries);
                    int id = validateInt(queries.get("id"));
                    int amount = validateInt(queries.get("amount"));
                    return dao.addStocks(id, amount);
                case "getCompany":
                    validateMapContainsFields(List.of("id"), queries);
                    return dao.getCompany(validateInt(queries.get("id")))
                            .flatMap(company -> {
                                try {
                                    return Observable.just(mapper.writeValueAsString(company));
                                } catch (JsonProcessingException e) {
                                    return Observable.error(e);
                                }
                            })
                            .defaultIfEmpty("No company found");
                case "buyStocks":
                    validateMapContainsFields(List.of("id", "amount", "moneyAvailable"), queries);
                    id = validateInt(queries.get("id"));
                    amount = validateInt(queries.get("amount"));
                    double moneyAvailable = validateCost(queries.get("moneyAvailable"));
                    return dao.buyStocks(id, amount, moneyAvailable)
                            .flatMap(company -> {
                                try {
                                    return Observable.just(mapper.writeValueAsString(company));
                                } catch (JsonProcessingException e) {
                                    return Observable.error(e);
                                }
                            });
                case "setStockPrice":
                    validateMapContainsFields(List.of("id", "stockPrice"), queries);
                    id = validateInt(queries.get("id"));
                    double stockPrice = validateCost(queries.get("stockPrice"));
                    return dao.setStockPrice(id, stockPrice);
                case "sellStocks":
                    validateMapContainsFields(List.of("id", "amount"), queries);
                    id = validateInt(queries.get("id"));
                    amount = validateInt(queries.get("amount"));
                    return dao.sellStocks(id, amount)
                            .flatMap(company -> {
                                try {
                                    return Observable.just(mapper.writeValueAsString(company));
                                } catch (JsonProcessingException e) {
                                    return Observable.error(e);
                                }
                            });
                default:
                    return Observable.error(new IllegalArgumentException("Unknown command: " + requestName));
            }
        } catch (IllegalArgumentException e) {
            return Observable.error(e);
        }
    }

}

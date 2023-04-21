package server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.rx.client.MongoClient;
import com.mongodb.rx.client.MongoClients;
import com.mongodb.rx.client.MongoDatabase;
import dto.Company;
import dao.MongoClientDao;
import dao.ClientDao;
import dto.User;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import rx.Observable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static utils.Utils.*;

public class NettyServer {

    private final ClientDao dao;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NettyServer() {
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = client.getDatabase("market");
        dao = new MongoClientDao(
                database.getCollection("users"),
                database.getCollection("usersStocks"));
    }

    public <T> Observable<String> handleRequest(HttpServerRequest<T> request) {
        String requestName = request.getDecodedPath().substring(1);
        Map<String, String> queries = request.getQueryParameters().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().get(0)));
        try {
            switch (requestName) {
                case "addUser":
                    return dao.addUser(User.validateQueriesAndGet(queries));
                case "removeUser":
                    validateMapContainsFields(List.of("userId"), queries);
                    return dao.removeUser(validateInt(queries.get("userId")));
                case "addMoney":
                    validateMapContainsFields(List.of("userId", "money"), queries);
                    int id = validateInt(queries.get("userId"));
                    int money = validateInt(queries.get("money"));
                    return dao.addMoney(id, money);
                case "getStocks":
                    validateMapContainsFields(List.of("userId"), queries);
                    return dao.getStocks(validateInt(queries.get("userId")))
                            .toList()
                            .flatMap(this::toJson);
                case "buyStocks":
                    validateMapContainsFields(List.of("userId", "companyId", "amount"), queries);
                    int userId = validateInt(queries.get("userId"));
                    int companyId = validateInt(queries.get("companyId"));
                    int amount = validateInt(queries.get("amount"));
                    return dao.buyStocks(userId, companyId, amount).flatMap(this::toJson);
                case "getWholeMoney":
                    validateMapContainsFields(List.of("userId"), queries);
                    userId = validateInt(queries.get("userId"));
                    return dao.getWholeMoney(userId).map(Objects::toString);
                case "sellStocks":
                    validateMapContainsFields(List.of("userId", "companyId", "amount"), queries);
                    userId = validateInt(queries.get("userId"));
                    companyId = validateInt(queries.get("companyId"));
                    amount = validateInt(queries.get("amount"));
                    return dao.sellStocks(userId, companyId, amount).flatMap(this::toJson);
                default:
                    return Observable.error(new IllegalArgumentException("Unknown command: " + requestName));
            }
        } catch (IllegalArgumentException e) {
            return Observable.error(e);
        }
    }

    private <T> Observable<String> toJson(T object) {
        try {
            return Observable.just(objectMapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            return Observable.error(e);
        }
    }

}

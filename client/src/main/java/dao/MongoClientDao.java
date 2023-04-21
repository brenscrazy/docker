package dao;

import com.mongodb.rx.client.MongoCollection;
import com.mongodb.rx.client.Success;
import dto.PurchaseResult;
import dto.SellResult;
import dto.User;
import dto.UserStocks;
import org.bson.Document;
import rx.Observable;
import server.StockServer;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.inc;

public class MongoClientDao implements ClientDao {

    private final MongoCollection<Document> users;
    private final MongoCollection<Document> usersStocks;
    private final StockServer stockServer = new StockServer();

    public MongoClientDao(MongoCollection<Document> users, MongoCollection<Document> usersStocks) {
        this.users = users;
        this.usersStocks = usersStocks;
    }


    @Override
    public Observable<String> addUser(User user) {
        return users.find(eq("id", user.getId()))
                .toObservable()
                .defaultIfEmpty(null)
                .flatMap(doc -> {
                    if (doc == null) {
                        Document document = user.toDocument();
                        return users.insertOne(document).asObservable().map(res -> "Inserted");
                    } else {
                        return Observable.error(new IllegalArgumentException("Id is taken"));
                    }
                });
    }

    @Override
    public Observable<String> removeUser(int userId) {
        return users.deleteOne(eq("id", userId))
                .map(res -> "Removed");
    }

    @Override
    public Observable<String> addMoney(int userId, int money) {
        return users.updateOne(eq("id", userId), inc("money", money))
                .map(res -> {
                    if (res.getModifiedCount() == 0) {
                        return "No user with given id is found";
                    } else {
                        return "Updated";
                    }
                });
    }

    @Override
    public Observable<UserStocks> getStocks(int userId) {
        return usersStocks.find(eq("userId", userId))
                .toObservable()
                .flatMap(doc -> {
                    int companyId = doc.getInteger("companyId");
                    int amount = doc.getInteger("amount");
                    return stockServer.getCompany(companyId).map(company ->
                            new UserStocks(userId, companyId, amount, company.getStockPrice()));
                });
    }

    @Override
    public Observable<Double> getWholeMoney(int userId) {
        return getStocks(userId)
                .map(userStocks -> userStocks.getStocksBought() * userStocks.getPrice())
                .reduce(Double::sum);
    }

    @Override
    public Observable<PurchaseResult> buyStocks(int userId, int companyId, int amount) {
        return users.find(eq("id", userId))
                .toObservable()
                .flatMap(document -> stockServer.buyStocks(companyId, amount, document.getDouble("money")))
                .flatMap(purchaseResult -> {
                    if (purchaseResult.isHasBought()) {
                        return usersStocks.updateOne(and(eq("userId", userId), eq("companyId", companyId)),
                                combine(inc("amount", amount)))
                                .flatMap(updateResult -> {
                                    if (updateResult.getModifiedCount() == 0) {
                                        return usersStocks.insertOne(new Document()
                                                .append("userId", userId)
                                                .append("companyId", companyId)
                                                .append("amount", amount));
                                    }
                                    return Observable.just(Success.SUCCESS);
                                })
                                .flatMap(success -> users.findOneAndUpdate(eq("id", userId),
                                        inc("money", -purchaseResult.getSpent())))
                                .map(doc -> purchaseResult);
                    } else {
                        return Observable.just(purchaseResult);
                    }
                });
    }

    @Override
    public Observable<SellResult> sellStocks(int userId, int companyId, int amount) {
        return usersStocks.findOneAndUpdate(and(eq("userId", userId), eq("companyId", companyId),
                        gte("amount", amount)), inc("amount", -amount))
                .flatMap(document -> stockServer.sellStocks(companyId, amount)
                        .flatMap(sellResult -> users.findOneAndUpdate(eq("id", userId),
                                inc("money", sellResult.getEarned()))
                                .map(doc -> sellResult)))
                .defaultIfEmpty(new SellResult(0));
    }

    @Override
    public Observable<String> deleteUserStocks(int userId, int companyId) {
        return usersStocks.deleteMany(and(eq("userId", userId), eq("companyId", companyId)))
                .map(res -> "Removed");
    }
}

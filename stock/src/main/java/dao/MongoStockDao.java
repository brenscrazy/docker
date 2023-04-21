package dao;

import com.mongodb.rx.client.MongoCollection;
import dto.Company;
import dto.PurchaseResult;
import dto.SellResult;
import org.bson.Document;
import rx.Observable;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

public class MongoStockDao implements StockDao {

    private final MongoCollection<Document> stocks;

    public MongoStockDao(MongoCollection<Document> stocks) {
        this.stocks = stocks;
    }

    @Override
    public Observable<String> addCompany(Company company) {
        return stocks.find(eq("id", company.getId()))
                .toObservable()
                .defaultIfEmpty(null)
                .flatMap(doc -> {
                    if (doc == null) {
                        Document document = company.toDocument();
                        return stocks.insertOne(document).asObservable().map(res -> "Inserted");
                    } else {
                        return Observable.error(new IllegalArgumentException("Id is taken"));
                    }
                });
    }

    @Override
    public Observable<String> addStocks(int companyId, int amount) {
        return stocks.updateOne(eq("id", companyId), inc("amount", amount))
                .map(res -> {
                    if (res.getModifiedCount() == 0) {
                        return "No company with given id is found";
                    } else {
                        return "Updated";
                    }
                });
    }

    @Override
    public Observable<Company> getCompany(int id) {
        return stocks.find(eq("id", id))
                .toObservable()
                .map(Company::new);
    }

    @Override
    public Observable<PurchaseResult> buyStocks(int companyId, int amount, double moneyAvailable) {
        double maxPrice = moneyAvailable / amount;
        return stocks.findOneAndUpdate(and(eq("id", companyId),
                                gte("amount", amount),
                                lte("stockPrice", maxPrice)),
                        inc("amount", -amount))
                .map(res -> new PurchaseResult(true, res.getDouble("stockPrice") * amount))
                .defaultIfEmpty(new PurchaseResult(false, 0));
    }

    @Override
    public Observable<SellResult> sellStocks(int companyId, int amount) {
        return stocks.findOneAndUpdate(eq("id", companyId), inc("amount", amount))
                .map(document -> new SellResult(amount * document.getDouble("stockPrice")));
    }

    @Override
    public Observable<String> setStockPrice(int companyId, double price) {
        if (price < 0) {
            return Observable.error(new IllegalArgumentException("Price have to me positive"));
        }
        return stocks.updateOne(eq("id", companyId), set("stockPrice", price))
                .map(res -> {
                    if (res.getModifiedCount() == 0) {
                        return "No company with given id is found";
                    } else {
                        return "Updated";
                    }
                });
    }
}

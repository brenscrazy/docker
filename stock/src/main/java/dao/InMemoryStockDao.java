package dao;

import com.mongodb.rx.client.Success;
import dto.Company;
import dto.PurchaseResult;
import dto.SellResult;
import rx.Observable;
import rx.exceptions.OnCompletedFailedException;

import java.util.HashMap;
import java.util.Map;

public class InMemoryStockDao implements StockDao {

    private final Map<Integer, Company> stocks = new HashMap<>();

    @Override
    public Observable<String> addCompany(Company company) {
        if (!stocks.containsKey(company.getId())) {
            stocks.put(company.getId(), company);
            return Observable.just("Inserted");
        } else {
            return Observable.error(new IllegalArgumentException("Id is taken"));
        }
    }

    @Override
    public Observable<String> deleteCompany(int companyId) {
        stocks.remove(companyId);
        return Observable.just("Removed");
    }

    @Override
    public Observable<String> addStocks(int companyId, int amount) {
        if (stocks.containsKey(companyId)) {
            stocks.get(companyId).incAmount(amount);
            return Observable.just("Updated");
        } else {
            return Observable.error(new IllegalArgumentException("No company with given id"));
        }
    }

    @Override
    public Observable<Company> getCompany(int id) {
        return checkId(id).map(success -> stocks.get(id));
    }

    @Override
    public Observable<PurchaseResult> buyStocks(int companyId, int amount, double moneyAvailable) {
        return checkId(companyId)
                .flatMap(success -> {
                    Company company = stocks.get(companyId);
                    double maxPrice = moneyAvailable / amount;
                    if (company.getStockPrice() > maxPrice) {
                        return Observable.just(PurchaseResult.getFail("Not enough money"));
                    }
                    if (company.getAmount() < amount) {
                        return Observable.just(PurchaseResult.getFail("Not enough stocks on market"));
                    }
                    company.incAmount(-amount);
                    return Observable.just(new PurchaseResult(true, amount * company.getStockPrice(), "Success"));
                });

    }

    @Override
    public Observable<SellResult> sellStocks(int companyId, int amount) {
        return checkId(companyId).map(success -> {
            Company company = stocks.get(companyId);
            company.incAmount(amount);
            return new SellResult(amount * company.getStockPrice());
        });
    }

    @Override
    public Observable<String> setStockPrice(int companyId, double price) {
        if (price < 0) {
            return Observable.error(new IllegalArgumentException("Price have to me positive"));
        }
        return checkId(companyId).map(success -> {
            stocks.get(companyId).setStockPrice(price);
            return "Updated";
        });
    }

    private Observable<Success> checkId(int id) {
        if (!stocks.containsKey(id)) {
            return Observable.error(new IllegalArgumentException("No company with given id"));
        }
        return Observable.just(Success.SUCCESS);
    }

}

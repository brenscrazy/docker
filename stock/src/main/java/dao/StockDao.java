package dao;

import dto.Company;
import dto.PurchaseResult;
import dto.SellResult;
import rx.Observable;

public interface StockDao {

    Observable<String> addCompany(Company company);
    Observable<String> deleteCompany(int companyId);
    Observable<String> addStocks(int companyId, int amount);
    Observable<Company> getCompany(int companyId);
    Observable<PurchaseResult> buyStocks(int companyId, int amount, double moneyAvailable);
    Observable<SellResult> sellStocks(int companyId, int amount);
    Observable<String> setStockPrice(int companyId, double price);

}

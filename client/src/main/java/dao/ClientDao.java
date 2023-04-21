package dao;

import dto.PurchaseResult;
import dto.SellResult;
import dto.User;
import dto.UserStocks;
import rx.Observable;

public interface ClientDao {

    Observable<String> addUser(User user);
    Observable<String> removeUser(int userId);
    Observable<String> addMoney(int userId, int amount);
    Observable<UserStocks> getStocks(int userId);
    Observable<Double> getWholeMoney(int userId);
    Observable<PurchaseResult> buyStocks(int userId, int companyId, int amount);
    Observable<SellResult> sellStocks(int userId, int companyId, int amount);
    Observable<String> deleteUserStocks(int userId, int companyId);

}

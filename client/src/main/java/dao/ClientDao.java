package dao;

import dto.User;
import dto.UserStocks;
import rx.Observable;

public interface ClientDao {

    Observable<String> addUser(User user);
    Observable<String> addMoney(int userId, int amount);
    Observable<UserStocks> getStocks(int userId);
    Observable<Double> getWholeMoney(int userId);
    Observable<String> buyStocks(int userId, int companyId, int amount);
    Observable<String> sellStocks(int userId, int companyId, int amount);

}

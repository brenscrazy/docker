package dto;

import org.bson.Document;

import java.util.List;

public class UserStocks {

    private final int userId;
    private final int companyId;
    private final int stocksBought;
    private final double price;


    public UserStocks(int userId, int companyId, int amountBought, double price) {
        this.userId = userId;
        this.companyId = companyId;
        this.stocksBought = amountBought;
        this.price = price;
    }

    public int getStocksBought() {
        return stocksBought;
    }

    public int getCompanyId() {
        return companyId;
    }

    public int getUserId() {
        return userId;
    }

    public double getPrice() {
        return price;
    }

}

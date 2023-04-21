package dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.Document;

import java.util.List;
import java.util.Map;

import static utils.Utils.*;

public class Company {

    private final int id;
    private final String name;
    private final double stockPrice;
    private final int amount;
    private final static List<String> fields = List.of("id", "name", "stockPrice", "amount");

    public Company(Document document) {
        this(document.getInteger("id"), document.getString("name"), document.getDouble("stockPrice"),
                document.getInteger("amount"));
    }

    @JsonCreator
    public Company(@JsonProperty("id") int id,
                   @JsonProperty("name") String name,
                   @JsonProperty("stockPrice") double stockPrice,
                   @JsonProperty("amount") int amount) {
        this.id = id;
        this.name = name;
        this.stockPrice = stockPrice;
        this.amount = amount;
    }

    public static Company validateQueriesAndGet(Map<String, String> queries) {
        validateMapContainsFields(fields, queries);
        int id = validateInt(queries.get("id"));
        String name = queries.get("name");
        double stockPrice = validateDouble(queries.get("stockPrice"));
        int amount = validateInt(queries.get("amount"));
        return new Company(id, name, stockPrice, amount);
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public double getStockPrice() {
        return stockPrice;
    }

    public int getAmount() {
        return amount;
    }

    public Document toDocument() {
        return new Document()
                .append("id", id)
                .append("name", name)
                .append("stockPrice", stockPrice)
                .append("amount", amount);
    }

    public String toString() {
        return "Company{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", stockPrice='" + stockPrice + '\'' +
                ", amount='" + amount + '\'' +
                '}';
    }

}

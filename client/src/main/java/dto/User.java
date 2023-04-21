package dto;

import org.bson.Document;

import java.util.List;
import java.util.Map;

import static utils.Utils.*;

public class User {

    private final int id;
    private final double money;
    private final static List<String> fields = List.of("id", "money");

    public User(int id, double money) {
        this.id = id;
        this.money = money;
    }

    public static User validateQueriesAndGet(Map<String, String> queries) {
        validateMapContainsFields(fields, queries);
        int id = validateInt(queries.get("id"));
        double money = validateDouble(queries.get("money"));
        return new User(id, money);
    }

    public Object getId() {
        return id;
    }

    public double getMoney() {
        return money;
    }

    public Document toDocument() {
        return new Document()
                .append("id", id)
                .append("money", money);
    }

}

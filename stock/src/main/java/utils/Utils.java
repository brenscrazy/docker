package utils;

import java.util.List;
import java.util.Map;

public class Utils {

    public static void validateMapContainsFields(List<String> fields, Map<String, String> queries) {
        for (String field : fields) {
            if (!queries.containsKey(field)) {
                throw new IllegalArgumentException("Missing field: " + field);
            }
        }
    }

    public static int validateInt(String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Illegal id format. Please enter integer number.");
        }
    }

    public static double validateCost(String cost) {
        try {
            return Double.parseDouble(cost);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Illegal cost format. Please enter double number.");
        }
    }

}

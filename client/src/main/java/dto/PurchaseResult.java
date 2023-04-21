package dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PurchaseResult {

    private final boolean hasBought;
    private final double spent;

    @JsonCreator
    public PurchaseResult(@JsonProperty("hasBought") boolean hasBought,
                          @JsonProperty("spent") double spent) {
        this.hasBought = hasBought;
        this.spent = spent;
    }

    public boolean isHasBought() {
        return hasBought;
    }

    public double getSpent() {
        return spent;
    }
}

package dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PurchaseResult {

    private final boolean hasBought;
    private final double spent;
    private final String verdict;

    @JsonCreator
    public PurchaseResult(@JsonProperty("hasBought") boolean hasBought,
                          @JsonProperty("spent") double spent,
                          @JsonProperty("verdict") String verdict) {
        this.hasBought = hasBought;
        this.spent = spent;
        this.verdict = verdict;
    }

    public boolean isHasBought() {
        return hasBought;
    }

    public double getSpent() {
        return spent;
    }

    public String getVerdict() {
        return verdict;
    }
}

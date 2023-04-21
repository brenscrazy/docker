package dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SellResult {

    private final double earned;

    @JsonCreator
    public SellResult(@JsonProperty("earned") double earned) {
        this.earned = earned;
    }

    public double getEarned() {
        return earned;
    }
}

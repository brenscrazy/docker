package dto;

public class PurchaseResult {

    private final boolean hasBought;
    private final double spent;

    public PurchaseResult(boolean hasBought, double spent) {
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

package dto;

public class PurchaseResult {

    private final boolean hasBought;
    private final double spent;
    private final String verdict;

    public PurchaseResult(boolean hasBought, double spent, String verdict) {
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

    public static PurchaseResult getFail(String verdict) {
        return new PurchaseResult(false, 0, verdict);
    }

}

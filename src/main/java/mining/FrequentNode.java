package mining;

/**
 * Representing a single item set
 * @param <T>
 */
class FrequentNode<T> {

    private ItemSet<T> itemSet;
    private int trueCount;
    private int estimateCount;

    FrequentNode(ItemSet<T> itemSet, int trueCount, int estimateCount) {
        this.itemSet = itemSet;
        this.trueCount = trueCount;
        this.estimateCount = estimateCount;
    }

    void incrementTrueCount() {
        this.trueCount += 1;
    }

    ItemSet<T> getItemSet() {
        this.itemSet.setSupport(this.getTrueCount() + this.getEstimateCount());
        return this.itemSet;
    }

    int getTrueCount() {
        return trueCount;
    }

    int getEstimateCount() {
        return estimateCount;
    }

    @Override
    public String toString() {
        return "FrequentNode{" +
                "itemSet=" + itemSet +
                ", trueCount=" + trueCount +
                ", estimateCount=" + estimateCount +
                '}';
    }
}

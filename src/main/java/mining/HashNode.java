package mining;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Used to represent the node in the hash map.
 * @param <T> The type of Items stored in the Item Sets
 */
class HashNode<T> {

    private int totalAccess;
    private int lastAccess;
    private List<FrequentNode<T>> frequentNodes;

    HashNode(int totalAccess, int lastAccess) {
        this.totalAccess = totalAccess;
        this.lastAccess = lastAccess;
        this.frequentNodes = new LinkedList<>();
    }

    void setLastAccess(int lastAccess) {
        this.lastAccess = lastAccess;
    }

    void incrementTotalAccess() {
        this.totalAccess += 1;
    }

    void addFrequentNodes(FrequentNode<T> frequentNodes) {
        this.frequentNodes.add(frequentNodes);
    }

    FrequentNode<T> getNodeForItemSet(ItemSet<T> itemSet) {
        for (FrequentNode<T> frequentNode : this.frequentNodes) {
            if(frequentNode.getItemSet().equals(itemSet)) {
                return frequentNode;
            }
        }

        return null;
    }

    void removeFNodeBelowThreashold(double threashold) {
        for (Iterator<FrequentNode<T>> iterator = this.frequentNodes.iterator(); iterator.hasNext(); ) {
            FrequentNode<T> frequentNode = iterator.next();

            if(frequentNode.getTrueCount() + frequentNode.getEstimateCount() < threashold) {
                System.out.println("Removing node: " + frequentNode);
                iterator.remove();
            }
        }
    }

    int getTotalTrueCount() {
        int totalTrueCount = 0;
        for (FrequentNode<T> frequentNode : this.frequentNodes) {
            totalTrueCount += frequentNode.getTrueCount();
        }
        return  totalTrueCount;
    }

    int getTotalEstimateCount() {
        int totaleEstimateCount = 0;
        for (FrequentNode<T> frequentNode : this.frequentNodes) {
            totaleEstimateCount += frequentNode.getEstimateCount();
        }
        return totaleEstimateCount;
    }

    int getTotalAccess() {
        return totalAccess;
    }

    int getLastAccess() {
        return lastAccess;
    }

    List<FrequentNode<T>> getFrequentNodes() {
        return frequentNodes;
    }

    @Override
    public String toString() {
        return "HashNode{" +
                "totalAccess=" + totalAccess +
                ", lastAccess=" + lastAccess +
                ", frequentNodes=" + frequentNodes +
                '}';
    }
}

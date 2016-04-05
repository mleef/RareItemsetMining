package Structures;

import java.util.Set;

/**
 * Created by marcleef on 4/5/16.
 * Common interface for mining structures.
 */
public interface ItemSetMiner<Type> {

    /**
     * Adds item set to structure
     * @param itemSet ItemSet to insert
     */
    void addItemSet(ItemSet<Type> itemSet);

    /**
     * @param minThreshold Lower bound on item set support
     * @param maxThreshold Upper bound on item set support
     * @return Set of item sets with frequencies within threshold boundaries
     */
    Set<ItemSet<Type>> mine(int minThreshold, int maxThreshold);


}

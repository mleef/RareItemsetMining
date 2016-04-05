package Structures;

import java.util.Set;

/**
 * Created by marcleef on 4/5/16.
 * Encapsulates FP Tree mining logic
 */
public class FPItemSetMiner<Type> implements ItemSetMiner<Type> {

    private FPTree<Type> tree;

    /**
     * Constructor
     */
    public FPItemSetMiner() {
        this.tree = new FPTree<Type>();
    }

    /**
     * Adds new item set for tree construction
     * @param itemSet ItemSet to add to tree
     */
    public void addItemSet(ItemSet<Type> itemSet) {
        this.tree.addItemSet(itemSet);
    }

    /**
     * Mines tree for frequent/infrequent item sets
     * @param minThreshold Lower bound on item set support
     * @param maxThreshold Upper bound on item set support
     * @return Set of item sets with frequencies within threshold boundaries
     */
    public Set<ItemSet<Type>> mine(int minThreshold, int maxThreshold) {
        this.tree.build();

        // Do the mining


        return null;
    }
}

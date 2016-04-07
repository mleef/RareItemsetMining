package mining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Created by marcleef on 4/5/16.
 * Encapsulates FP Tree mining logic
 */
public class FPItemSetMiner<Type> implements ItemSetMiner<Type>, Serializable {

    private int MIN_THRESHOLD;
    private int MAX_THRESHOLD;

    private FPTree<Type> tree;

    /**
     * Constructor
     */
    public FPItemSetMiner() {
        this.tree = new FPTree<>();
    }

    /**
     * Adds new item set for tree construction
     * @param itemSet ItemSet to add to tree
     */
    public void addItemSet(ItemSet<Type> itemSet) {
        System.out.println("Adding: " + itemSet);
        this.tree.addItemSet(itemSet);
    }

    /**
     * Mines tree for frequent/infrequent item sets
     * @return Set of item sets with frequencies within threshold boundaries
     */
    public Set<ItemSet<Type>> mine(int minThreshold, int maxThreshold) {
        this.tree.build();
        this.MIN_THRESHOLD = minThreshold;
        this.MAX_THRESHOLD = maxThreshold;

        Set<ItemSet<Type>> result = new HashSet<>();
        // populate result with item sets within bounds
        mine(new Stack<>(), this.tree, new HashSet<>());
        return result;

    }

    private void mine(Stack<Item<Type>> currentSuffix,
                      FPTree<Type> conditionalTree,  Set<ItemSet<Type>> resultItemSets) {

        for(Item<Type> item : conditionalTree.items()) {
            if(conditionalTree.getSupport(item) > MIN_THRESHOLD) {
                currentSuffix.push(item);
                FPTree<Type> newTree = conditionalTree.buildConditional(item);
            }

        }


    }
}

package Structures;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by marcleef on 3/29/16.
 * Frequent Pattern tree construction and querying operations
 */
public class FPTree<Type> {
    private ArrayList<ItemSet<Type>> itemSets;
    private ArrayList<ArrayList<Item<Type>>> orderedItemSets;
    private final ConcurrentHashMap<Item<Type>, Integer> itemSupports; // To track item frequencies

    /**
     * Constructor
     */
    public FPTree() {
        this.itemSets = new ArrayList<ItemSet<Type>>();
        this.orderedItemSets = new ArrayList<ArrayList<Item<Type>>>();
        this.itemSupports = new ConcurrentHashMap<Item<Type>, Integer>();
    }

    /**
     * Transforms unordered ItemSets into lists sorted by item support
     */
    private void orderBySupport() {
        for(ItemSet<Type> itemSet : itemSets) {
            orderedItemSets.add(itemSet.supportOrder());
        }
    }

    /**
     * Updates tree wide item supports
     * @param itemSet ItemSet to update counts with
     */
    private void updateCounts(ItemSet<Type> itemSet) {
        for(Item<Type> item : itemSet) {
            if(!itemSupports.containsKey(item)) {
                itemSupports.put(item, 0);
            }
            itemSupports.put(item, itemSupports.get(item) + 1);
        }
    }

    /**
     * Adds new item set for tree construction
     * @param itemSet ItemSet to add to tree
     */
    public void addItemSet(ItemSet<Type> itemSet) {
        if(itemSet == null) {
            throw new NullPointerException("ItemSet cannot be null");
        }
        updateCounts(itemSet);
        itemSets.add(itemSet);
    }
}

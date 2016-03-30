package Structures;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by marcleef on 3/29/16.
 * Frequent Pattern tree construction and querying operations
 */
public class FPTree<Type> {
    private final ConcurrentHashMap<Item<Type>, Integer> itemSupports; // To track item frequencies

    public FPTree() {
        this.itemSupports = new ConcurrentHashMap<Item<Type>, Integer>();
    }
}

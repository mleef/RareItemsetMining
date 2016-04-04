package Structures;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by marcleef on 3/29/16.
 * Frequent Pattern tree construction and querying operations
 */
public class FPTree<Type> {
    private FPNode<Type> root;
    private final List<ItemSet<Type>> itemSets;
    private final ConcurrentHashMap<Item<Type>, Integer> itemSupports; // To track item frequencies

    /**
     * Constructor
     */
    public FPTree() {
        this.root = new FPNode<Type>(null, null, null);
        this.itemSets = Collections.synchronizedList(new ArrayList<ItemSet<Type>>());
        this.itemSupports = new ConcurrentHashMap<Item<Type>, Integer>();
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

    /**
     * Constructs FP tree by inserting each stored itemset into the tree
     */
    public void build() {
        synchronized (itemSets) {
            for(ItemSet<Type> itemSet : itemSets) {
                insert(itemSet.supportOrder());
            }
        }
    }

    /**
     * Inserts item sets into the tree
     * @param itemSet ItemSet to be inserted
     */
    public void insert(ArrayList<Item<Type>> itemSet) {
        FPNode<Type> current = root;
        for(Item<Type> item : itemSet) {
            if(current.children.containsKey(item)) {
                current.children.get(item).support++;
                current = current.children.get(item);
            } else {
                FPNode<Type> newNode = new FPNode<Type>(null, item, current);
                current.children.put(item, newNode);
                current = newNode;
            }
        }
    }

    /**
     * Prints the tree in level order
     */
    private void levelOrder() {
        LinkedList<FPNode<Type>> queue = new LinkedList<FPNode<Type>>();
        queue.add(root);
        while(!queue.isEmpty()) {
            FPNode<Type> cur = queue.poll();
            System.out.println(cur);
            for(FPNode<Type> child : cur.children.values()) {
                queue.add(child);
            }
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

    public static void main(String[] args) {
        FPTree<Character> fp = new FPTree<Character>();
        ItemGenerator<Character> gen = new ItemGenerator<Character>(Character.class);
        Random r = new Random();

        for(int i = 0; i < 1000; i++) {
            ItemSet<Character> itemSet = gen.newItemSet();
            for(int j = 0; j < r.nextInt(10); j++) {
                itemSet.add(gen.newItem((char)(r.nextInt(26) + 'a')));
            }
            fp.addItemSet(itemSet);
        }

        fp.build();
        fp.levelOrder();

    }
}

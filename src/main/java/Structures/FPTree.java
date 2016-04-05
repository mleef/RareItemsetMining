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
    private final ConcurrentHashMap<Item<Type>, FPNode<Type>> neighbors;

    /**
     * Constructor
     */
    public FPTree() {
        this.root = new FPNode<Type>(null, null, null);
        this.itemSets = Collections.synchronizedList(new ArrayList<ItemSet<Type>>());
        this.itemSupports = new ConcurrentHashMap<Item<Type>, Integer>();
        this.neighbors = new ConcurrentHashMap<Item<Type>, FPNode<Type>>();
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
            // Insert each item set into tree before removing
            Iterator<ItemSet<Type>> itemSetIterator = itemSets.iterator();
            while(itemSetIterator.hasNext()) {
                insert(itemSetIterator.next().supportOrder());
                itemSetIterator.remove();
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
            // If child with matching item exists, update support
            if(current.children.containsKey(item)) {
                current.children.get(item).support++;
                current = current.children.get(item);
            } else {
                // Create new node with associated item and update neighboring links
                FPNode<Type> newNode = new FPNode<Type>(null, item, current);
                current.children.put(item, newNode);
                updateNeighborLinks(newNode);
                current = newNode;
            }

        }
    }

    /**
     * Constructs list of all nodes in the tree with the same item
     * @param item Contained item in nodes to match
     * @return List of nodes containing matching item
     */
    private ArrayList<FPNode<Type>> getNodesByItem(Item<Type> item) {
        ArrayList<FPNode<Type>> neighboringNodes = new ArrayList<FPNode<Type>>();
        if(!neighbors.containsKey(item)) {
            return neighboringNodes;
        } else {
            FPNode<Type> currentNeighbor = neighbors.get(item);
            while(currentNeighbor != null) {
                neighboringNodes.add(currentNeighbor);
                currentNeighbor = currentNeighbor.neighbor;
            }
        }
        return neighboringNodes;

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

    /**
     * Updates links between nodes that possess the same item
     * @param node Node to add to end of chain
     */
    private void updateNeighborLinks(FPNode<Type> node) {
        if(!neighbors.containsKey(node.item)) {
            neighbors.put(node.item, node);
        } else {
            FPNode<Type> cur = neighbors.get(node.item);
            while(cur.neighbor != null) {
                cur = cur.neighbor;
            }
            cur.neighbor = node;
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

        System.out.println("Links of nodes containing item 'g':");
        for(FPNode<Character> node : fp.getNodesByItem(gen.newItem('g'))) {
            System.out.println(node);
        }
        System.out.println();
        System.out.println("Level order traversal:");
        fp.levelOrder();

    }
}

package mining;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by marcleef on 3/29/16.
 * Frequent Pattern tree construction and querying operations
 */
public class FPTree<Type> implements Serializable {
    private FPNode<Type> root;
    private final List<ItemSet<Type>> itemSets;
    private final ConcurrentHashMap<Item<Type>, Integer> itemSupports; // To track item frequencies
    private final ConcurrentHashMap<Item<Type>, FPNode<Type>> neighbors;

    /**
     * Constructor
     */
    public FPTree() {
        this.root = new FPNode<>(null, null);
        this.itemSets = Collections.synchronizedList(new ArrayList<>());
        this.itemSupports = new ConcurrentHashMap<>();
        this.neighbors = new ConcurrentHashMap<>();
    }

    /**
     * Adds new item set for tree construction
     * @param itemSet ItemSet to add to tree
     */
    public void addItemSet(ItemSet<Type> itemSet) {
        if(itemSet == null) {
            throw new NullPointerException("ItemSet cannot be null");
        }
        updateGlobalSupports(itemSet);
        itemSets.add(itemSet);
    }

    /**
     * Constructs FP tree by inserting each stored itemset into the tree
     */
    public void build() {
        System.out.println("Building tree:  " + itemSets);
        synchronized (itemSets) {
            // Insert each item set into tree before removing
            Iterator<ItemSet<Type>> itemSetIterator = itemSets.iterator();
            while(itemSetIterator.hasNext()) {
                ItemSet<Type> itemSet = itemSetIterator.next();
                updateItemSupports(itemSet);
                insert(itemSet.supportOrder());
                itemSetIterator.remove();
            }

            //TODO Remove this once the mining is being done. Maybe add functionalyty to expose the tree through the interface?
            levelOrder();
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
                FPNode<Type> newNode = new FPNode<>(item, current);
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
        ArrayList<FPNode<Type>> neighboringNodes = new ArrayList<>();
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
        LinkedList<FPNode<Type>> queue = new LinkedList<>();
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
    private void updateGlobalSupports(ItemSet<Type> itemSet) {
        for(Item<Type> item : itemSet) {
            if(!itemSupports.containsKey(item)) {
                itemSupports.put(item, 0);
            }
            itemSupports.put(item, itemSupports.get(item) + 1);
        }
    }

    /**
     * Updates support of item objects to reflect global counts
     * @param itemSet ItemSet of items to be modified
     */
    private void updateItemSupports(ItemSet<Type> itemSet) {
        for(Item<Type> item : itemSet) {
            item.setSupport(itemSupports.get(item));
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


    //TODO Move this to a separate test class
    public static void main(String[] args) {

        FPTree<Character> fp = new FPTree<>();
        ItemGenerator<Character> gen = new ItemGenerator<>(Character.class);

        // Testing sample tree from https://en.wikibooks.org/wiki/Data_Mining_Algorithms_In_R/Frequent_Pattern_Mining/The_FP-Growth_Algorithm#cite_note-CorneliaRobert-5
        ItemSet<Character> is1 = gen.newItemSet();
        ItemSet<Character> is2 = gen.newItemSet();
        ItemSet<Character> is3 = gen.newItemSet();
        ItemSet<Character> is4 = gen.newItemSet();
        ItemSet<Character> is5 = gen.newItemSet();
        ItemSet<Character> is6 = gen.newItemSet();

        is1.add(gen.newItem('A'));
        is1.add(gen.newItem('B'));
        is1.add(gen.newItem('D'));
        is1.add(gen.newItem('E'));

        is2.add(gen.newItem('B'));
        is2.add(gen.newItem('C'));
        is2.add(gen.newItem('E'));

        is3.add(gen.newItem('A'));
        is3.add(gen.newItem('B'));
        is3.add(gen.newItem('D'));
        is3.add(gen.newItem('E'));

        is4.add(gen.newItem('A'));
        is4.add(gen.newItem('B'));
        is4.add(gen.newItem('C'));
        is4.add(gen.newItem('E'));

        is5.add(gen.newItem('A'));
        is5.add(gen.newItem('B'));
        is5.add(gen.newItem('C'));
        is5.add(gen.newItem('D'));
        is5.add(gen.newItem('E'));

        is6.add(gen.newItem('B'));
        is6.add(gen.newItem('C'));
        is6.add(gen.newItem('D'));


        fp.addItemSet(is1);
        fp.addItemSet(is2);
        fp.addItemSet(is3);
        fp.addItemSet(is4);
        fp.addItemSet(is5);
        fp.addItemSet(is6);

        fp.build();
        fp.levelOrder();

        /*
        // Randomly generate 1000 item sets and populate the tree
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
        */

    }
}

package mining;

import scala.Char;

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
    private final ConcurrentHashMap<Item<Type>, LinkedList<FPNode<Type>>> neighbors;

    /**
     * Constructor
     */
    public FPTree() {
        this.root = new FPNode<>(null, null, 0);
        this.itemSets = Collections.synchronizedList(new ArrayList<>());
        this.itemSupports = new ConcurrentHashMap<>();
        this.neighbors = new ConcurrentHashMap<>();
    }

    /**
     * Copy constructor
     */
    public FPTree(FPTree<Type> tree) {
        this.root = new FPNode<>(tree.root, null);
        this.itemSets = Collections.synchronizedList(new ArrayList<>());
        this.itemSupports = new ConcurrentHashMap<>();
        this.neighbors = new ConcurrentHashMap<>();
        // Update neighbor links and item frequencies
        for(FPNode<Type> node : this.levelOrder()) {
            if(node == root) {
                continue;
            }
            //this.updateGlobalSupports(node.item);
            this.updateNeighborLinks(node);
        }
    }

    /**
     * Adds new item set for tree construction and updates global support counts
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
        synchronized (itemSets) {
            // Insert each item set into tree before removing
            Iterator<ItemSet<Type>> itemSetIterator = itemSets.iterator();
            while(itemSetIterator.hasNext()) {
                ItemSet<Type> itemSet = itemSetIterator.next();
                updateItemSupports(itemSet);
                insert(itemSet.supportOrder());
                itemSetIterator.remove();
            }
        }
    }

    /**
     * Constructs conditional FP tree without given item
     * @param item Item to remove from tree.
     */
    public FPTree<Type> buildConditional(Item<Type> item, int minThreshold) {
        FPTree<Type> conditionalTree = new FPTree<>(this);

        // Zero out supports of non-target item nodes
        for(FPNode<Type> node : conditionalTree.levelOrder()) {
            if(node == conditionalTree.root) {
                continue;
            }

            if(!node.item.equals(item)) {
                node.support = 0;
            }
        }

        // Start from leaves, search upward and increment supports
        for(FPNode<Type> leaf : conditionalTree.getNodesByItem(item)) {
            int leafSupport = leaf.support;
            FPNode<Type> current = leaf.parent;
            while (current != null) {
                current.support += leafSupport;
                current = current.parent;
            }

            // Remove leaf from parent's children
            leaf.parent.children.remove(leaf.item);
        }

        // Remove nodes that have support below threshold
        for(FPNode<Type> node : conditionalTree.levelOrder()) {
            if(node == conditionalTree.root) {
                continue;
            }
            if(node.support < minThreshold) {
                node.parent.children.remove(node.item);
            } else {
                conditionalTree.itemSupports.put(node.item, node.support);
            }
        }

        // Remove item for neighbor set
        neighbors.remove(item);

        return conditionalTree;
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
                FPNode<Type> newNode = new FPNode<>(item, current, 1);
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
    private LinkedList<FPNode<Type>> getNodesByItem(Item<Type> item) {
        LinkedList<FPNode<Type>> neighboringNodes = new LinkedList<>();
        if(!neighbors.containsKey(item)) {
            return neighboringNodes;
        } else {
            return neighbors.get(item);
        }
    }

    /**
     * Collects nodes of the tree in level order
     * @return Linked list result of traversal
     */
    public LinkedList<FPNode<Type>> levelOrder() {
        LinkedList<FPNode<Type>> queue = new LinkedList<>();
        LinkedList<FPNode<Type>> result = new LinkedList<>();
        queue.add(root);
        while(!queue.isEmpty()) {
            FPNode<Type> cur = queue.poll();
            result.add(cur);
            for(FPNode<Type> child : cur.children.values()) {
                queue.add(child);
            }
        }
        return result;
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
     * Updates tree wide item supports
     * @param item Item to update counts of
     */
    private void updateGlobalSupports(Item<Type> item) {
        if(!itemSupports.containsKey(item)) {
            itemSupports.put(item, 0);
        }
        itemSupports.put(item, itemSupports.get(item) + 1);

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
            LinkedList<FPNode<Type>> newNeighbors = new LinkedList<>();
            newNeighbors.add(node);
            neighbors.put(node.item, newNeighbors);
        } else {
            neighbors.get(node.item).add(node);
        }
    }

    /**
     * Gets the unique items used to build the tree
     * @return All items in the tree
     */
    public ArrayList<Item<Type>> items() {
        return new ArrayList<>(itemSupports.keySet());
    }

    /**
     * Gets the support of given item
     * @param item to get support of
     * @return Support of item
     */
    public int getSupport(Item<Type> item) {
        return itemSupports.get(item);
    }

    /**
     * Gets the support of given itemf
     * @return Whether or not the tree has one path
     */
    public boolean hasSinglePath() {
        FPNode<Type> current = root;
        while(current != null) {
            if(current.children.size() > 1) {
                return false;
            } else if(current.children.size() == 0) {
                return true;
            } else {
                // Runs one time
                for(FPNode<Type> node : current.children.values()) {
                    current = node;
                }
            }
        }

        throw new NullPointerException("Root of tree is null");
    }
    /**
     * Gets the unique item sets used to build the tree
     * @return All item sets in the tree
     */
    public ArrayList<ItemSet<Type>> itemSets() {
        return new ArrayList<>(itemSets);
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
        ItemSet<Character> is7 = gen.newItemSet();

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

        is7.add(gen.newItem('A'));
        is7.add(gen.newItem('B'));
        is7.add(gen.newItem('F'));


        fp.addItemSet(is1);
        fp.addItemSet(is2);
        fp.addItemSet(is3);
        fp.addItemSet(is4);
        fp.addItemSet(is5);
        fp.addItemSet(is6);


        fp.build();

        for(FPNode<Character> node : fp.levelOrder()) {
            System.out.println(node);
        }
        System.out.println();
        FPTree<Character> condTree = fp.buildConditional(gen.newItem('A'), 3);

        for(FPNode<Character> node : condTree.levelOrder()) {
            System.out.println(node);
        }

        for(Item<Character> item : condTree.items()) {
            System.out.println(item);
        }

        System.out.println(condTree.hasSinglePath());



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

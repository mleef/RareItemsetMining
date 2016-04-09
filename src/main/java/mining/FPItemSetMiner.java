package mining;

import java.io.Serializable;
import java.util.*;

/**
 * Created by marcleef on 4/5/16.
 * Encapsulates FP Tree mining logic
 */
public class FPItemSetMiner<Type> implements ItemSetMiner<Type>, Serializable {

    private int MIN_THRESHOLD;
    private int MAX_THRESHOLD;

    private FPTree<Type> tree;
    private ItemGenerator<Type> generator;

    /**
     * Constructor
     */
    public FPItemSetMiner(Class<Type> type) {
        this.tree = new FPTree<>();
        this.generator = new ItemGenerator<>(type);
    }

    /**
     * Adds new item set for tree construction
     * @param itemSet ItemSet to add to tree
     */
    public void addItemSet(ItemSet<Type> itemSet) {
        //System.out.println("Adding: " + itemSet);
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
        mine(new ArrayList<>(), this.tree, result);

        return result;

    }

    private void mine(ArrayList<Item<Type>> currentSuffix,
                      FPTree<Type> conditionalTree,  Set<ItemSet<Type>> resultItemSets) {

        // Base case
        if(conditionalTree.hasSinglePath()) {
            ArrayList<Item<Type>> newSuffix = new ArrayList<>(currentSuffix);
            for(Item<Type> item : conditionalTree.items()) {
                newSuffix.add(item);
                resultItemSets.add(generator.newItemSet(newSuffix));
                newSuffix.remove(item);
            }
            newSuffix.addAll(conditionalTree.items());
            resultItemSets.add(generator.newItemSet(newSuffix));

            return;
        }

        // Generate condition trees using each item as a suffix
        for(Item<Type> item : conditionalTree.items()) {
            if(conditionalTree.getSupport(item) > MIN_THRESHOLD) {
                ArrayList<Item<Type>> newSuffix = new ArrayList<>(currentSuffix);
                newSuffix.add(item);
                FPTree<Type> newTree = conditionalTree.buildConditional(item, MIN_THRESHOLD);
                mine(newSuffix, newTree, resultItemSets);
            }

        }


    }

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

        FPItemSetMiner<Character> miner = new FPItemSetMiner<>(Character.class);

        miner.addItemSet(is1);
        miner.addItemSet(is2);
        miner.addItemSet(is3);
        miner.addItemSet(is4);
        miner.addItemSet(is5);
        miner.addItemSet(is6);


        for(ItemSet<Character> pattern : miner.mine(3, 10)) {
            System.out.println(pattern);
        }

    }
}

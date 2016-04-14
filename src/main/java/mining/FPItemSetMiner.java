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

    private int MIN_SIZE;
    private int MAX_SIZE;

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
    public Set<ItemSet<Type>> mine(int minThreshold, int maxThreshold, int minSize, int maxSize) {
        this.tree.build();
        this.MIN_THRESHOLD = minThreshold;
        this.MAX_THRESHOLD = maxThreshold;

        this.MIN_SIZE = minSize;
        this.MAX_SIZE = maxSize;

        Set<ItemSet<Type>> result = new HashSet<>();
        // populate result with item sets within bounds
        mine(new ArrayList<>(), this.tree, result);

        Set<ItemSet<Type>> prunedResult = new HashSet<>();

        for(ItemSet<Type> itemset : result) {
            if (itemset.getSupport() <= MAX_THRESHOLD)
                prunedResult.add(itemset);
        }

        return prunedResult;

    }

    private void mine(ArrayList<Item<Type>> currentSuffix,
                      FPTree<Type> conditionalTree,  Set<ItemSet<Type>> resultItemSets) {

        // if the current conditional tree IS a conditional tree, add the set which it is conditional upon
        if (!currentSuffix.isEmpty() && currentSuffix.size() >= MIN_SIZE) {
            ItemSet<Type> suffixSet = generator.newItemSet(currentSuffix);
            suffixSet.setSupport(conditionalTree.getRootSupport());
            resultItemSets.add(generator.newItemSet(currentSuffix));
        }
        if (currentSuffix.size() >= MAX_SIZE)
            return;

        // In the base case of a single path, we generate all subsets of the items along that path
        if(conditionalTree.hasSinglePath()) {
            ArrayList<ItemSet<Type>> allItemSetsInPath = new ArrayList<>();

            /* this one weird trick, invented by a mom, for generating all subsets */
            ItemSet<Type> baseSet = generator.newItemSet(currentSuffix);
            allItemSetsInPath.add(baseSet);
            for (Item<Type> item : conditionalTree.items()) {
                ArrayList<ItemSet<Type>> newSetsThisIter = new ArrayList<>();
                for (ItemSet<Type> itemset : allItemSetsInPath) {
                    if (itemset.size() < MAX_SIZE) {
                        ItemSet<Type> newSet = new ItemSet<>(itemset);
                        newSet.add(item);
                        newSetsThisIter.add(newSet);
                    }
                }
                allItemSetsInPath.addAll(newSetsThisIter);
            }
            allItemSetsInPath.remove(baseSet); // baseSet was only included for suffixing the generated sets

            for (ItemSet<Type> itemset : allItemSetsInPath) {
                itemset.setSupportFromPath(conditionalTree);
                if (itemset.size() >= MIN_SIZE)
                    resultItemSets.add(itemset);
            }
            return;
        }

        // Generate condition trees using each item as a suffix
        for(Item<Type> item : conditionalTree.items()) {
            if(conditionalTree.getSupport(item) > MIN_THRESHOLD) {
                currentSuffix.add(item);
                FPTree<Type> newTree = conditionalTree.buildConditional(item, MIN_THRESHOLD);
                mine(currentSuffix, newTree, resultItemSets);
                currentSuffix.remove(currentSuffix.size() - 1);
            }

        }


    }

    public static void main(String[] args) {
        FPTree<Character> fp = new FPTree<>();
        ItemGenerator<Character> gen = new ItemGenerator<>(Character.class);
        FPItemSetMiner<Character> miner = new FPItemSetMiner<>(Character.class);

        /*
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



        miner.addItemSet(is1);
        miner.addItemSet(is2);
        miner.addItemSet(is3);
        miner.addItemSet(is4);
        miner.addItemSet(is5);
        miner.addItemSet(is6);

        System.out.println("Patterns found:");
        for(ItemSet<Character> pattern : miner.mine(3, 10, 1, 2)) {
            System.out.println(pattern);
        }
        */

        Random r = new Random();
        for(int i = 0; i < 100000; i++) {
            ItemSet<Character> itemSet = gen.newItemSet();
            for(int j = 0; j < r.nextInt(10); j++) {
                itemSet.add(gen.newItem((char)(r.nextInt(26) + 'a')));
            }
            miner.addItemSet(itemSet);
        }

        for(ItemSet<Character> pattern : miner.mine(1, 10, 2, 10)) {
            System.out.println(pattern);
        }

    }
}

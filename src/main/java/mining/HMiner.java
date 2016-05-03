package mining;


import org.apache.commons.collections.map.HashedMap;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mikkelKringelbach on 4/18/16.
 */
public class HMiner<T> implements ItemSetMiner<T> {

    /**
     * This is called $m$ in the paper.
     */
    private final int hashTableSize;
    private final Map<Integer, HashNode<T>> synopsis;
    private final double minSupport;
    private int N;


    private final boolean debugMode;
    /**
     * Construct a miner for a data stream
     * @param minSupport - The procentage of the stream that should contain an itemset before it is considered frequent
     * @param hashTableSize - The size of the hashtable, this will be final and not resized.
     * @param debugMode - If we are in debug mode then we will print information
     */
    public HMiner(double minSupport, int hashTableSize, boolean debugMode) {
        this.hashTableSize = hashTableSize;
        this.debugMode = debugMode;
        this.synopsis = new HashMap<>(hashTableSize);
        this.N = 0;
        this.minSupport = minSupport;

    }

    /**
     * construct a miner for a data stream, using information about the stream to provide the theoretical guarantees.
     * @param minSupport - min support \sigma in the paper
     * @param errorParam - error \varepsilon in the paper
     * @param confidence - confidence \rho in the paper
     * @param averageTransactionLength - average length of transactions in the stream L in the paper
     * @param numberOfDiffertItems - number of different items present in the data stream M in the paper
     * @param debugMode - If we are in debug mode then we will print extra information
     */
    public HMiner(double minSupport,
                  double errorParam,
                  double confidence,
                  double averageTransactionLength,
                  double numberOfDiffertItems,
                  boolean debugMode) {
        double temp = Math.ceil(Math.E / (errorParam * errorParam) * numberOfDiffertItems * (Math.exp(averageTransactionLength) - 1)
                * Math.log((1-Math.pow(2, numberOfDiffertItems)) / Math.log(confidence)));
        System.out.println("Calculated m : " + temp);
        this.hashTableSize = (int) Math.ceil(Math.E / (errorParam*errorParam) * numberOfDiffertItems * (Math.exp(averageTransactionLength) - 1)
                * Math.log((1-Math.pow(2, numberOfDiffertItems)) / Math.log(confidence)));
        this.minSupport = minSupport;
        this.debugMode = debugMode;
        this.synopsis = new HashMap<>(hashTableSize);
        this.N = 0;

        if(debugMode) {
            System.out.println("Hash table size: " + this.hashTableSize);
        }
    }

    @Override
    public void addItemSet(ItemSet<T> itemSet) {
        List<HashNode<T>> accessedNodes = new LinkedList<>();

        SubsetIterator<Item<T>> subsets = new SubsetIterator<>(itemSet);

        if(debugMode) {
            System.out.println("\nAdding itemset: " + itemSet + "\nsize: " + itemSet.size());
        }

        N++;

        if(N % 25 == 0) {
            System.out.println("Adding itemset number " + N);
        }

        while(subsets.hasNext()) {
            List<Item<T>> next = subsets.next();
            while(next.size() == 0 && subsets.hasNext()){
                next = subsets.next();
            }
            ItemSet<T> tItemSet = new ItemSet<>(next);

            if(debugMode) {
                System.out.println("subset: " + tItemSet);
            }
    
            HashNode<T> hashNode = getHashNodeForItemSet(tItemSet);

            hashNode.incrementTotalAccess();
            accessedNodes.add(hashNode);

            FrequentNode<T> frequentNode = hashNode.getNodeForItemSet(tItemSet);

            if(frequentNode != null) {
                frequentNode.incrementTrueCount();
            } else {
                boolean hasAllSubsets = true;

                Iterable<ItemSet<T>> allImmediateSubsets = getAllImmediateSubsets(tItemSet);
                for (ItemSet<T> immediateSubSet : allImmediateSubsets) {
                    if(immediateSubSet.isEmpty()) {
                        continue;
                    }
                    if (!hasFNodeForItemSet(immediateSubSet)) {
                        hasAllSubsets = false;
                        break;
                    }
                }

                if(debugMode) {
                    System.out.println("ImmediateSubsets: " + allImmediateSubsets);
                    System.out.println("Has all subsets = " + hasAllSubsets);
                }

                if(hasAllSubsets) {

                    int estimateCount = computeEstimateCount(tItemSet);

                    if(debugMode) {
                        System.out.println("Estimate for " + tItemSet + " is " + estimateCount);
                    }

                    if (estimateCount + 1 > minSupport * N) {
                        FrequentNode<T> tFrequentNode = new FrequentNode<>(tItemSet, 1, estimateCount);
                        hashNode.addFrequentNodes(tFrequentNode);
                    }
                }
            }
        }

        if(debugMode) {
            System.out.println("synopsis: " + synopsis);
        }

        // Maintain the invariant
        for (HashNode<T> accessedNode : accessedNodes) {
            accessedNode.removeFNodeBelowThreashold(minSupport * N);
            accessedNode.setLastAccess(N);
        }
    }

    /**
     * Gets the HashNode for the particular ItemSet, this is abstracted to allow for easily creation of the new nodes
     * @param itemSet
     * @return
     */
    private HashNode<T> getHashNodeForItemSet(ItemSet<T> itemSet) {
        int hashIndex = itemSet.hashCode() % this.hashTableSize;

        HashNode<T> hashNode = synopsis.get(hashIndex);

        // Create node if it doesn't exist
        if(hashNode == null) {
            hashNode = new HashNode<>(0, N);
            synopsis.put(hashIndex, hashNode);
        }

        return hashNode;
    }

    /**
     * Estimates the number of time we ahve seen an item set if it is no longer stored in the synopsis
     * @param itemSet - The item set to find the estimate of
     * @return The estimated number of times we have seen the item set.
     */
    private int computeEstimateCount(ItemSet<T> itemSet) {
        HashNode<T> hashNode = getHashNodeForItemSet(itemSet);

        int totalTrueCount = hashNode.getTotalTrueCount();
        int totalEstimateCount = hashNode.getTotalEstimateCount();

        int cUpperBound = hashNode.getTotalAccess() - totalTrueCount;
        int cLowerBound = Integer.max(0, hashNode.getTotalAccess() - (totalTrueCount + totalEstimateCount));

        double estimateCount;

        double neededSupport = minSupport * hashNode.getLastAccess();
        if (Math.ceil(neededSupport) - 1 == 0) {
            estimateCount = 0;
        } else {
            double nLowerBount = Math.ceil(cLowerBound / (Math.ceil(neededSupport) - 1));
            if(nLowerBount == 0) {
                estimateCount = cUpperBound;
            } else {
                estimateCount = cUpperBound - (nLowerBount - 1);
                if(estimateCount > neededSupport) {
                    estimateCount = Math.floor(estimateCount);
                }
            }
        }

        return (int) Math.round(estimateCount);
    }

    /**
     * Creates all the subsets of size of less than the input.
     * @param itemSet - The item set to find the immediate subsets of
     * @return An Iterable with all of the immediate subsets of the provided input
     */
    private Iterable<ItemSet<T>> getAllImmediateSubsets(ItemSet<T> itemSet) {
        Set<ItemSet<T>> subsets = new HashSet<>();

        for (Item<T> item : itemSet) {
            ItemSet<T> tItemSet = new ItemSet<>(itemSet);
            tItemSet.remove(item);
            subsets.add(tItemSet);
        }

        return subsets;
    }

    /**
     * A simple utility method to check if the synopsis contains a frequent node for a specific set.
     * @param itemSet the itemset to check if exist
     * @return true if there exists an fNode for the itemset
     */
    private boolean hasFNodeForItemSet(ItemSet<T> itemSet) {
        HashNode<T> hashNode = getHashNodeForItemSet(itemSet);
        return hashNode != null && hashNode.getNodeForItemSet(itemSet) != null;
    }

    @Override
    public Set<ItemSet<T>> mine(int minThreshold, int maxThreshold, int minSize, int maxSize) {
        System.out.println("NOTE - Ignoring minThreshold, maxThreshold, minSize, and maxSize. The threashold has to be set when the miner is created!");
        if(debugMode) {
            System.out.println("Synopsis: " + synopsis);
        }

        Set<ItemSet<T>> result = new HashSet<>();

        for (HashNode<T> hashNode : synopsis.values()) {
            result.addAll(
                    hashNode.getFrequentNodes().stream()
                            .filter(fNode -> minThreshold < fNode.getEstimateCount() + fNode.getTrueCount())
                            .map(FrequentNode::getItemSet)
                            .collect(Collectors.toList()));
        }

        return result;
    }
}



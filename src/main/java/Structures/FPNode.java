package Structures;

import java.util.HashSet;

/**
 * Created by marcleef on 3/29/16.
 * Nodes in the FP tree
 */
public class FPNode<Type> {
    private Item<Type> value; // Item stored in node
    private FPNode<Type> parent; // Parent of node
    private FPNode<Type> neighbor; // Next occurrence of Item in tree
    private HashSet<FPNode<Type>> children; // Children of node
    private int support; // Frequency of path occurrence
}

package Structures;

import java.util.HashSet;
import java.util.HashMap;

/**
 * Created by marcleef on 3/29/16.
 * Nodes in the FP tree
 */
public class FPNode<Type> {
    public Item<Type> item; // Item stored in node
    private FPNode<Type> parent; // Parent of node
    public FPNode<Type> neighbor; // Next occurrence of Item in tree
    public HashMap<Item<Type>, FPNode<Type>> children; // Children of node
    public int support; // Frequency of path occurrence

    public FPNode(Item<Type> item, FPNode<Type> parent) {
        this.item = item;
        this.parent = parent;
        this.children = new HashMap<Item<Type>, FPNode<Type>>();
        this.support = 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FPNode)) return false;

        FPNode<?> fpNode = (FPNode<?>) o;

        return item.equals(fpNode.item);

    }

    @Override
    public String toString() {
        return "FPNode{" +
                "item=" + item +
                ", support=" + support +
                '}';
    }

    @Override
    public int hashCode() {
        return item.hashCode();
    }
}

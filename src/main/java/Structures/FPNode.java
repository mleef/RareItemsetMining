package Structures;

import java.util.HashSet;
import java.util.HashMap;

/**
 * Created by marcleef on 3/29/16.
 * Nodes in the FP tree
 */
public class FPNode<Type> {
    private Item<Type> value; // Item stored in node
    private FPNode<Type> parent; // Parent of node
    private FPNode<Type> neighbor; // Next occurrence of Item in tree
    public HashMap<Item<Type>, FPNode<Type>> children; // Children of node
    public int support; // Frequency of path occurrence

    public FPNode(FPNode<Type> neighbor, Item<Type> value, FPNode<Type> parent) {
        this.neighbor = neighbor;
        this.value = value;
        this.parent = parent;
        this.children = new HashMap<Item<Type>, FPNode<Type>>();
        this.support = 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FPNode)) return false;

        FPNode<?> fpNode = (FPNode<?>) o;

        return value.equals(fpNode.value);

    }

    @Override
    public String toString() {
        return "FPNode{" +
                "value=" + value +
                ", support=" + support +
                '}';
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}

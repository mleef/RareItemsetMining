package Structures;

import java.util.ArrayList;

/**
 * Created by marcleef on 3/29/16.
 * Nodes in the FP tree
 */
public class FPNode<Type> {
    private Item<Type> value;
    private FPNode<Type> parent;
    private ArrayList<FPNode<Type>> children;
    private int support;
}

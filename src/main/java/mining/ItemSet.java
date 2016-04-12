package mining;

import com.sun.corba.se.impl.io.TypeMismatchException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * Created by marcleef on 3/29/16.
 * HashSet wrapper for item grouping
 */
public class ItemSet<Type> extends HashSet<Item<Type>> implements Serializable {
    private final Class<Type> type;
    private int support;

    /**
     * Constructor
     * @param type Item type
     */
    public ItemSet(Class<Type> type) {
        this.type = type;
        this.support = 0;
    }

    /**
     * Constructor
     * @param that An item set to duplicate
     */
    public ItemSet(ItemSet<Type> that) {
        this.type = that.type;
        this.support = that.support;
        for (Item<Type> item : that)
            this.add(item);
    }

    @Override
    /**
     * @param item Item to insert into item set
     * @return True if successful addition, false otherwise
     */
    public boolean add(Item<Type> item) {
        if(this.getType() != item.getType()) {
            throw new TypeMismatchException(String.format("Cannot add Item<%s> to ItemSet<%s>", item.getType(), this.getType()));
        }
        boolean retVal = super.add(item);
        if (retVal) {
            if (item.support < this.support)
                this.support = item.support;
            return true;
        }
        return false;
    }

    /**
     * Orders items by support
     * @return Items in set ordered by support (Greatest to least)
     */
    public ArrayList<Item<Type>> supportOrder() {
        ArrayList<Item<Type>> sortedItems = new ArrayList<>(this);
        Collections.sort(sortedItems);
        return sortedItems;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ItemSet{support=");
        sb.append(this.support);
        sb.append(",items=");
        for(Item<Type> item : this) {
            sb.append(item.toString());
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }

    /**
     * Set item set support
     * @param support Frequency of item set
     */
    public void setSupport(int support) {
        this.support = support;
    }

    /**
     * Get item set support
     * @return Item set support
     */
    public int getSupport() {
        return support;
    }

    /**
     * Sets the support of this itemset to be the minimum of the
     * supports of all nodes in the path in which this itemset's items appear.
     * ASSUMPTION: FPTree 'path' is a path. Too slow to check though.
     * @param path An unbranching FP-tree from which we will read supports
     */
    public void setSupportFromPath(FPTree<Type> path) {
        int minSupport = Integer.MAX_VALUE;
        for (Item<Type> item : this) {
            // getSupport works because each item appears only once in the
            // tree, so the tree's global support for that item is also
            // the node's supports


            if (path.contains(item)) {
                int curSupport = path.getSupport(item);
                if (curSupport < minSupport)
                    minSupport = curSupport;
            }
        }
        if (minSupport == Integer.MAX_VALUE)
            minSupport = 0;
        this.setSupport(minSupport);
    }

    /**
     * Gets the item type
     * @return Type of item set
     */
    public Class<Type> getType() {
        return type;
    }
}

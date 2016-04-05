package Structures;

import com.sun.corba.se.impl.io.TypeMismatchException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;
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

    @Override
    /**
     * @param item Item to insert into item set
     * @return True if succesful addition, false otherwise
     */
    public boolean add(Item<Type> item) {
        if(this.getType() != item.getType()) {
            throw new TypeMismatchException(String.format("Cannot add Item<%s> to ItemSet<%s>", item.getType(), this.getType()));
        }
        return super.add(item);
    }

    /**
     * Orders items by support
     * @return Items in set ordered by support (least to greatest)
     */
    public ArrayList<Item<Type>> supportOrder() {
        ArrayList<Item<Type>> sortedItems = new ArrayList<Item<Type>>(this);
        Collections.sort(sortedItems);
        return sortedItems;
    }

    /**
     * Set item set support
     * @param support Frequency of item set
     */
    public void setSupport(int support) {
        this.support = support;
    }

    /**
     * Gets the item type
     * @return Type of item set
     */
    public Class<Type> getType() {
        return type;
    }
}

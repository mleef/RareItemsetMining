package mining;

import com.sun.corba.se.impl.io.TypeMismatchException;
import java.io.Serializable;

/**
 * Created by marcleef on 3/29/16.
 * Helps in simplifying item creation.
 */
public class ItemGenerator<Type> implements Serializable{
    private final Class<Type> type;

    /**
     * @param type Type of items to generate
     */
    public ItemGenerator(Class<Type> type) {
        this.type = type;
    }

    /**
     * Generates new item based on value
     * @param value Value of item to create
     * @return Newly constructed item with given value and type
     */
    public Item<Type> newItem(Type value) {
        checkType(value);
        return new Item<>(value, type);
    }

    /**
     * Generates new value based on value and support
     * @param value Value of item to create
     * @param support Frequency of item to create
     * @return Newly constructed item with given value and type
     */
    public Item<Type> newItem(Type value, int support) {
        checkType(value);
        return new Item<>(value, support, type);
    }

    /**
     * Generates new item set of the associated type
     * @return New item set of the same type as the generator
     */
    public ItemSet<Type> newItemSet() {
        return new ItemSet<>(type);
    }

    /**
     * Generates new item set from iterable of items and sets support value to that of lowest item
     * @return New item set of the same type as the generator
     */
    public ItemSet<Type> newItemSet(Iterable<Item<Type>> items) {
        int lowSupport = Integer.MAX_VALUE;
        ItemSet<Type> itemSet = new ItemSet<>(type);
        for(Item<Type> item : items) {
            itemSet.add(item);
            if(item.support < lowSupport) {
                lowSupport = item.support;
            }
        }
        itemSet.setSupport(lowSupport);
        return itemSet;
    }

    /**
     * Validates that value matches type of generator
     * @param value Type to validate
     */
    private void checkType(Type value) {
        if(this.getType() != value.getClass()) {
            throw new TypeMismatchException(String.format("Cannot generate Item<%s> from Generator<%s>", value.getClass(), this.getType()));
        }
    }

    /**
     * @return Type of item generator
     */
    public Class<Type> getType() {
        return type;
    }
}

package Structures;

import com.sun.corba.se.impl.io.TypeMismatchException;

import java.util.HashSet;

/**
 * Created by marcleef on 3/29/16.
 * HashSet wrapper that for item grouping
 */
public class ItemSet<Type> extends HashSet<Item> {
    private final Class<Type> type;

    public ItemSet(Class<Type> type) {
        this.type = type;
    }

    @Override
    /**
     * @param item Item to insert into item set
     * @return True if succesful addition, false otherwise
     */
    public boolean add(Item item) {
        if(this.getType() != item.getType()) {
            throw new TypeMismatchException(String.format("Cannot add Item<%s> to ItemSet<%s>", item.getType(), this.getType()));
        }
        return super.add(item);
    }

    /**
     * @return Type of item set
     */
    public Class<Type> getType() {
        return type;
    }
}

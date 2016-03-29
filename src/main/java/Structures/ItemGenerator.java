package Structures;

import com.sun.corba.se.impl.io.TypeMismatchException;

/**
 * Created by marcleef on 3/29/16.
 * Helps in simplifying item creation.
 */
public class ItemGenerator<Type> {
    private final Class<Type> type;

    /**
     * @param type Type of items to generate
     */
    public ItemGenerator(Class<Type> type) {
        this.type = type;
    }

    /**
     * @param value Value of item to create
     * @return Newly constructed item with given value and type
     */
    public Item<Type> newItem(Type value) {
        if(this.getType() != value.getClass()) {
            throw new TypeMismatchException(String.format("Cannot generate Item<%s> from Generator<%s>", value.getClass(), this.getType()));
        }
        return new Item<Type>(value, type);
    }

    /**
     * @return Type of item generator
     */
    public Class<Type> getType() {
        return type;
    }
}

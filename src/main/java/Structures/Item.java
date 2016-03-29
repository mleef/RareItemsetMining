package Structures;

/**
 * Created by marcleef on 3/29/16.
 * Item definition and basic typing functionality
 */
public class Item<Type> {
    private Type value; // Item type
    private final Class<Type> type; // Class of type

    /**
     * @param val Item value
     * @param type Item type
     */
    public Item(Type val, Class<Type> type) {
        this.value = val;
        this.type = type;
    }


    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * @return Type of item
     */
    public Class<Type> getType() {
        return type;
    }
}

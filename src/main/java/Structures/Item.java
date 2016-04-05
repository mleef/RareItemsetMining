package Structures;

/**
 * Created by marcleef on 3/29/16.
 * Item definition and basic typing functionality
 */
public class Item<Type> implements Comparable<Item<Type>> {
    private Type value; // Item type
    private int support; // Item frequency
    private final Class<Type> type; // Class of type

    /**
     * Constructor with no support
     * @param val Item value
     * @param type Item type
     */
    public Item(Type val, Class<Type> type) {
        this.value = val;
        this.type = type;
        this.support = 0;
    }

    /**
     * Constructor with support
     * @param val Item value
     * @param support Support of item
     * @param type Item type
     */
    public Item(Type val, int support, Class<Type> type) {
        this.value = val;
        this.type = type;
        this.support = support;
    }

    /**
     * Support based comparison
     * @param that Target item to compare
     * @return Positive, negative, and zero for greater than, less than, and equal
     */
    public int compareTo(Item<Type> that) {
        return that.support - this.support;
    }

    /**
     * Get type of item
     * @return Type of item
     */
    public Class<Type> getType() {
        return type;
    }

    /**
     * Set item support
     * @param support Frequency of item
     */
    public void setSupport(int support) {
        this.support = support;
    }

    @Override
    public String toString() {
        return "Item{" +
                "value=" + value +
                '}';
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;

        Item<?> item = (Item<?>) o;

        return value.equals(item.value) && type.equals(item.type);
    }
}

package mining;

import org.glassfish.grizzly.utils.ArraySet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

// Gotten from https://www.quora.com/How-do-I-generate-all-subsets-of-a-set-in-Java-iteratively
public class SubsetIterator<E> {

    private final Set<E> set;
    private final int max;
    private int index;

    public SubsetIterator(Set<E> originalList) {
        set = originalList;
        max = (1 << set.size());
        index = 0;
    }

    public boolean hasNext() {
        return index < max;
    }

    public List<E> next() {
        List<E> newSet = new ArrayList<E>();
        int flag = 1;      
        for (E element : set) {
            if ((index & flag) != 0) {
                newSet.add(element);
            }
            flag <<= 1;
        }
        ++index;
        return newSet;
    }
}


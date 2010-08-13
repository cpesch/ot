package slash.ot;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReduceTest extends TestCase {

    private Operations asList(Operation... operations) {
        List<Operation> result = new ArrayList<Operation>();
        if (operations != null) {
            result.addAll(Arrays.asList(operations));
        }
        return new Operations(-1, result);
    }

    public void testDontMergeInserts() {
        assertEquals(asList(new Insert(1, "a", 1)),
                asList(new Insert(1, "a", 1)).reduce());
        assertEquals(asList(new Insert(1, "a", 1), new Insert(3, "b", 1)),
                asList(new Insert(1, "a", 1), new Insert(3, "b", 1)).reduce());
        assertEquals(asList(new Insert(1, "a", 1), new Insert(3, "b", 1), new Insert(5, "c", 1)),
                asList(new Insert(1, "a", 1), new Insert(3, "b", 1), new Insert(5, "c", 1)).reduce());
    }

    public void testMergeInserts() {
        assertEquals(asList(new Insert(1, "ab", 1)),
                asList(new Insert(1, "a", 1), new Insert(2, "b", 1)).reduce());
        assertEquals(asList(new Insert(1, "ba", 1)),
                asList(new Insert(2, "a", 1), new Insert(1, "b", 1)).reduce());
    }

    public void testMergeDeletes() {
        assertEquals(asList(new Delete(1, 3, 1)),
                asList(new Delete(1, 2, 1), new Delete(2, 3, 1)).reduce());
        assertEquals(asList(new Delete(1, 3, 1)),
                asList(new Delete(2, 3, 1), new Delete(1, 2, 1)).reduce());
    }
}

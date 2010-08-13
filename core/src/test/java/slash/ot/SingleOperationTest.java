package slash.ot;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SingleOperationTest extends TestCase {

    private List<Operation> asList(Operation... operations) {
        List<Operation> result = new ArrayList<Operation>();
        if (operations != null) {
            result.addAll(Arrays.asList(operations));
        }
        return result;
    }

    public void testInsertBeforeInsert() {
        assertEquals(asList(new Insert(6, "a", 2)), new Insert(5, "a", 2).transformToInclude(new Insert(2, "b", 1)));
    }

    public void testInsertAfterInsert() {
        assertEquals(asList(new Insert(5, "a", 2)), new Insert(5, "a", 2).transformToInclude(new Insert(10, "b", 1)));
    }

    public void testInsertsAtSamePositionHigherClientId() {
        assertEquals(asList(new Insert(6, "a", 2)), new Insert(5, "a", 2).transformToInclude(new Insert(5, "b", 1)));
    }

    public void testInsertsAtSamePositionLowerClientId() {
        assertEquals(asList(new Insert(5, "a", 1)), new Insert(5, "a", 1).transformToInclude(new Insert(5, "b", 2)));
    }


    public void testDeleteBeforeDelete() {
        assertEquals(asList(new Delete(4, 5, 2)), new Delete(5, 6, 2).transformToInclude(new Delete(2, 3, 1)));
        assertEquals(asList(new Delete(1, 4, 2)), new Delete(4, 7, 2).transformToInclude(new Delete(1, 4, 1)));
    }

    public void testDeleteAfterDelete() {
        assertEquals(asList(new Delete(5, 6, 2)), new Delete(5, 6, 2).transformToInclude(new Delete(10, 11, 1)));
        assertEquals(asList(new Delete(1, 4, 2)), new Delete(1, 4, 2).transformToInclude(new Delete(4, 7, 1)));
    }

    public void testDeleteIncludedInDelete() {
        assertEquals(asList(), new Delete(5, 6, 2).transformToInclude(new Delete(4, 7, 1)));
    }

    public void testDeleteCoversDelete() {
        assertEquals(asList(new Delete(2, 6, 2)), new Delete(2, 8, 2).transformToInclude(new Delete(4, 6, 1)));
    }

    public void testDeleteOverlapsWithStartOfDelete() {
        assertEquals(asList(new Delete(5, 8, 2)), new Delete(5, 10, 2).transformToInclude(new Delete(8, 12, 1)));
        assertEquals(asList(new Delete(5, 9, 2)), new Delete(5, 10, 2).transformToInclude(new Delete(9, 10, 1)));
    }

    public void testDeleteOverlapsWithEndOfDelete() {
        assertEquals(asList(new Delete(5, 7, 2)), new Delete(8, 12, 2).transformToInclude(new Delete(5, 10, 1)));
        assertEquals(asList(new Delete(5, 9, 2)), new Delete(5, 10, 2).transformToInclude(new Delete(5, 6, 1)));
    }


    public void testInsertBeforeDelete() {
        assertEquals(asList(new Delete(6, 8, 2)), new Delete(5, 7, 2).transformToInclude(new Insert(2, "a", 1)));
    }

    public void testInsertAfterDelete() {
        assertEquals(asList(new Delete(5, 7, 2)), new Delete(5, 7, 2).transformToInclude(new Insert(10, "a", 1)));
    }

    public void testInsertIncludedInDelete() {
        assertEquals(asList(new Delete(2, 4, 2), new Delete(5, 9, 2)), new Delete(2, 8, 2).transformToInclude(new Insert(4, "abc", 1)));
    }


    public void testDeleteBeforeInsert() {
        assertEquals(asList(new Insert(4, "a", 2)), new Insert(5, "a", 2).transformToInclude(new Delete(2, 3, 1)));
    }

    public void testDeleteAfterInsert() {
        assertEquals(asList(new Insert(5, "a", 2)), new Insert(5, "a", 2).transformToInclude(new Delete(10, 11, 1)));
    }

    public void testDeleteIncludedInInsert() {
        assertEquals(asList(new Insert(2, "a", 2)), new Insert(4, "a", 2).transformToInclude(new Delete(2, 8, 1)));
    }

    public void testDeleteOverlapsWithStartOfInsert() {
        assertEquals(asList(new Insert(2, "abc", 2)), new Insert(4, "abc", 2).transformToInclude(new Delete(2, 4, 1)));
    }
}
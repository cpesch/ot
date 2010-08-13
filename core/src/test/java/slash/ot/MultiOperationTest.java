package slash.ot;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiOperationTest extends TestCase {

    private Operations asList(Operation... operations) {
        List<Operation> result = new ArrayList<Operation>();
        if (operations != null) {
            result.addAll(Arrays.asList(operations));
        }
        return new Operations(-1, result);
    }

    public void testInsertAtSamePositionHigherClientId() {
        assertEquals(asList(new Insert(8, "a", 2)),
                asList(new Insert(5, "a", 2)).transformToInclude(
                        asList(new Insert(5, "b", 1), new Insert(6, "b", 1), new Insert(7, "b", 1))));
    }

    public void testInsertAtSamePositionLowerClientId() {
        assertEquals(asList(new Insert(5, "a", 1)),
                asList(new Insert(5, "a", 1)).transformToInclude(
                        asList(new Insert(5, "b", 2), new Insert(6, "b", 2), new Insert(7, "b", 2))));
    }

    public void testInsertsAtSamePositionHigherClientId() {
        assertEquals(asList(new Insert(8, "a", 2), new Insert(9, "a", 2)),
                asList(new Insert(5, "a", 2), new Insert(6, "a", 2)).transformToInclude(
                        asList(new Insert(5, "b", 1), new Insert(6, "b", 1), new Insert(7, "b", 1))));
    }

    public void testInsertsAtSamePositionLowerClientId() {
        assertEquals(asList(new Insert(5, "a", 1), new Insert(6, "a", 1)),
                asList(new Insert(5, "a", 1), new Insert(6, "a", 1)).transformToInclude(
                        asList(new Insert(5, "b", 2), new Insert(6, "b", 2), new Insert(7, "b", 2))));
    }

    public void testDeleteBeforeDelete() {
        assertEquals(asList(new Delete(4, 5, 2)), asList(new Delete(5, 6, 2)).transformToInclude(asList(new Delete(2, 3, 1))));
    }
}
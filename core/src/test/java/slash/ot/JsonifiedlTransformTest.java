package slash.ot;

import junit.framework.TestCase;
import org.svenson.JSONParser;
import org.svenson.tokenize.InputStreamSource;
import slash.ot.client.Client;
import slash.ot.server.Server;
import slash.ot.server.ServerImpl;

import java.util.*;

public class JsonifiedlTransformTest extends TestCase {
    private Client client1 = new Client();
    private Client client2 = new Client();
    private Server server = new ServerImpl();

    protected void setUp() throws Exception {
        client1.connect(server, 1);
        client2.connect(server, 2);
    }

    public void testChristiansTestcase() {
        execute("christians-testcases.json");
    }

    public void testDanielsTestcase() {
        execute("daniels-testcases.json");
    }

    public void testDanielsMultiTestcase() {
        execute("daniels-multi-testcases.json");
    }

    @SuppressWarnings({"unchecked"})
    private void execute(String resourceName) {
        JSONParser parser = JSONParser.defaultJSONParser();
        InputStreamSource source = new InputStreamSource(getClass().getResourceAsStream(resourceName), true);
        Map<String, Map> testCases = parser.parse(Map.class, source);
        Set<String> testCaseKeys = testCases.keySet();
        String[] testCaseNames = testCaseKeys.toArray(new String[testCaseKeys.size()]);
        Arrays.sort(testCaseNames, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        for (String testCaseName : testCaseNames) {
            execute(testCaseName, testCases.get(testCaseName));
        }
    }

    private void execute(String testCaseName, Map<String, List<Map<String, Object>>> testCaseDetails) {
        List<Operation> ops1 = extract("op1", testCaseDetails);
        List<Operation> ops2 = extract("op2", testCaseDetails);
        List<Operation> results = extract("result", testCaseDetails);

        Operations o1 = new Operations(-1, ops1);
        Operations o2 = new Operations(-1, ops2);
        Operations r = new Operations(-1, results);
        assertEquals(testCaseName + " failed", r, o1.transformToInclude(o2));
    }

    private List<Operation> extract(String operationName, Map<String, List<Map<String, Object>>> operationsDetails) {
        List<Map<String, Object>> operations = operationsDetails.get(operationName);
        List<Operation> result = new ArrayList<Operation>();
        for (Map<String, Object> operation : operations) {
            result.add(extract(operation));
        }
        return result;
    }

    private Operation extract(Map<String, Object> operationDetails) {
        String op = operationDetails.get("op").toString();
        if ("i".equals(op)) {
            return new Insert(extractInteger(operationDetails.get("pos")), operationDetails.get("text").toString(), extractClientId(operationDetails.get("user")));
        } else if ("d".equals(op)) {
            int startIndex = extractInteger(operationDetails.get("pos"));
            return new Delete(startIndex, startIndex + extractInteger(operationDetails.get("len")), extractClientId(operationDetails.get("user")));
        }
        throw new IllegalArgumentException("Operation " + op + " is not supported");
    }

    private int extractInteger(Object aLong) {
        return new Long(aLong.toString()).intValue();
    }

    private int extractClientId(Object aLong) {
        return aLong.toString().charAt(0);
    }
}
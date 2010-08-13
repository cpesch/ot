package slash.ot.common;

import org.svenson.JSON;
import org.svenson.JSONParser;
import org.svenson.JSONifier;
import org.svenson.PropertyValueBasedTypeMapper;
import slash.ot.Delete;
import slash.ot.Insert;
import slash.ot.Operations;
import slash.ot.OperationsOnContent;

import java.util.HashMap;
import java.util.Map;

public class JsonHelper {
    public static final String CLIENT_ID = "clientId";
    public static final String CONTENT_IDS = "contentIds";
    public static final String OPERATIONS_ON_CONTENTS = "operationsOnContents";

    private static final String INSERT_TYE = "insert";
    private static final String DELETE_TYPE = "delete";

    public static String format(Object object) {
        return createFormatter().forValue(object);
    }

    public static Object parse(String string) {
        return createParser().parse(string);
    }

    private static JSONParser parser = null;

    private synchronized static JSONParser createParser() {
        if (parser == null) {
            parser = new JSONParser();
            parser.addTypeHint("." + OPERATIONS_ON_CONTENTS + "[]", OperationsOnContent.class);
            parser.addTypeHint("." + OPERATIONS_ON_CONTENTS + "[].operations", Operations.class);
            PropertyValueBasedTypeMapper mapper = new PropertyValueBasedTypeMapper();
            mapper.setParsePathInfo("." + OPERATIONS_ON_CONTENTS + "[].operations.operations[]");
            mapper.addFieldValueMapping(INSERT_TYE, Insert.class);
            mapper.addFieldValueMapping(DELETE_TYPE, Delete.class);
            parser.setTypeMapper(mapper);
        }
        return parser;
    }

    private static JSON formatter = null;

    private synchronized static JSON createFormatter() {
        if (formatter == null) {
            formatter = new JSON();
            formatter.registerJSONifier(Insert.class, new InsertJSONifier());
            formatter.registerJSONifier(Delete.class, new DeleteJSONifier());
        }
        return formatter;
    }

    private static class InsertJSONifier implements JSONifier {
        public String toJSON(Object o) {
            Insert insert = (Insert) o;
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("type", INSERT_TYE);
            map.put("clientId", insert.getClientId());
            map.put("delta", insert.getDelta());
            map.put("startIndex", insert.getStartIndex());
            return JSON.defaultJSON().forValue(map);
        }
    }

    private static class DeleteJSONifier implements JSONifier {
        public String toJSON(Object o) {
            Delete delete = (Delete) o;
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("type", DELETE_TYPE);
            map.put("clientId", delete.getClientId());
            map.put("endIndex", delete.getEndIndex());
            map.put("startIndex", delete.getStartIndex());
            return JSON.defaultJSON().forValue(map);
        }
    }
}

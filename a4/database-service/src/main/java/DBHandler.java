import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

public class DBHandler {

  private static final String KEY_NAME = "PurchaseID";
  private static final String STORE_ID_NAME = "StoreID";
  private static final String CUSTOMER_ID_NAME = "CustomerID";
  private static final String DATE_NAME = "Date";
  private static final String ITEMS_NAME = "Items";
  private static final String TABLE_NAME = "Purchases";

  public static void insert(DynamoDbClient ddb, PurchaseRecord record) {
    HashMap<String, AttributeValue> itemValues = new HashMap<>();
    // Add all content to the table
    itemValues.put(KEY_NAME, AttributeValue.builder().s(record.getUuid()).build());
    itemValues.put(STORE_ID_NAME,
        AttributeValue.builder().n(String.valueOf(record.getStoreID())).build());
    itemValues.put(CUSTOMER_ID_NAME,
        AttributeValue.builder().n(String.valueOf(record.getCustomerID())).build());
    itemValues.put(DATE_NAME, AttributeValue.builder().s(record.getDate()).build());

    Map<String, AttributeValue> m = new HashMap<>();
    for (Entry<String, Integer> en : record.getItemsMap().entrySet()) {
      m.put(en.getKey(), AttributeValue.builder().n(String.valueOf(en.getValue())).build());
    }
    itemValues.put(ITEMS_NAME, AttributeValue.builder().m(m).build());

    PutItemRequest request = PutItemRequest.builder()
        .tableName(TABLE_NAME)
        .item(itemValues)
        .build();

    try {
      ddb.putItem(request);
      System.out.println(request.toString());
    } catch (ResourceNotFoundException e) {
      System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", TABLE_NAME);
      System.err.println("Be sure that it exists and that you've typed its name correctly!");
      System.exit(1);
    } catch (DynamoDbException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }
}

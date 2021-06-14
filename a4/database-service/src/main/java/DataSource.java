import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DataSource {

  private static DynamoDbClient ddb;

  static {
    ddb = DynamoDbClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();
  }

  public static DynamoDbClient getDataSource() {
    return ddb;
  }
}
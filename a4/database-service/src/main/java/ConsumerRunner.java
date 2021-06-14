import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class ConsumerRunner implements Runnable {

  private final KafkaConsumer<String, String> consumer;

  public ConsumerRunner() {
    Properties props = new Properties();
    props.put("bootstrap.servers", "ec2-18-212-4-84.compute-1.amazonaws.com:9092");
    props.put("advertised.listeners", "ec2-18-212-4-84.compute-1.amazonaws.com");
    props.setProperty("group.id", "post");
    props.setProperty("enable.auto.commit", "true");
    props.setProperty("auto.commit.interval.ms", "1000");
    props.setProperty("key.deserializer",
        "org.apache.kafka.common.serialization.StringDeserializer");
    props.setProperty("value.deserializer",
        "org.apache.kafka.common.serialization.StringDeserializer");
    this.consumer = new KafkaConsumer<>(props);
    consumer.subscribe(Arrays.asList("post"));
  }

  @Override
  public void run() {
    while (true) {
      ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
      ObjectMapper mapper = new ObjectMapper();
      for (ConsumerRecord<String, String> record : records) {
        try {
          PurchaseRecord purchaseRecord = mapper.readValue(record.value(), PurchaseRecord.class);

          // insert data to dynamodb
          DynamoDbClient ddb = DataSource.getDataSource();
          DBHandler.insert(ddb, purchaseRecord);
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }
      }
    }

  }
}

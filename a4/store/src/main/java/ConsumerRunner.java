import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisHashCommands;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class ConsumerRunner implements Runnable {

  private static final String DELIMITER = "-";
  private final KafkaConsumer<String, String> consumer;
  private StatefulRedisConnection<String, String> redisConn;

  public ConsumerRunner(StatefulRedisConnection<String, String> redisConn) {
    Properties props = new Properties();
    props.put("bootstrap.servers", "ec2-18-212-4-84.compute-1.amazonaws.com:9092");
    props.put("advertised.listeners", "ec2-18-212-4-84.compute-1.amazonaws.com");
    props.setProperty("group.id", "store");
    props.setProperty("enable.auto.commit", "true");
    props.setProperty("auto.commit.interval.ms", "1000");
    props.setProperty("key.deserializer",
        "org.apache.kafka.common.serialization.StringDeserializer");
    props.setProperty("value.deserializer",
        "org.apache.kafka.common.serialization.StringDeserializer");
    this.consumer = new KafkaConsumer<>(props);
    consumer.subscribe(Arrays.asList("post"));
    this.redisConn = redisConn;
  }

  @Override
  public void run() {
    while (true) {
      ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
      ObjectMapper mapper = new ObjectMapper();
      for (ConsumerRecord<String, String> record : records) {
        try {
          PurchaseRecord purchaseRecord = mapper.readValue(record.value(), PurchaseRecord.class);
          // update data in redis
          updateCache(purchaseRecord);
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }
//        System.out.printf("%s offset = %d, key = %s, value = %s%n", Thread.currentThread(), record.offset(), record.key(), record.value());
      }
    }
  }

  private void updateCache(PurchaseRecord record) {
    RedisHashCommands<String, String> syncCommands = redisConn.sync();
    for (Entry<String, Integer> pair : record.getItemsMap().entrySet()) {
      String key = record.getStoreID() + DELIMITER + pair.getKey();
      syncCommands.hincrby(key, "number", pair.getValue());
    }
  }

}

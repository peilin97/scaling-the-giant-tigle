import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class ConsumerDataSource {

  private static final String GET_REPLY_TOPIC = "get-reply";

  public static String getReply() {
    KafkaConsumer<String, String> consumer = newConsumer();
    while (true) {
      ConsumerRecords<String, String> messages = consumer.poll(Duration.ofMillis(100));
      for (ConsumerRecord<String, String> message : messages) {
        return message.value();
      }
    }
  }

  public static KafkaConsumer<String, String> newConsumer() {
    Properties props = new Properties();
    props.put("bootstrap.servers", "ec2-18-212-4-84.compute-1.amazonaws.com:9092");
    props.put("advertised.listeners", "ec2-18-212-4-84.compute-1.amazonaws.com");
    props.setProperty("group.id", "servlet-get-consumer");
    props.setProperty("enable.auto.commit", "true");
    props.setProperty("auto.commit.interval.ms", "1000");
    props.setProperty("key.deserializer",
        "org.apache.kafka.common.serialization.StringDeserializer");
    props.setProperty("value.deserializer",
        "org.apache.kafka.common.serialization.StringDeserializer");
    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
    consumer.subscribe(Arrays.asList(GET_REPLY_TOPIC));

    return consumer;
  }
}

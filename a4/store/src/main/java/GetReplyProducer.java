import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class GetReplyProducer {

  private static final String GET_REPLY_TOPIC = "get-reply";
  private static Producer<String, String> producer;

  static {
    Properties props = new Properties();
    props.put("bootstrap.servers", "ec2-18-212-4-84.compute-1.amazonaws.com:9092");
    props.put("advertised.listeners", "ec2-18-212-4-84.compute-1.amazonaws.com");
    props.put("acks", "all");
    props.put("retries", 0);
    props.put("batch.size", 16384);
    props.put("linger.ms", 1);
    props.put("buffer.memory", 33554432);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

    producer = new KafkaProducer<>(props);
  }

  public static void sendReply(String message) {
    producer.send(new ProducerRecord(GET_REPLY_TOPIC, message));
  }

}

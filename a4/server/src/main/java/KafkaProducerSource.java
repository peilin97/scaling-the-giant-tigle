import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

public class KafkaProducerSource {

  private static Producer<String, String> producer;

  static {
    Properties props = new Properties();
    props.put("bootstrap.servers", "ec2-18-212-4-84.compute-1.amazonaws.com:9092");
    props.put("advertised.listeners", "ec2-18-212-4-84.compute-1.amazonaws.com");
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    producer = new KafkaProducer<>(props);
  }

  public static Producer<String, String> getProducer() {
    return producer;
  }

}


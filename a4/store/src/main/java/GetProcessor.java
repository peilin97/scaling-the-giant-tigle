import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisHashCommands;
import io.lettuce.core.api.sync.RedisKeyCommands;
import io.swagger.client.model.TopItems;
import io.swagger.client.model.TopItemsStores;
import io.swagger.client.model.TopStores;
import io.swagger.client.model.TopStoresStores;
import java.time.Duration;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class GetProcessor implements Runnable {

  private static final String DELIMITER = "-";
  private static final String GET_REQUEST_TOPIC = "get-request";
  private final KafkaConsumer<String, String> consumer;
  private StatefulRedisConnection<String, String> redisConn;

  public GetProcessor(RedisClient redisClient) {
    Properties replyProps = new Properties();
    replyProps.put("bootstrap.servers", "ec2-18-212-4-84.compute-1.amazonaws.com:9092");
    replyProps.put("advertised.listeners", "ec2-18-212-4-84.compute-1.amazonaws.com");
    replyProps.setProperty("group.id", "store-get-response");
    replyProps.setProperty("enable.auto.commit", "true");
    replyProps.setProperty("auto.commit.interval.ms", "1000");
    replyProps.setProperty("key.deserializer",
        "org.apache.kafka.common.serialization.StringDeserializer");
    replyProps.setProperty("value.deserializer",
        "org.apache.kafka.common.serialization.StringDeserializer");
    this.consumer = new KafkaConsumer<>(replyProps);
    this.consumer.subscribe(Arrays.asList(GET_REQUEST_TOPIC));
    this.redisConn = redisClient.connect();
  }

  @Override
  public void run() {
    while (true) {
      ConsumerRecords<String, String> messages = consumer.poll(Duration.ofMillis(100));
      for (ConsumerRecord<String, String> message : messages) {
        String response = getResponse(message.value());
        // send response
        GetReplyProducer.sendReply(response);
      }
    }
  }

  private String getResponse(String message) {
    String[] request = message.split("/");
    return (request[0].equals("store"))
        ? getTop10Items(request[1])
        : getTop10Stores(request[1]);
  }

  private String getTop10Items(String storeID) {
    PriorityQueue<Entry<String, Integer>> pq = new PriorityQueue<>(
        new Comparator<Entry<String, Integer>>() {
          @Override
          public int compare(Entry<String, Integer> t1, Entry<String, Integer> t2) {
            return t1.getValue() - t2.getValue();
          }
        });
    RedisHashCommands<String, String> hashCommands = redisConn.sync();
    RedisKeyCommands<String, String> keyCommands = redisConn.sync();
    for (String key : keyCommands.keys(storeID + DELIMITER + "*")) {
      String itemID = key.split(DELIMITER)[1];
      Integer num = Integer.valueOf(hashCommands.hget(key, "number"));
      pq.add(new SimpleEntry<>(itemID, num));
      if (pq.size() > 10) {
        pq.poll();
      }
    }
    TopItems topItems = new TopItems();
    if (pq.isEmpty()) {
      TopItemsStores tmp = new TopItemsStores();
      tmp.setItemID(0);
      tmp.setNumberOfItems(0);
      topItems.addStoresItem(tmp);
    } else {
      while (!pq.isEmpty()) {
        Entry<String, Integer> en = pq.poll();
        TopItemsStores tmp = new TopItemsStores();
        tmp.setItemID(Integer.valueOf(en.getKey()));
        tmp.setNumberOfItems(en.getValue());
        topItems.addStoresItem(tmp);
      }
    }

    return topItems.toString();
  }

  private String getTop10Stores(String itemID) {
    PriorityQueue<Entry<String, Integer>> pq = new PriorityQueue<>(
        new Comparator<Entry<String, Integer>>() {
          @Override
          public int compare(Entry<String, Integer> t1, Entry<String, Integer> t2) {
            return t1.getValue() - t2.getValue();
          }
        });
    RedisHashCommands<String, String> hashCommands = redisConn.sync();
    RedisKeyCommands<String, String> keyCommands = redisConn.sync();
    for (String key : keyCommands.keys("*" + DELIMITER + itemID)) {
      String storeID = key.split(DELIMITER)[0];
      Integer num = Integer.valueOf(hashCommands.hget(key, "number"));
      pq.add(new SimpleEntry<>(storeID, num));
      if (pq.size() > 10) {
        pq.poll();
      }
    }
    TopStores topStores = new TopStores();
    if (pq.isEmpty()) {
      TopStoresStores tmp = new TopStoresStores();
      tmp.setStoreID(0);
      tmp.setNumberOfItems(0);
      topStores.addStoresItem(tmp);
    } else {
      while (!pq.isEmpty()) {
        Entry<String, Integer> en = pq.poll();
        TopStoresStores tmp = new TopStoresStores();
        tmp.setStoreID(Integer.valueOf(en.getKey()));
        tmp.setNumberOfItems(en.getValue());
        topStores.addStoresItem(tmp);
      }
    }

    return topStores.toString();
  }
}

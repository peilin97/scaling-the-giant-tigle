import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisHashCommands;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class PostConsumer implements Runnable {

  private static final String WRITE_QUEUE_NAME = "store_queue";
  private static final String DELIMITER = "-";
  private Connection rabbitConn;
  private StatefulRedisConnection<String, String> redisConn;

  public PostConsumer(Connection rabbitConn, StatefulRedisConnection<String, String> redisConn) {
    this.rabbitConn = rabbitConn;
    this.redisConn = redisConn;
  }

  private static Object deserialize(byte[] byteArr) throws IOException, ClassNotFoundException {
    ByteArrayInputStream in = new ByteArrayInputStream(byteArr);
    ObjectInputStream is = new ObjectInputStream(in);
    return is.readObject();
  }

  @Override
  public void run() {
    try {
      Channel channel = rabbitConn.createChannel();
      boolean durable = true;
      Map<String, Object> args = new HashMap<String, Object>();
      args.put("x-queue-mode", "lazy");
      channel.queueDeclare(WRITE_QUEUE_NAME, durable, false, false, args);
//      System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
      channel.queuePurge(WRITE_QUEUE_NAME);
      channel.basicQos(1); // accept only one unack-ed message at a time (see below)
      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        PurchaseRecord record = null;
        try {
          record = (PurchaseRecord) deserialize(delivery.getBody());
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
        updateCache(record);
//        System.out.println(channel.getChannelNumber()+" Received '" + record.getItemsMap() + "'");
      };
      boolean autoAck = true;
      channel.basicConsume(WRITE_QUEUE_NAME, autoAck, deliverCallback, consumerTag -> {
      });
    } catch (IOException e) {
      e.printStackTrace();
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


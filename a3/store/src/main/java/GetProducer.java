import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisHashCommands;
import io.lettuce.core.api.sync.RedisKeyCommands;
import io.swagger.client.model.TopItems;
import io.swagger.client.model.TopItemsStores;
import io.swagger.client.model.TopStores;
import io.swagger.client.model.TopStoresStores;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.concurrent.TimeoutException;

public class GetProducer implements Runnable {

  private static final String DELIMITER = "-";
  private static String RPC_QUEUE_NAME = "rpc_queue";
  private Connection conn;
  private StatefulRedisConnection<String, String> redisConn;

  public GetProducer(Connection conn, RedisClient redisClient) {
    this.conn = conn;
    this.redisConn = redisClient.connect();
  }

  @Override
  public void run() {
    try (Channel channel = conn.createChannel()) {
      Map<String, Object> args = new HashMap<String, Object>();
//      args.put("x-queue-mode", "lazy");
      channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, args);
      channel.queuePurge(RPC_QUEUE_NAME);
      channel.basicQos(1);

      System.out.println(" [x] Awaiting RPC requests");
      Object monitor = new Object();
      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
            .Builder()
            .correlationId(delivery.getProperties().getCorrelationId())
            .build();
        String response = "";
        try {
          String message = new String(delivery.getBody(), "UTF-8");
          System.out.println(" [.] request(" + message + ")");
          response = getResponse(message);
        } catch (RuntimeException e) {
          System.out.println(" [.] " + e.toString());
        } finally {
          channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps,
              response.getBytes("UTF-8"));
//          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
          // RabbitMq consumer worker thread notifies the RPC server owner thread
          synchronized (monitor) {
            monitor.notify();
          }
        }
      };
      channel.basicConsume(RPC_QUEUE_NAME, true, deliverCallback, (consumerTag -> {
      }));
      // Wait and be prepared to consume the message from RPC client.
      while (true) {
        synchronized (monitor) {
          try {
            monitor.wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    } catch (IOException | TimeoutException e) {
      e.printStackTrace();
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


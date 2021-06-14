import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class Main {

  private static final int THREADS_NUMBER = 20;

  public static void main(String[] args)
      throws IOException, TimeoutException, NoSuchAlgorithmException, KeyManagementException, URISyntaxException, InterruptedException {
    RedisClient redisClient = RedisClient.create("redis://" + System.getProperty("REDIS_URI"));
    StatefulRedisConnection<String, String> redisConn = redisClient.connect();
    ConnectionFactory factory = new ConnectionFactory();
    factory.setUri("amqp://" + System.getProperty("MQ_URI"));

    Connection conn = factory.newConnection();
    Thread[] posts = new Thread[THREADS_NUMBER];
    for (int i = 0; i < THREADS_NUMBER; i++) {
      posts[i] = new Thread(new PostConsumer(conn, redisConn));
      posts[i].start();
    }
    Thread getTh = new Thread(new GetProducer(conn, redisClient));
    getTh.start();
  }
}

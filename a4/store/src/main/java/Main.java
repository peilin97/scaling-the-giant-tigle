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

    // create multiple threads to poll data from the post topic
    Thread[] posts = new Thread[THREADS_NUMBER];
    for (int i = 0; i < THREADS_NUMBER; i++) {
      posts[i] = new Thread(new ConsumerRunner(redisConn));
      posts[i].start();
    }

    // one thread to handle the get top request
    Thread getTh = new Thread(new GetProcessor(redisClient));
    getTh.start();
  }

}

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@WebServlet(name = "Servlet", value = "/Servlet")
public class Servlet extends HttpServlet {

  private static final String EXCHANGE_NAME = "purchase_exchange";
  private static final String requestQueueName = "rpc_queue";
  private static final String purchaseQueueName = "purchase_queue";
  private static final String storeQueueName = "store_queue";
  private Connection conn;
  private ObjectPool<Channel> channelPool;

  public void init() {
    // TODO Channel pool
    ConnectionFactory factory = new ConnectionFactory();
    try {
      factory.setUri("amqp://moony:moony@3.89.195.229:5672/custom-vhost");
    } catch (URISyntaxException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (KeyManagementException e) {
      e.printStackTrace();
    }
    try {
      this.conn = factory.newConnection();
      this.channelPool = new GenericObjectPool<>(new ChannelFactory());
    } catch (IOException e) {
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    // validate the url
    String path = request.getPathInfo();
    if (!validateGetPath(path)) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().append("Invalid URL.\n");
      return;
    }
    // send the request e.g. stores/56
    String result = "";
    try (Channel channel = conn.createChannel()) {
      result = call(channel, path.substring(7));
    } catch (TimeoutException | InterruptedException e) {
      e.printStackTrace();
    }
    response.getWriter().append(result);
//    response.getWriter().append(request.getPathInfo());  // /items/store/56
  }

  private String call(Channel channel, String message) throws IOException, InterruptedException {
    final String corrId = UUID.randomUUID().toString();
    String replyQueueName = channel.queueDeclare().getQueue();
    AMQP.BasicProperties props = new AMQP.BasicProperties
        .Builder()
        .correlationId(corrId)
        .replyTo(replyQueueName)
        .build();
    channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));
    final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);
    String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
      if (delivery.getProperties().getCorrelationId().equals(corrId)) {
        response.offer(new String(delivery.getBody(), "UTF-8"));
      }
    }, consumerTag -> {
    });

    String result = response.take();
    channel.basicCancel(ctag);
    return result;
  }

  private boolean validateGetPath(String path) {
    return path.startsWith("/items/store/") || path.startsWith("/items/top10/");
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    Channel channel = null;
    try {
      channel = channelPool.borrowObject();
      boolean durable = true;
      Map<String, Object> args = new HashMap<String, Object>();
      args.put("x-queue-mode", "lazy");
      channel.queueDeclare(storeQueueName, durable, false, false, args);
      channel.queueDeclare(purchaseQueueName, durable, false, false, args);
      PurchaseRecord record = generateRecord(request);
      if (record == null) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().append("Invalid values provided.\n");
        return;
      }
      byte[] byteArr = getByteArray(record);
      channel.basicPublish("", purchaseQueueName, null, byteArr);
      channel.basicPublish("", storeQueueName, null, byteArr);
      response.setStatus(HttpServletResponse.SC_CREATED);
      response.getWriter().append("write success\n");
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.getWriter().append("Failed to borrow a channel: " + e.toString());
    } finally {
      try {
        if (channel != null) {
          channelPool.returnObject(channel);
        }
      } catch (Exception ex) {
        // ignored
      }
    }

  }

  private byte[] getByteArray(PurchaseRecord record) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ObjectOutputStream os = new ObjectOutputStream(out);
    os.writeObject(record);
    byte[] result = out.toByteArray();
    out.close();
    os.close();
    return result;
  }

  private PurchaseRecord generateRecord(HttpServletRequest request) {
    String urlPath = request.getPathInfo();
    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      return null;
    }
    urlPath = urlPath.substring(1).toLowerCase();  // remove the front /
    String[] urlParts = urlPath.split("/");
    if (urlParts.length != 6 || !urlParts[0].equals("purchase")
        || !urlParts[2].equals("customer") || !urlParts[4].equals("date")
        || urlParts[5].length() != 8) {
      return null;
    }
    try {
      // process value of url
      int storeID = Integer.valueOf(urlParts[1]);
      int customerID = Integer.valueOf(urlParts[3]);
      Integer.valueOf(urlParts[5]);
      String date = urlParts[5];

      // process items from body
      BufferedReader reader = request.getReader();
      String body = reader.lines().collect(Collectors.joining());
      reader.close();
      JSONObject bodyJson = new JSONObject(body);
      JSONArray itemsJson = bodyJson.getJSONArray("items");
      HashMap<String, Integer> itemsMap = new HashMap<>();
      for (int i = 0; i < itemsJson.length(); i++) {
        JSONObject itemJson = itemsJson.getJSONObject(i);
        String itemID = "";
        int numberOfItems = 0;
        try {
          itemID = itemJson.getString("ItemID");
          numberOfItems = itemJson.getInt("numberOfItems");
        } catch (JSONException e) {
          e.printStackTrace();
          System.err.println(body);
        }
        itemsMap.put(itemID, itemsMap.getOrDefault(itemID, 0) + numberOfItems);
      }
      // convert itemsMap to json
//      ObjectMapper objectMapper = new ObjectMapper();
//      String itemsJSON = objectMapper.writeValueAsString(itemsMap);
      PurchaseRecord record = new PurchaseRecord(storeID, customerID, date, itemsMap);
      return record;
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public class ChannelFactory extends BasePooledObjectFactory<Channel> {

    @Override
    public Channel create() throws Exception {
      return conn.createChannel();
    }

    @Override
    public PooledObject<Channel> wrap(Channel channel) {
      return new DefaultPooledObject<>(channel);
    }
  }
}

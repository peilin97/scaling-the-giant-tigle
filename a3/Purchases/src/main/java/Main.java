import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

  private static final String QUEUE_NAME = "purchase_queue";
  private static final int THREADS_NUMBER = 30;

  public static void main(String[] args)
      throws IOException, TimeoutException, NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setUri("amqp://" + System.getProperty("MQ_URI"));
    SupermarketDao dao = new SupermarketDao();
    final Connection connection = factory.newConnection();
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          final Channel channel = connection.createChannel();
          boolean durable = true;
          Map<String, Object> args = new HashMap<String, Object>();
          args.put("x-queue-mode", "lazy");
          channel.queueDeclare(QUEUE_NAME, durable, false, false, args);
          channel.queuePurge(QUEUE_NAME);
          channel.basicQos(1);
//          System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");
          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            byte[] byteArr = delivery.getBody();
            PurchaseRecord record;
            try {
              record = (PurchaseRecord) deserialize(byteArr);
              dao.insertPurchase(record);
            } catch (ClassNotFoundException e) {
              record = null;
              e.printStackTrace();
            }
//            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//            System.out.println( "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + record.getItemsMap() + "'");
          };
          // auto-ack: true
          channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
          });
        } catch (IOException ex) {
          Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    };
    Thread[] consumers = new Thread[THREADS_NUMBER];
    for (int i = 0; i < THREADS_NUMBER; i++) {
      consumers[i] = new Thread(runnable);
      consumers[i].start();
    }
  }

  private static Object deserialize(byte[] byteArr) throws IOException, ClassNotFoundException {
    ByteArrayInputStream in = new ByteArrayInputStream(byteArr);
    ObjectInputStream is = new ObjectInputStream(in);
    return is.readObject();
  }

}

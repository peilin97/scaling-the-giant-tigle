import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.client.model.Purchase;
import io.swagger.client.model.PurchaseItems;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

@WebServlet(name = "Servlet", value = "/Servlet")
public class Servlet extends HttpServlet {

  private static final String POST_TOPIC = "post";
  private static final String GET_REQUEST_TOPIC = "get-request";

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
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
    Producer<String, String> producer = KafkaProducerSource.getProducer();
    if (producer == null) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().append("kafka producer is null\n");
      return;
    }
    producer.send(new ProducerRecord(GET_REQUEST_TOPIC, path.substring(7)));

    // then, poll response from GET_REPLY_TOPIC
    response.getWriter().append(ConsumerDataSource.getReply());
  }

  private boolean validateGetPath(String path) {
    return path.startsWith("/items/store/") || path.startsWith("/items/top10/");
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");

    PurchaseRecord record = generateRecord(request);
    if (record == null) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().append("Invalid values provided.\n");
      return;
    }

    // add the record to the kafka topics
    Producer<String, String> producer = KafkaProducerSource.getProducer();
    if (producer == null) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().append("kafka producer is null\n");
      return;
    }
    producer.send(new ProducerRecord(POST_TOPIC, record.toJsonString()));

    response.getWriter().append("Insertion success.\n");
    response.setStatus(HttpServletResponse.SC_CREATED);
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

      // use jackson to convert body to a Purchase
      ObjectMapper objectMapper = new ObjectMapper();
      Purchase purchase = objectMapper.readValue(body, Purchase.class);

      // key is the itemID, value is the number of items
      HashMap<String, Integer> itemsMap = new HashMap<>();
      for (PurchaseItems purchaseItems1 : purchase.getItems()) {
        itemsMap.put(purchaseItems1.getItemID(), purchaseItems1.getNumberOfItems());
      }
      // generate a purchase record
      PurchaseRecord record = new PurchaseRecord(storeID, customerID, date, itemsMap);
      return record;
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }
}

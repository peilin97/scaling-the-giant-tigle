import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet(name = "SupermarketServlet", value = "/SupermarketServlet")
public class SupermarketServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
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
    // insert to the database
    SupermarketDao dao = new SupermarketDao();
    if (!dao.insertPurchase(record)) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.getWriter().append("Insertion failure.\n");
      return;
    }
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
      JSONObject bodyJson = new JSONObject(body);
      JSONArray itemsJson = bodyJson.getJSONArray("items");
      Map<String, Integer> itemsMap = new HashMap<>();
      for (int i = 0; i < itemsJson.length(); i++) {
        JSONObject itemJson = itemsJson.getJSONObject(i);
        String itemID = itemJson.getString("ItemID");
        int numberOfItems = itemJson.getInt("numberOfItems:");
        itemsMap.put(itemID, itemsMap.getOrDefault(itemID, 0) + numberOfItems);
      }
      // convert itemsMap to json
      ObjectMapper objectMapper = new ObjectMapper();
      String items = objectMapper.writeValueAsString(itemsMap);
      PurchaseRecord record = new PurchaseRecord(storeID, customerID, date, items);
      return record;
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }
}

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SupermarketDao {

  private static HikariDataSource ds;

  public SupermarketDao() {
    ds = DataSource.getDataSource();
  }

  public boolean insertPurchase(PurchaseRecord newPurchaseRecord) {
    PreparedStatement preparedStatement = null;
    String insertQueryStatement =
        "INSERT INTO purchaserecords (storeID, customerID, date, items) " +
            "VALUES (?,?,?,?)";
    boolean inserted = false;
    for (int i = 0; i < 3 && !inserted; i++) {
      inserted = true;
      try (Connection conn = ds.getConnection()) {
        preparedStatement = conn.prepareStatement(insertQueryStatement);
        preparedStatement.setInt(1, newPurchaseRecord.getStoreID());
        preparedStatement.setInt(2, newPurchaseRecord.getCustomerID());
        preparedStatement.setString(3, newPurchaseRecord.getDate());
//        preparedStatement.setString(4, newPurchaseRecord.getItemsJSON());
        // convert itemsMap to json
        ObjectMapper objectMapper = new ObjectMapper();
        String items = objectMapper.writeValueAsString(newPurchaseRecord.getItemsMap());
        preparedStatement.setString(4, items);
        preparedStatement.executeUpdate();
      } catch (SQLException throwables) {
        throwables.printStackTrace();
        inserted = false;
      } catch (JsonProcessingException e) {
        e.printStackTrace();
        inserted = false;
      } finally {
        try {
          if (preparedStatement != null) {
            preparedStatement.close();
          }
        } catch (SQLException se) {
          se.printStackTrace();
        }
      }
    }

    return inserted;
  }
}

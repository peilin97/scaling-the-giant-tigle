import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class PurchaseRecord implements Serializable {

  private String uuid;
  private int storeID;
  private int customerID;
  private String date;
  private HashMap<String, Integer> itemsMap;

  public PurchaseRecord() {
    super();
  }

  public PurchaseRecord(int storeID, int customerID, String date,
      HashMap<String, Integer> itemsMap) {
    this.uuid = UUID.randomUUID().toString();
    this.storeID = storeID;
    this.customerID = customerID;
    this.date = date;
    this.itemsMap = itemsMap;
  }

  public String getUuid() {
    return uuid;
  }

  public int getStoreID() {
    return storeID;
  }

  public void setStoreID(int storeID) {
    this.storeID = storeID;
  }

  public int getCustomerID() {
    return customerID;
  }

  public void setCustomerID(int customerID) {
    this.customerID = customerID;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public HashMap<String, Integer> getItemsMap() {
    return itemsMap;
  }

  public void setItemsMap(HashMap<String, Integer> itemsMap) {
    this.itemsMap = itemsMap;
  }

  @Override
  public String toString() {
    return "PurchaseRecord{" +
        "uuid='" + uuid + '\'' +
        ", storeID=" + storeID +
        ", customerID=" + customerID +
        ", date='" + date + '\'' +
        ", itemsMap=" + itemsMap +
        '}';
  }

  public String toJsonString() throws JsonProcessingException {
    ObjectMapper obj = new ObjectMapper();
    return obj.writeValueAsString(this);
  }
}

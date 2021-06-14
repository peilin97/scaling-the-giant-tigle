import java.io.Serializable;
import java.util.HashMap;

public class PurchaseRecord implements Serializable {

  private int storeID;
  private int customerID;
  private String date;
  private HashMap<String, Integer> itemsMap;

  public PurchaseRecord(int storeID, int customerID, String date,
      HashMap<String, Integer> itemsMap) {
    this.storeID = storeID;
    this.customerID = customerID;
    this.date = date;
    this.itemsMap = itemsMap;
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

}

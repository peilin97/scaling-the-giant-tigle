public class PurchaseRecord {

  private int storeID;
  private int customerID;
  private String date;
  private String items;

  public PurchaseRecord(int storeID, int customerID, String date, String items) {
    this.storeID = storeID;
    this.customerID = customerID;
    this.date = date;
    this.items = items;
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

  public String getItems() {
    return items;
  }

  public void setItems(String items) {
    this.items = items;
  }
}

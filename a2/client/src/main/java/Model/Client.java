package Model;

import java.util.Objects;

public class Client {

  private int maxStores;
  private int numCustomersPerStore;
  private int maxItemID;
  private int numPurchasesPerHour;
  private int numItemsPerPurchase;
  private String date;
  private String serverIP;

  /**
   * Constructs a Client with required maxStores and serverIP
   * @param maxStores maximum number of stores to simulate, required
   * @param serverIP IP/port address of the server, required
   */
  public Client(int maxStores, String serverIP) {
    this.maxStores = maxStores;
    this.numCustomersPerStore = 1000;
    this.maxItemID = 100000;
    this.numPurchasesPerHour = 300;
    this.numItemsPerPurchase = 5;
    this.date = "20210101";
    this.serverIP = serverIP;
  }

  public void setMaxStores(int maxStores) {
    this.maxStores = maxStores;
  }

  public void setNumCustomersPerStore(int numCustomersPerStore) {
    this.numCustomersPerStore = numCustomersPerStore;
  }

  public void setMaxItemID(int maxItemID) {
    this.maxItemID = maxItemID;
  }

  public void setNumPurchasesPerHour(int numPurchasesPerHour) {
    this.numPurchasesPerHour = numPurchasesPerHour;
  }

  public void setNumItemsPerPurchase(int numItemsPerPurchase) {
    this.numItemsPerPurchase = numItemsPerPurchase;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public void setServerIP(String serverIP) {
    this.serverIP = serverIP;
  }

  public int getMaxStores() {
    return maxStores;
  }

  public int getNumCustomersPerStore() {
    return numCustomersPerStore;
  }

  public int getMaxItemID() {
    return maxItemID;
  }

  public int getNumPurchasesPerHour() {
    return numPurchasesPerHour;
  }

  public int getNumItemsPerPurchase() {
    return numItemsPerPurchase;
  }

  public String getDate() {
    return date;
  }

  public String getServerIP() {
    return serverIP;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Client client = (Client) o;
    return getMaxStores() == client.getMaxStores() && getNumCustomersPerStore() == client
        .getNumCustomersPerStore() && getMaxItemID() == client.getMaxItemID()
        && getNumPurchasesPerHour() == client.getNumPurchasesPerHour()
        && getNumItemsPerPurchase() == client.getNumItemsPerPurchase() && getDate()
        .equals(client.getDate()) && getServerIP().equals(client.getServerIP());
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(getMaxStores(), getNumCustomersPerStore(), getMaxItemID(), getNumPurchasesPerHour(),
            getNumItemsPerPurchase(), getDate(), getServerIP());
  }

  @Override
  public String toString() {
    return "Client{" +
        "maxStores=" + maxStores +
        ", numCustomersPerStore=" + numCustomersPerStore +
        ", maxItemID=" + maxItemID +
        ", numPurchasesPerHour=" + numPurchasesPerHour +
        ", numItemsPerPurchase=" + numItemsPerPurchase +
        ", date='" + date + '\'' +
        ", serverIP='" + serverIP + '\'' +
        '}';
  }
}

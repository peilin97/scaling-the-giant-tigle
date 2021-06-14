package Model;

import io.swagger.client.api.PurchaseApi;
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
    this.numPurchasesPerHour = 60;
    this.numItemsPerPurchase = 5;
    this.date = "20210101";
    this.serverIP = serverIP;
  }

  /**
   * Constructs a Client instance with a builder
   * @param builder a client builder
   */
  public Client(Builder builder) {
    this.maxStores = builder.maxStores;
    this.numCustomersPerStore = builder.numCustomersPerStore;
    this.maxItemID = builder.maxItemID;
    this.numPurchasesPerHour = builder.numPurchasesPerHour;
    this.numItemsPerPurchase = builder.numItemsPerPurchase;
    this.date = builder.date;
    this.serverIP = builder.serverIP;
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

  public static class Builder {
    private int maxStores;
    private int numCustomersPerStore;
    private int maxItemID;
    private int numPurchasesPerHour;
    private int numItemsPerPurchase;
    private String date;
    private String serverIP;

    /**
     * Constructs a Builder with required maxStores and serverIP
     * @param maxStores maximum number of stores to simulate
     * @param serverIP IP/port address of the server
     */
    public Builder(int maxStores, String serverIP) {
      this.maxStores = maxStores;
      this.numCustomersPerStore = 1000;
      this.maxItemID = 100000;
      this.numPurchasesPerHour = 60;
      this.numItemsPerPurchase = 5;
      this.date = "20210101";
      this.serverIP = serverIP;
    }

    /**
     * Set the number of customers/store (default 1000)
     * @param numCustomersPerStore the number of customers/store
     * @return this builder
     */
    public Builder setNumCustomersPerStore(int numCustomersPerStore) {
      this.numCustomersPerStore = numCustomersPerStore;
      return this;
    }

    /**
     * Set the maximum itemID - default 100000
     * @param maxItemID the maximum itemID
     * @return this builder
     */
    public Builder setMaxItemID(int maxItemID) {
      this.maxItemID = maxItemID;
      return this;
    }

    /**
     * Set the number of purchases per hour: (default 60)
     * @param numPurchasesPerHour the number of purchases per hour
     * @return this builder
     */
    public Builder setNumPurchasesPerHour(int numPurchasesPerHour) {
      this.numPurchasesPerHour = numPurchasesPerHour;
      return this;
    }

    /**
     * Set the number of items for each purchase (range 1-20, default 5)
     * @param numItemsPerPurchase the number of items for each purchase (range 1-20, default 5)
     * @return this builder
     */
    public Builder setNumItemsPerPurchase(int numItemsPerPurchase) {
      this.numItemsPerPurchase = numItemsPerPurchase;
      return this;
    }

    /**
     * Set date
     * @param date date
     * @return this builder
     */
    public Builder setDate(String date) {
      this.date = date;
      return this;
    }

    /**
     * Build a Client
     * @return a Client
     */
    public Client build() {
      return new Client(this);
    }
  }

}

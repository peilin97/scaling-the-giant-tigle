package Model;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.PurchaseApi;
import io.swagger.client.model.Purchase;
import io.swagger.client.model.PurchaseItems;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Store implements Runnable {

  private CountDownLatch startPhase2;
  private CountDownLatch startPhase3;
  private int purchaseCount;
  private int upperBoundPurchaseCount;
  private int storeID;
  private int numCustomersPerStore;
  private int maxItemID;
  private int numPurchasesPerHour;
  private int numItemsPerPurchase;
  private String date;
  private String serverIP;
  private PurchaseApi purchaseApi;
  private AtomicInteger successfulCount;
  private AtomicInteger failedCount;

  /**
   * Constructs a Store
   *
   * @param startPhase2             a CountDownLatch to signal starting phase 2
   * @param startPhase3             a CountDownLatch to signal starting phase 3
   * @param purchaseCount           number of purchases has been made
   * @param upperBoundPurchaseCount number of purchases to close the store
   * @param storeID                 this Store's ID
   * @param numCustomersPerStore    the number of customers/store
   * @param maxItemID               the maximum itemID
   * @param numPurchasesPerHour     the number of purchases per hour
   * @param numItemsPerPurchase     the number of items for each purchase
   * @param date                    date, in format YYYYMMDD
   * @param serverIP                IP/port address of the server
   * @param successfulCount
   * @param failedCount
   */
  public Store(CountDownLatch startPhase2, CountDownLatch startPhase3,
      int purchaseCount, int upperBoundPurchaseCount, int storeID,
      int numCustomersPerStore, int maxItemID, int numPurchasesPerHour, int numItemsPerPurchase,
      String date, String serverIP, AtomicInteger successfulCount, AtomicInteger failedCount) {
    this.startPhase2 = startPhase2;
    this.startPhase3 = startPhase3;
    this.purchaseCount = purchaseCount;
    this.upperBoundPurchaseCount = upperBoundPurchaseCount;
    this.storeID = storeID;
    this.numCustomersPerStore = numCustomersPerStore;
    this.maxItemID = maxItemID;
    this.numPurchasesPerHour = numPurchasesPerHour;
    this.numItemsPerPurchase = numItemsPerPurchase;
    this.date = date;
    this.serverIP = serverIP;
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(serverIP);
    this.purchaseApi = new PurchaseApi(apiClient);
    this.successfulCount = successfulCount;
    this.failedCount = failedCount;
  }

  /**
   * Generate a random Purchase
   *
   * @return a Purchase
   */
  private Purchase generatePurchase() {
    Purchase body = new Purchase();
    for (int i = 0; i < numItemsPerPurchase; i++) {
      // 1. generate a PurchaseItems
      PurchaseItems purchaseItems = new PurchaseItems();
      purchaseItems.setItemID(generateItemID());
      purchaseItems.setNumberOfItems(1);
      // 2. add it to body
      body.addItemsItem(purchaseItems);
    }
    return body;
  }

  /**
   * Generate a random item ID; range: [1, maxItemID]
   *
   * @return a random item ID
   */
  private String generateItemID() {
    return Integer.toString((int) (Math.random() * maxItemID + 1));
  }

  /**
   * Generate a random customer ID; range: [1000*storeID+1, 1000*storeID+numCustomersPerStore]
   *
   * @return a random customer ID
   */
  private int generateCustID() {
    return (int) Math.random() * numCustomersPerStore + storeID * 1000 + 1;
  }

  @Override
  public void run() {
    while (purchaseCount < upperBoundPurchaseCount) {
      try {
        ApiResponse apiResponse = purchaseApi
            .newPurchaseWithHttpInfo(generatePurchase(), storeID, generateCustID(), date);
        purchaseCount++;
        successfulCount.getAndIncrement();
//        System.out.println("store " + storeID + " " + "purchase count " + purchaseCount);
        if (purchaseCount == numPurchasesPerHour * 3) {
          startPhase2.countDown();
        }
        if (purchaseCount == numPurchasesPerHour * 5) {
          startPhase3.countDown();
        }
      } catch (ApiException e) {
        System.err.println("Exception when calling PurchaseApi#newPurchase");
        System.err.append(e.getResponseBody()+System.lineSeparator());
//        System.err.append("Status code: " + e.getCode() + System.lineSeparator());
        e.printStackTrace();
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Store store = (Store) o;
    return purchaseCount == store.purchaseCount
        && upperBoundPurchaseCount == store.upperBoundPurchaseCount && storeID == store.storeID
        && numCustomersPerStore == store.numCustomersPerStore && maxItemID == store.maxItemID
        && numPurchasesPerHour == store.numPurchasesPerHour
        && numItemsPerPurchase == store.numItemsPerPurchase && startPhase2.equals(store.startPhase2)
        && startPhase3.equals(store.startPhase3) && date
        .equals(store.date) && serverIP.equals(store.serverIP) && purchaseApi
        .equals(store.purchaseApi);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(startPhase2, startPhase3, purchaseCount, upperBoundPurchaseCount,
            storeID,
            numCustomersPerStore, maxItemID, numPurchasesPerHour, numItemsPerPurchase, date,
            serverIP,
            purchaseApi);
  }

  @Override
  public String toString() {
    return "Store{" +
        "startPhase2=" + startPhase2 +
        ", startPhase3=" + startPhase3 +
        ", purchaseCount=" + purchaseCount +
        ", upperBoundPurchaseCount=" + upperBoundPurchaseCount +
        ", storeID=" + storeID +
        ", numCustomersPerStore=" + numCustomersPerStore +
        ", maxItemID=" + maxItemID +
        ", numPurchasesPerHour=" + numPurchasesPerHour +
        ", numItemsPerPurchase=" + numItemsPerPurchase +
        ", date='" + date + '\'' +
        ", serverIP='" + serverIP + '\'' +
        ", purchaseApi=" + purchaseApi +
        '}';
  }
}

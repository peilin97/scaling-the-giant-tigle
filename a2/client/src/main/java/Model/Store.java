package Model;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.PurchaseApi;
import io.swagger.client.model.Purchase;
import io.swagger.client.model.PurchaseItems;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

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
  private final BlockingQueue bq;
  private int successfulCount;
  private int failedCount;

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
   */
  public Store(CountDownLatch startPhase2, CountDownLatch startPhase3,
      int purchaseCount, int upperBoundPurchaseCount, int storeID,
      int numCustomersPerStore, int maxItemID, int numPurchasesPerHour, int numItemsPerPurchase,
      String date, String serverIP, BlockingQueue bq) {
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
    this.bq = bq;
    this.successfulCount = 0;
    this.failedCount = 0;
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

  private void sendPurchaseRequests() {
    while (purchaseCount < upperBoundPurchaseCount) {
      long requestEndTime = 0;
      int statusCode = 201;
      long requestStartTime = System.currentTimeMillis();
      try {
        purchaseApi.newPurchaseWithHttpInfo(
            generatePurchase(), storeID, generateCustID(), date);
        requestEndTime = System.currentTimeMillis();
        successfulCount++;
      } catch (ApiException e) {
        requestEndTime = System.currentTimeMillis();
        failedCount++;
        statusCode = e.getCode();
        System.err.println("Exception when calling PurchaseApi#newPurchase");
        System.err.append(e.getResponseBody() + System.lineSeparator());
        e.printStackTrace();
      }
      long latency = requestEndTime - requestStartTime;
      // write the record to the blocking queue
      try {
        bq.put(requestStartTime+","+latency+",POST,"+statusCode+System.lineSeparator());
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      purchaseCount++;
      if (purchaseCount == numPurchasesPerHour * 3) {
        startPhase2.countDown();
      }
      if (purchaseCount == numPurchasesPerHour * 5) {
        startPhase3.countDown();
      }
    }
  }

  @Override
  public void run() {
    sendPurchaseRequests();
  }

  public int getSuccessfulCount() {
    return successfulCount;
  }

  public int getFailedCount() {
    return failedCount;
  }
}

import Controller.ClientController;
import Model.Store;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

  public static void main(String[] args) throws IOException, InterruptedException {
    ClientController cc = new ClientController();
    CountDownLatch startPhase2 = new CountDownLatch(1);
    CountDownLatch startPhase3 = new CountDownLatch(1);
    Thread[] storeThreads = new Thread[cc.getMaxStores()];
    int quarterMaxStores = cc.getMaxStores()/4;
    AtomicInteger successfulCount = new AtomicInteger(0);
    AtomicInteger failedCount = new AtomicInteger(0);
    long start = System.currentTimeMillis();
    // phase 1
    for (int i=0; i<quarterMaxStores; i++) {
      Store store = new Store(startPhase2, startPhase3, 0,
      9*cc.getNumPurchasesPerHour(), i,
      cc.getNumCustomersPerStore(), cc.getMaxItemID(),
      cc.getNumPurchasesPerHour(), cc.getNumItemsPerPurchase(),
      cc.getDate(), cc.getServerIP(), successfulCount, failedCount);
      storeThreads[i] = new Thread(store);
      storeThreads[i].start();
    }
    startPhase2.await();
    // phase 2
    for (int i=0; i<quarterMaxStores; i++) {
      Store store = new Store(startPhase2, startPhase3, 3*cc.getNumPurchasesPerHour(),
          12*cc.getNumPurchasesPerHour(), i+quarterMaxStores,
          cc.getNumCustomersPerStore(), cc.getMaxItemID(),
          cc.getNumPurchasesPerHour(), cc.getNumItemsPerPurchase(),
          cc.getDate(), cc.getServerIP(), successfulCount, failedCount);
      storeThreads[i+quarterMaxStores] = new Thread(store);
      storeThreads[i+quarterMaxStores].start();
    }
    startPhase3.await();
    // phase 3
    for (int i=0; i<cc.getMaxStores()-2*quarterMaxStores; i++) {
      Store store = new Store(startPhase2, startPhase3, 5*cc.getNumPurchasesPerHour(),
          14*cc.getNumPurchasesPerHour(), i+2*quarterMaxStores,
          cc.getNumCustomersPerStore(), cc.getMaxItemID(),
          cc.getNumPurchasesPerHour(), cc.getNumItemsPerPurchase(),
          cc.getDate(), cc.getServerIP(), successfulCount, failedCount);
      storeThreads[i+2*quarterMaxStores] = new Thread(store);
      storeThreads[i+2*quarterMaxStores].start();
    }
    // wait for all threads to finish
    for (int i=0; i<storeThreads.length; i++) {
      storeThreads[i].join();
    }
    long end = System.currentTimeMillis();
    // print out the running result
    System.out.println("total number of successful requests sent: "+ successfulCount.intValue());
    System.out.println("total number of unsuccessful requests: "+ failedCount.intValue());
    System.out.println("the total run time (wall time) for all phases to complete: "+(double)(end-start)/1000+"s");
    System.out.println("throughput = requests per second = "+ (successfulCount.intValue()+failedCount.intValue())/((double)(end-start)/1000));
  }

}

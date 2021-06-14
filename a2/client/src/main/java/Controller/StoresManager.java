package Controller;

import Model.Client;
import Model.Store;
import View.ProgramView;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class StoresManager {

  private Client client;
  private CountDownLatch startPhase2;
  private CountDownLatch startPhase3;
  private Store[] stores;
  private Thread[] storeThreads;
  private final BlockingQueue bq;
  private List<Long> latencies;

  public StoresManager(Client client, BlockingQueue bq) {
    this.client = client;
    this.startPhase2 = new CountDownLatch(1);
    this.startPhase3 = new CountDownLatch(1);
    this.stores = new Store[client.getMaxStores()];
    this.storeThreads = new Thread[client.getMaxStores()];
    this.bq = bq;
    this.latencies = new ArrayList<>();
  }

  private void runPhase1(int storesSize) {
    for (int i=0; i<storesSize; i++) {
      stores[i] = new Store(startPhase2, startPhase3, 0,
          9*client.getNumPurchasesPerHour(), i,
          client.getNumCustomersPerStore(), client.getMaxItemID(),
          client.getNumPurchasesPerHour(), client.getNumItemsPerPurchase(),
          client.getDate(), client.getServerIP(), bq);
      storeThreads[i] = new Thread(stores[i]);
      storeThreads[i].start();
    }
  }

  private void runPhase2(int storesSize) {
    for (int i=0; i<storesSize; i++) {
      stores[i+storesSize] = new Store(startPhase2, startPhase3, 3*client.getNumPurchasesPerHour(),
          12*client.getNumPurchasesPerHour(), i+storesSize,
          client.getNumCustomersPerStore(), client.getMaxItemID(),
          client.getNumPurchasesPerHour(), client.getNumItemsPerPurchase(),
          client.getDate(), client.getServerIP(), bq);
      storeThreads[i+storesSize] = new Thread(stores[i+storesSize]);
      storeThreads[i+storesSize].start();
    }
  }

  private void runPhase3(int openedStores, int leftStores) {
    for (int i=0; i<leftStores; i++) {
      stores[i+openedStores] = new Store(startPhase2, startPhase3, 5*client.getNumPurchasesPerHour(),
          14*client.getNumPurchasesPerHour(), i+openedStores,
          client.getNumCustomersPerStore(), client.getMaxItemID(),
          client.getNumPurchasesPerHour(), client.getNumItemsPerPurchase(),
          client.getDate(), client.getServerIP(), bq);
      storeThreads[i+openedStores] = new Thread(stores[i+openedStores]);
      storeThreads[i+openedStores].start();
    }
  }

  private void waitStoresClose() throws InterruptedException {
    for (int i=0; i<storeThreads.length; i++) {
      storeThreads[i].join();
    }
  }

  private void updateData(long start, long end) {
    double wallTime = (double)(end-start)/1000;
    int totalSuccessfulCount = 0;
    int totalFailedCount = 0;
    for (Store store: stores) {
      totalSuccessfulCount += store.getSuccessfulCount();
      totalFailedCount += store.getFailedCount();
    }
    double throughput = (totalSuccessfulCount+totalFailedCount)/wallTime;

    // read latencies from the csv
    String csvFile = "request-records.csv";
    try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
      reader.readLine();  // skip the header
      String line= null;
      while ((line=reader.readLine()) != null) {
        latencies.add(Long.valueOf(line.split(",")[1]));
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    int n = latencies.size();
    Collections.sort(latencies);
    double mean = calculateMeanResponseTime(latencies);
    double median = calculateMedianResponseTime(latencies);
    long p99 = calculateP99ResponseTime(latencies);
    long maxLatency = latencies.get(n-1);
    ProgramView.printProgramOutput(totalSuccessfulCount, totalFailedCount, wallTime, throughput,
        mean, median, p99, maxLatency);
  }

  private long calculateP99ResponseTime(List<Long> latencies) {
    int n = latencies.size();
    return latencies.get(n/100*99);
  }

  private double calculateMedianResponseTime(List<Long> latencies) {
    int n = latencies.size();
    return (n%2==1)
        ? latencies.get(n/2)
        : (double) latencies.get(n/2-1)+(latencies.get(n/2)-latencies.get(n/2-1))/2;
  }

  private double calculateMeanResponseTime(List<Long> latencies) {
    long sum = 0;
    for (int i=0; i<latencies.size(); i++) {
      sum += latencies.get(i);
    }
    return (double) sum/latencies.size();
  }

  public void execute() throws InterruptedException {
    int quarterMaxStores = client.getMaxStores()/4;

    // write records to a csv
    String csvFile = "request-records.csv";
    RecordsProcessor recordsProcessor = new RecordsProcessor(csvFile, bq);
    Thread consumerTh = new Thread(recordsProcessor);
    consumerTh.start();

    // start running stores
    long start = System.currentTimeMillis();
    runPhase1(quarterMaxStores);
    startPhase2.await();
    runPhase2(quarterMaxStores);
    startPhase3.await();
    runPhase3(quarterMaxStores*2, client.getMaxStores()-quarterMaxStores*2);
    waitStoresClose();
    long end = System.currentTimeMillis();
    bq.put("exit");  // signal to terminate the consumer
    consumerTh.join();
    updateData(start, end);
  }

  public Client getClient() {
    return client;
  }

  public CountDownLatch getStartPhase2() {
    return startPhase2;
  }

  public CountDownLatch getStartPhase3() {
    return startPhase3;
  }

  public Store[] getStores() {
    return stores;
  }

  public Thread[] getStoreThreads() {
    return storeThreads;
  }
}

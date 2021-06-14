public class Main {

  public static void main(String[] args) {
    Thread[] consumers = new Thread[15];
    for (Thread consumer : consumers) {
      consumer = new Thread(new ConsumerRunner());
      consumer.start();
    }
  }

}

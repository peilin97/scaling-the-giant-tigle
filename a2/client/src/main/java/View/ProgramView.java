package View;

public class ProgramView {

  public static void printProgramOutput(int successfulCount, int failedCount, double wallTime,
      double throughput, double meanResponseTime, double medianResponseTime, long p99ResponseTime,
      long maxResponseTime) {
    System.out.println("total number of successful requests sent: " + successfulCount);
    System.out.println("total number of unsuccessful requests: " + failedCount);
    System.out.println(
        "the total run time (wall time) for all phases to complete (seconds): " + wallTime);
    System.out.println("throughput = requests per second = " + throughput);
    System.out.println("mean response time for POSTs (millisecs): " + meanResponseTime);
    System.out.println("median response time for POSTs (millisecs): " + medianResponseTime);
    System.out
        .println("p99 (99th percentile) response time for POSTs (millisecs): " + p99ResponseTime);
    System.out.println("max response time for POSTs (millisecs): " + maxResponseTime);
  }
}

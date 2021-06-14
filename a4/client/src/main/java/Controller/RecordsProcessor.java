package Controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class RecordsProcessor implements Runnable {

  private static final String HEADER =
      "start time,latency,request type,response code" + System.lineSeparator();
  private final BlockingQueue bq;
  private String filePath;

  public RecordsProcessor(String filePath, BlockingQueue bq) {
    this.filePath = filePath;
    this.bq = bq;
  }

  @Override
  public void run() {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
      writer.write(HEADER);
      String line = null;
      while (!(line = (String) bq.take()).equals("exit")) {
        writer.write(line);
      }
      writer.flush();
    } catch (InterruptedException interruptedException) {
      interruptedException.printStackTrace();
    } catch (IOException ioException) {
      ioException.printStackTrace();
    }
  }
}


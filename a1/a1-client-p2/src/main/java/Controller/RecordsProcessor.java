package Controller;

import Model.RequestRecord;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class RecordsProcessor {

  private static final String HEADER =
      "start time,latency,request type,response code" + System.lineSeparator();

  public static void convertToCSV(String filePath, List<RequestRecord> records) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
      writer.write(HEADER);
      for (RequestRecord record : records) {
        String line =
            record.getStartTime() + "," + record.getLatency() + "," + record.getRequestType() + ","
                + record.getResponseCode() + System.lineSeparator();
        writer.write(line);
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}

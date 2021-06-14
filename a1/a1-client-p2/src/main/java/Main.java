import Controller.ClientController;
import Controller.RecordsProcessor;
import Controller.StoresManager;
import java.io.IOException;

public class Main {

  public static void main(String[] args) throws IOException, InterruptedException {
    ClientController cc = new ClientController();
    StoresManager storesManager = new StoresManager(cc.getClient());
    storesManager.execute();
    // write records to a csv
    String csvFile = "request-records.csv";
    RecordsProcessor.convertToCSV(csvFile, storesManager.getRecords());
  }

}

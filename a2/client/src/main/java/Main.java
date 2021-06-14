import Controller.ClientController;
import Controller.StoresManager;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

  public static void main(String[] args) throws IOException, InterruptedException {
    ClientController cc = new ClientController();
    BlockingQueue bq = new LinkedBlockingQueue();
    StoresManager storesManager = new StoresManager(cc.getClient(), bq);
    storesManager.execute();
  }

}

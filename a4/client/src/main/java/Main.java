import Controller.StoresManager;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

  public static void main(String[] args) throws IOException, InterruptedException {
    BlockingQueue bq = new LinkedBlockingQueue();
    StoresManager storesManager = new StoresManager(args, bq);
    storesManager.execute();
  }

}

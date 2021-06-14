package Controller;

import Model.Client;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.Properties;

public class ClientController {

  private Client client;

  /**
   * Generates a client controller
   *
   * @throws IOException if the parameter file has errors
   */
  public ClientController() throws IOException {
    this.client = generateClient();
  }

  public Client getClient() {
    return client;
  }

  public int getMaxStores() {
    return client.getMaxStores();
  }

  public int getNumCustomersPerStore() {
    return client.getNumCustomersPerStore();
  }

  public int getMaxItemID() {
    return client.getMaxItemID();
  }

  public int getNumPurchasesPerHour() {
    return client.getNumPurchasesPerHour();
  }

  public int getNumItemsPerPurchase() {
    return client.getNumItemsPerPurchase();
  }

  public String getDate() {
    return client.getDate();
  }

  public String getServerIP() {
    return client.getServerIP();
  }

  /**
   * Generates a Client with parameters set in config.properties
   *
   * @return a Client if all parameters are valid
   * @throws IOException
   */
  private Client generateClient() throws IOException {
    // https://crunchify.com/java-properties-file-how-to-read-config-properties-values-in-java/
    InputStream is = null;
    Properties props = new Properties();
    try {
      String propFileName = "config.properties";
      is = getClass().getClassLoader().getResourceAsStream(propFileName);
      if (is != null) {
        props.load(is);
      } else {
        throw new FileNotFoundException(
            "property file '" + propFileName + "' not found in the classpath");
      }
    } catch (Exception e) {
      System.err.println(e);
    } finally {
      is.close();
    }
    int maxStores = 0;
    String serverIP = "";
    if (props.getProperty("maxStores") != null && props.getProperty("serverIP") != null) {
      maxStores = Integer.parseInt(props.getProperty("maxStores"));
      serverIP = props.getProperty("serverIP");
    } else {
      throw new IllegalArgumentException("The maxStores and serverIP are required");
    }
    if (maxStores <= 0) {
      throw new IllegalArgumentException("The maxStores must be larger than 0");
    }
    if (!isValidIP(serverIP)) {
      throw new InvalidParameterException("The serverIP is invalid");
    }
    Client client = new Client(maxStores, serverIP);
    // check if props contains optional parameters
    if (props.getProperty("numCustomersPerStore") != null) {
      int numCustomersPerStore = Integer.parseInt(props.getProperty("numCustomersPerStore"));
      if (numCustomersPerStore > 0) {
        client.setNumCustomersPerStore(numCustomersPerStore);
      }
    }
    if (props.getProperty("maxItemID") != null) {
      int maxItemID = Integer.parseInt(props.getProperty("maxItemID"));
      if (maxItemID > 0) {
        client.setMaxItemID(maxItemID);
      }
    }
    if (props.getProperty("numPurchasesPerHour") != null) {
      int numPurchasesPerHour = Integer.parseInt(props.getProperty("numPurchasesPerHour"));
      if (numPurchasesPerHour > 0) {
        client.setNumPurchasesPerHour(numPurchasesPerHour);
      }
    }
    if (props.getProperty("numItemsPerPurchase") != null) {
      int numItemsPerPurchase = Integer.parseInt(props.getProperty("numItemsPerPurchase"));
      if (numItemsPerPurchase > 0 && numItemsPerPurchase <= 20) {
        client.setNumItemsPerPurchase(numItemsPerPurchase);
      }
    }
    if (props.getProperty("date") != null) {
      String date = props.getProperty("date");
      client.setDate(date);
    }

    return client;
  }

  /**
   * A simple check for the input IP
   *
   * @param ip the serverIP
   * @return true if it is valid, false otherwise
   * @throws IOException
   */
  private boolean isValidIP(String ip) throws IOException {
    URL url = new URL(ip);
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    int status = con.getResponseCode();
    con.disconnect();
    return status == 200;
  }
}

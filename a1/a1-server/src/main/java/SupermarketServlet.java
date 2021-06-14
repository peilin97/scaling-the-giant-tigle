import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "SupermarketServlet", value = "/SupermarketServlet")
public class SupermarketServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("text/html");  // Set content type of the response so that jQuery knows what it can expect.
    response.setCharacterEncoding("UTF-8"); // You want world domination, huh?
    // validate the uri
    String uri = request.getRequestURI();
    if (!checkPurchaseURI(uri)) {
      response.sendError(404, "Invalid Data");
      return;
    }
    response.setStatus(HttpServletResponse.SC_CREATED);
  }

  private boolean checkPurchaseURI(String uri) {
    // remove prefix and suffix "/"
    while (uri.startsWith("/"))
      uri = uri.substring(1);
    while (uri.endsWith("/"))
      uri = uri.substring(0, uri.length()-1);
    String[] paths = uri.split("/+");
    // 1. length check
    if (paths.length != 8) return false;
    // paths[0]="a1_war_exploded", paths[1]="supermarket"
    // start check from paths[3]
    if (paths[2].compareTo("purchase") != 0 || paths[4].compareTo("customer") != 0 || paths[6].compareTo("date") != 0)
      return false;
    // check storeID, customerID and date
    try {
      Integer.valueOf(paths[3]);
      Integer.valueOf(paths[5]);
      Integer.valueOf(paths[7]);
    } catch (Exception e){
      return false;
    }
    // check date
    if (paths[7].length() != 8) return false;
    return true;
  }
}

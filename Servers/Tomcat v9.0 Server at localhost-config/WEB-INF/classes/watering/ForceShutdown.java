package watering;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

@WebServlet("/ForceShutdown")
public class ForceShutdown extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(ForceShutdown.class);
	View View = new View();
	WateringScheduler wateringscheduler = new WateringScheduler();
	DeleteFromDB  DeleteFromDB= new DeleteFromDB ();

	public void closeWaterNow() throws SQLException {
		wateringscheduler.stop();
		View.disablePin();
		log.info("Pin Closed");
		DeleteFromDB.deleteFromDatabaseΝow();		
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		try {
			closeWaterNow();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		out.println("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\">");
		out.println("<link rel=\"stylesheet\" href=\"graphic.css\" type=\"text/css\">");
		out.println("</head><body>");
		out.println("<div class=\"responsed\" align=\"center\">");
		out.println("<h3>Το πότισμα σταμάτησε.</h3>");
		out.println("<h3>Θα μεταβείτε στην αρχική σελίδα αυτόματα.</h3>");
		out.println("</div></body></html>");
		response.setHeader("Refresh", "3;url=./");
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doPost(request, response);
	}
}
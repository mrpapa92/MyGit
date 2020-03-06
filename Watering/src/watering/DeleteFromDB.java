package watering;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.mail.internet.AddressException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

@WebServlet("/DeleteFromDB")
public class DeleteFromDB extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(DeleteFromDB.class);
	private static Connection conn;
	static DBConnection DBConnection = new DBConnection();
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		String day = request.getParameter("day");
		String hourFrom = request.getParameter("hourFrom");
		String hourTo = request.getParameter("hourTo");
		String status = request.getParameter("status");
		out.println("<html><head>");
		out.println("<link rel=\"stylesheet\" href=\"graphic.css\" type=\"text/css\">");
		out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		out.println("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\">");
		out.println("</head><body>");
		out.println("<div class=\"responsed\" align=\"center\">");
		out.println("</div></body></html>");
		response.setHeader("Refresh", "0;url=./ReturnDB?");

		try {
			delete(Integer.parseInt(day), Integer.parseInt(hourFrom), Double.parseDouble(hourTo), status);
		} catch (NumberFormatException | SQLException | AddressException e) {
			e.printStackTrace();
		}
	}
	public void delete(int day, int hourFrom, double hourTo, String status)
			throws SQLException, AddressException {
		if (status.equals("on")) {
			ForceShutdown ForceShutdown = new ForceShutdown();
			ForceShutdown.closeWaterNow();
		} else if (status.equals("off")) {
			deleteFromDatabase(day, hourFrom, hourTo, status);
		}
	}
	
	public void deleteFromDatabaseNow() throws SQLException {
		conn = DBConnection.createDBConnection();
		log.info("Watering With Status ON deleted");
		String status = "on";
		PreparedStatement st = conn.prepareStatement("DELETE FROM wateringtable WHERE Status = ?");
		st.setString(1, status);
		st.executeUpdate();
		DBConnection.closeConnection(conn);
	}
	
	public void deleteFromDatabase(int day, int hourFrom, double hourTo, String status) throws SQLException {
		conn = DBConnection.createDBConnection();
		log.info("Deleting Insertion: Day: " + day + " hourFrom: " + hourFrom + " hourTo: " + hourTo + " Status: " + status);
		PreparedStatement st = conn
				.prepareStatement("DELETE FROM wateringtable WHERE Day = ? AND hourFrom = ? AND hourTo = ?  AND Status = ?");
		st.setInt(1, day);
		st.setInt(2, hourFrom);
		st.setDouble(3, hourTo);
		st.setString(4, status);
		st.executeUpdate();
		DBConnection.closeConnection(conn);
	}
}
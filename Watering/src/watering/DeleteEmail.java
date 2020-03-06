package watering;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

@WebServlet("/DeleteEmail")
public class DeleteEmail extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(DeleteEmail.class);
	private static Connection conn;
	static DBConnection DBConnection = new DBConnection();

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		out.println("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\">");
		out.println("<link rel=\"stylesheet\" href=\"graphic.css\" type=\"text/css\">");
		out.println("</head><body>");
		out.println("<div class=\"responsed\" align=\"center\">");

		try {
			conn = DBConnection.createDBConnection();
			String query = "SELECT * FROM emailT";
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			if (rs.next() == false) {
				out.println("<h3>Δεν υπάρχουν εγγεραμμένα email.</h3>");
				log.info("No emails exist");
				response.setHeader("Refresh", "4;url=./");
			} else {
				out.println("<center><h3>Εγγεγραμμένα Email</h3>");
				out.println("<table id=\"waterings\" border=\"2\">");
				out.println("<tr class=\"scheduleTH\">");
				out.println("<td>Email</td>");
				out.println("</tr><br><br>");
				log.info("Listing emails");
				do {
					out.println("<tr>");
					out.println("<td>" + rs.getString("email") + "</td>");
					out.println("<form  method =\"post\" action=\"DeleteEmail\">");
					out.println("<input type=\"hidden\" name=\"email\" value=" + rs.getString("email") + " />");
					out.println("<td><button class=\"rmbutton\" id=\"stop\"><b>X</b></button></form></td>");
					out.println("</tr>");
				} while (rs.next());
				out.println("</table>");
			}
			DBConnection.closeConnection(conn);
			out.println("<a href=\"./\"><button>Επιστροφή</button></a>");
			out.println("</div></body></html>");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		String email = request.getParameter("email");
		out.println("<html><head>");
		out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		out.println("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\">");
		out.println("<link rel=\"stylesheet\" href=\"graphic.css\" type=\"text/css\">");
		out.println("</head><body>");
		out.println("<div class=\"responsed\" align=\"center\">");
		out.println("</div></body></html>");
		response.setHeader("Refresh", "0;url=./DeleteEmail?");
		try {
			conn = DBConnection.createDBConnection();
			log.info("Deleting: "+ email);
			String query = " DELETE FROM emailT WHERE email = ?";
			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, email);
			preparedStmt.execute();
			DBConnection.closeConnection(conn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
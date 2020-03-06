package watering;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

@WebServlet("/ReturnDB")
public class ReturnDB extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(ReturnDB.class);
	private static Connection conn = null;
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
			log.info("Get watering insesrtions");
			String query = "SELECT * FROM wateringtable";
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			Subscribe sb = new Subscribe();
			if (rs.next() == false) {
				out.println("<center><h3>Δεν υπάρχουν προγραμματισμένα ποτίσματα.</h3>");
				response.setHeader("Refresh", "3;url=./");
			} else {
				out.println("<center><h3>Προγραμματισμένα Ποτίσματα</h3>");
				out.println(
						"<iframe src=\"http://free.timeanddate.com/clock/i6igvaxw/n26/tlgr17/fn6/fs16/fcfff/tct/pct/ftb/tt0/tm1/td1/th1/tb4\" frameborder=\"0\" width=\"199\" height=\"42\" ></iframe>");
				out.println("<table id=\"waterings\" border=\"2\">");
				out.println("<tr class=\"scheduleTH\">");
				out.println("<td>Ημέρα</td>");
				out.println("<td>Έναρξη</td>");
				out.println("<td>Διάρκεια (Λεπτά)</td>");
				out.println("<td>Κατάσταση</td>");
				out.println("</tr><br><br>");
				do {
					Calendar c = Calendar.getInstance();
					Calendar cal = Calendar.getInstance();
					String pattern = "HH:mm";
					c.set(Calendar.HOUR_OF_DAY, rs.getInt("hourFrom"));
					c.set(Calendar.MINUTE, rs.getInt("hourFromMin"));
					Date d = c.getTime();
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
					String output = simpleDateFormat.format(d);
					String color = null;
					if (rs.getString("status").equals("on")) {
						color = "#DAA520";// Color gold
					}
					out.println("<tr style=\"color: " + color + ";\">");
					out.println("<td>");
					if ((rs.getInt("day") == cal.get(Calendar.DAY_OF_WEEK)
							&& cal.get(Calendar.HOUR_OF_DAY) <= rs.getInt("hourFrom"))
							|| rs.getString("status").equals("on")) {
						out.println("Σήμερα");
					} else {
						out.println(sb.renameDays(rs.getInt("day")));
						out.println("</td>");
					}
					out.println("<td>" + output + "</td>");
					out.println("<td>" + rs.getInt("Duration") + "</td>");
					out.println("<td>");
					if (rs.getString("status").equals("on")) {
						out.println("Ενεργό");
					} else {
						out.println("Άνενεργό");
					}
					out.println("</td>");
					out.println("<form  method =\"GET\" action=\"DeleteFromDB\">");
					out.println("<input type=\"hidden\" name=\"hourFrom\" value=" + rs.getInt("hourFrom") + " />");
					out.println("<input type=\"hidden\" name=\"day\" value=" + rs.getInt("day") + " />");
					out.println("<input type=\"hidden\" name=\"hourTo\" value=" + rs.getDouble("hourTo") + " />");
					out.println("<input type=\"hidden\" name=\"status\" value=" + rs.getString("status") + " />");
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
		doGet(request, response);
	}
}

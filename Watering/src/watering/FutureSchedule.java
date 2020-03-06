package watering;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.apache.log4j.Logger;

@WebServlet("/FutureSchedule")
public class FutureSchedule extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// private static Logger log = Logger.getLogger(FutureSchedule.class);
	Subscribe Subscribe = new Subscribe();

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		String hourFrom = request.getParameter("hourFrom");
		String day = request.getParameter("day");
		String duration = request.getParameter("duration");
		String hourTo = request.getParameter("hourTo");
		String status = request.getParameter("status");

		out.println("<html><head>");
		out.println("<link rel=\"stylesheet\" href=\"graphic.css\" type=\"text/css\">");
		out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		out.println("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\">");
		out.println("</head>");
		out.println("<body>");
		out.println("<div class=\"responsed\" align=\"center\">");
		out.println("<p align=\"CENTER\">"
				+ toPrint(Integer.parseInt(hourFrom), Integer.parseInt(duration), Integer.parseInt(day), status));
		out.println("<a href=\"./\"><button>Επιστροφή</button></a><br>");
		out.println("<a href=\"DeleteFromDB?hourFrom=" + hourFrom + "&day=" + day + "&hourTo=" + hourTo + "&status="
				+ status + "\"><button id=\"stop\">Ακύρωση</button></a><br><br><br>");
		out.println("<a href=\"ReturnDB\"\"><button>Προγγραματισμένα ποτίσματα</button></a> ");
		out.println("</div></body></html>");
		response.setHeader("Refresh", "5;url=./");
	}

	public String toPrint(int hourFrom, int duration, int day, String status) {
		Calendar c = Calendar.getInstance();

		if (c.get(Calendar.DAY_OF_WEEK) == day && c.get(Calendar.HOUR_OF_DAY) <= hourFrom) {
			if (hourFrom % 10 == hourFrom) {
				if (duration == 1) {
					return ("<h2>Σήμερα!\n" + " Ωρα έναρξης: " + hourFrom + ":00. Διάρκεια ποτίσματος : " + duration
							+ " λεπτό.</h2>");
				} else {
					return ("<h2>Σήμερα!\n" + " Ωρα έναρξης: " + hourFrom + ":00. Διάρκεια ποτίσματος : " + duration
							+ " λεπτά.</h2>");
				}
			} else {
				if (duration == 1) {
					return ("<h2>Σήμερα!\n" + " Ωρα έναρξης: " + hourFrom + ":00. Διάρκεια ποτίσματος : " + duration
							+ " λεπτό.</h2>");
				} else {
					return ("<h2>Σήμερα!\n" + " Ωρα έναρξης: " + hourFrom + ":00. Διάρκεια ποτίσματος : " + duration
							+ " λεπτά.</h2>");
				}
			}
		} else {
			if ((hourFrom % 10) == hourFrom) {
				if (duration == 1) {
					return ("<h2>Το πότισμα θα ξεκινήσει τη μέρα " + Subscribe.renameDays(day) + " και ώρα 0" + hourFrom
							+ ":00. Διάρκεια ποτίσματος : " + duration + " λεπτό.</h2>");
				} else {
					return ("<h2>Το πότισμα θα ξεκινήσει τη μέρα " + Subscribe.renameDays(day) + " και ώρα 0" + hourFrom
							+ ":00. Διάρκεια ποτίσματος : " + duration + " λεπτά. </h2>");
				}
			} else {
				if (duration == 1) {
					return ("<h2>Το πότισμα θα ξεκινήσει τη μέρα " + Subscribe.renameDays(day) + " και ώρα " + hourFrom
							+ ":00. Διάρκεια ποτίσματος : " + duration + " λεπτό.</h2>");
				} else {
					return ("<h2>Το πότισμα θα ξεκινήσει τη μέρα " + Subscribe.renameDays(day) + " και ώρα " + hourFrom
							+ ":00. Διάρκεια ποτίσματος : " + duration + " λεπτά.</h2>");
				}
			}
		}
	}
}

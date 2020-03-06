package watering;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.log4j.Logger;

@WebServlet("/Subscribe")
public class Subscribe extends HttpServlet implements GetSameComponents {
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(Subscribe.class);
	private static final String provider = "smtp.office365.com";
	private static final int port = 587;
	Connection conn;
	static DBConnection DBConnection = new DBConnection();

	public String renameDays(int i) {
		String dayName = null;
		switch (i) {
		case 1:
			dayName = "Κυριακή";
			break;
		case 2:
			dayName = "Δευτέρα";
			break;
		case 3:
			dayName = "Τρίτη";
			break;
		case 4:
			dayName = "Τετάρτη";
			break;
		case 5:
			dayName = "Πέμπτη";
			break;
		case 6:
			dayName = "Παρασκευή";
			break;
		case 7:
			dayName = "Σάββατο";
			break;
		}
		return dayName;
	}

	public void sendEmailDaily(String text, int flag) throws AddressException {
		String to;
		String fin = "\n\n\nAυτό είναι ένα αυτόματο μήνυμα. Παρακαλώ μην απαντήσετε σε αυτό το μήνυμα ηλεκτρονικού ταχυδρομείου.";

		try {
			to = EmailDB();
			log.info("Email clients : " + to + to.length());
			if (to.length() > 0) {
				InternetAddress[] parse = InternetAddress.parse(to, true);
				String password = "W@t3rB0t";
				String from = "wateringbot@outlook.com";
				Properties props = System.getProperties();
				props.put("mail.smtp.starttls.enable", "true");
				props.put("mail.smtp.auth", "true");
				props.put("mail.smtp.host", provider);
				props.put("mail.smtp.port", port);

				Session session = Session.getDefaultInstance(props, new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(from, password);
					}
				});
				MimeMessage message = new MimeMessage(session);
				message.setFrom(new InternetAddress(from));

				switch (flag) {
				case 1:
					message.setRecipients(javax.mail.Message.RecipientType.TO, parse);
					message.setSubject("[Watering Bot] Ένα πότισμα είναι έτοιμο να ξεκινήσει.", "UTF-8");
					message.setText(text + fin, "UTF-8");
					break;
				case 2:
					message.setRecipients(javax.mail.Message.RecipientType.TO, parse);
					message.setSubject("[Watering Bot] Ένα πότισμα ολοκληρώθηκε με επιτυχία.", "UTF-8");
					message.setText(text + fin, "UTF-8");
					break;
				case 3:
					message.setRecipients(javax.mail.Message.RecipientType.TO, parse);
					message.setSubject("[Watering Bot] Ένα πότισμα διακόπηκε.", "UTF-8");
					message.setText(text + fin, "UTF-8");
					break;
				case 4:
					message.setSubject("[Watering Bot] Επιτυχής Εγγραφή", "UTF-8");
					message.setText(
							"Εγγραφήκατε με επιτυχία στη λίστα μας.\n Μπορείτε να καταργήσετε την εγγραφή σας πατώντας το κουμπί κατάργηση εγγραφής στο site."
									+ fin,
							"UTF-8");
					message.setRecipients(javax.mail.Message.RecipientType.TO, text);
					break;
				}
				Transport.send(message);
				log.info("Sent message successfully....");
			} else {
				log.info("No recipients");
			}
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException
				| MessagingException e) {
			e.printStackTrace();
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String email = request.getParameter("email");
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		int checkEx = returnEmailResults(email);
		out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		out.println("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\">");
		out.println("<link rel=\"stylesheet\" href=\"graphic.css\" type=\"text/css\">");
		out.println("</head><body>");
		out.println("<div class=\"responsed\" align=\"center\">");

		if (checkEx == 1) {
			out.println("<h3>To email υπάρχει ήδη.</h3>");
		} else if (checkEx == 2) {
			try {
				sendEmailDaily(email, 4);
				out.println("<h3>Η εγγραφή σας δημιουργήθηκε με επιτυχία. Θα λαμβάνεται ενημερώσεις.</h3>");
			} catch (AddressException e) {
				e.printStackTrace();
			}
		} else {
			out.println("<h3>Μη έγκυρη διέυθυνση ηλεκτρονικού ταχυδρομείου.Παρακαλώ προσπαθήστε ξανά.</h3>");
		}
		out.println("<h3>Θα μεταβείτε στην αρχική σελίδα αυτόματα.</h3>");
		out.println("<a href=\"./\"><button>Επιστροφή</button></a>");
		out.println("</div></body></html>");
		response.setHeader("Refresh", "5;url=./");
	}

	public int returnEmailResults(String email) {

		try {
			conn = DBConnection.createDBConnection();
			boolean check = checkIFexists(email);
			boolean valid = EmailValidator.getInstance().isValid(email);
			
			Calendar calendar = Calendar.getInstance();
			java.sql.Date startDate = new java.sql.Date(calendar.getTime().getTime());

			if (valid) {
				if (!check) {
					String query = " insert into emailT (email,Date)" + " values (?, ?)";
					PreparedStatement preparedStmt = conn.prepareStatement(query);
					preparedStmt.setString(1, email);
					preparedStmt.setDate(2, startDate);
					preparedStmt.execute();
					log.error("Email inserted in database");
					DBConnection.closeConnection(conn);
					return 2;
				} else {
					
					return 1;
				}
			} else {
				log.error("Invalid email address");
				DBConnection.closeConnection(conn);
				return 3;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public boolean checkIFexists(String email)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		String query = "SELECT * FROM emailT";
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery(query);

		while (rs.next()) {
			if (email.equals(rs.getString("email"))) {
				log.info("Email already exists");
				DBConnection.closeConnection(conn);
				return true;
			}
		}
		
		return false;
	}

	public String EmailDB()
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		conn = DBConnection.createDBConnection();
		log.info("Email list entered");
		String query = "SELECT * FROM emailT";
		Statement st = conn.createStatement();
		String result = "";
		ResultSet rs = st.executeQuery(query);

		while (rs.next()) {
			String email = rs.getString("email");
			result += email + ", ";
		}
		DBConnection.closeConnection(conn);
		return result;
	}
}
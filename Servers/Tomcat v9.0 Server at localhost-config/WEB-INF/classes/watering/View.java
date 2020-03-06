package watering;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import javax.mail.internet.AddressException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

@WebServlet("/timer")

public class View extends HttpServlet implements Job {
	private static final long serialVersionUID = 1L;
	static GpioController gpio;
	static GpioPinDigitalOutput pina;
	private static Connection conn = null;
	static DBConnection DBConnection = new DBConnection();
	private static Logger log = Logger.getLogger(View.class);
	static SchedulerFactory CronchedulerFactory = new StdSchedulerFactory();
	static Scheduler CronScheduler;

	public void init(ServletConfig config) throws ServletException {
		log.info("Server Initialized");
		try {
			initialiseGPIO();
			startScheduleJobForDBCheck();
		} catch (SchedulerException e1) {
			e1.printStackTrace();
		}
	}

	public void destroy() {
		DeleteFromDB DeleteFromDB = new DeleteFromDB  ();
		try {
			gpio.unprovisionPin(pina);
			stopScheduler();
			WateringScheduler watS = new WateringScheduler();
			watS.stop();
			pina.low();
			DeleteFromDB.deleteFromDatabaseΝow();
		} catch (SchedulerException e) {
			log.error(e.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int initialiseGPIO() {
		if (pina == null) {
			gpio = GpioFactory.getInstance();
			pina = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "MyLED", PinState.LOW);
			return 1;
		}
		return 0;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (pina.isHigh()) {
			RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/html/indexON.html");
			rd.include(request, response);
		} else {
			RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/html/indexOFF.html");
			rd.include(request, response);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		String defaultbt = request.getParameter("defaultbt");
		String button = request.getParameter("button");
		String info = ("<h3><b>Προσοχή!</b>"
				+ "Υπάρχει <a  style=\"color:red;\" href=\"ReturnDB\">προγραμματισμένο</a> πότισμα "
				+ "που συμπίπτει με το παρών.</h3/<br>");
		String stopbt = ("<a href=\"ForceShutdown\"><button id=\"stop\">Διακοπή Ποτίσματος </button></a><br>");
		String returnbt = ("<a href=\"./\"><button>Επιστροφή</button></a>");
		out.println("<html><head>");
		out.println("<link rel=\"stylesheet\" href=\"graphic.css\" type=\"text/css\">");
		out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		out.println("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\">");
		out.println("</head><body>");
		out.println("<div class=\"responsed\" align=\"center\">");

		try {
			if (defaultbt != null) {
				int value = Integer.parseInt(defaultbt);
				if (setNow(value)) {
					out.println("<h3>Ένα πότισμα ξεκίνησε με διάρκεια: " + value + " λεπτά.</h3>");
					out.println(stopbt);
					response.setHeader("Refresh", "5;url=./");
				} else {
					out.println(info);
				}
				out.println(returnbt);
			} else if (button.equals("manualbt") && button != null) {
				int minutes = Integer.parseInt(request.getParameter("minutes"));
				if (minutes <= 0 || minutes > 300) {
					out.println("<h3><b>Προσοχή!</b> H διάρκεια δεν μπορεί να είναι μηδενική,"
							+ " αρνητική ή μεγαλύτερη από 300 λεπτά.</h3>");
				} else {
					if (setNow(minutes)) {
						out.println("<h3>Ένα πότισμα ξεκίνησε με διάρκεια: " + minutes + " λεπτά.</h3>");
						out.println(stopbt);
						response.setHeader("Refresh", "5;url=./");
					} else {
						out.println(info);
					}
				}
				out.println(returnbt);
			} else if (button.equals("schedulebt") && button != null) {
				Calendar c = Calendar.getInstance();
				int day = Integer.parseInt((request.getParameter("day")));
				int hourFrom = Integer.parseInt((request.getParameter("hourFrom")));
				int hourTo = Integer.parseInt((request.getParameter("hourTo")));
				int duration = 0;
				int limit = 0;

				if ((hourFrom >= 19) && (hourTo <= 5)) {
					duration = ((hourTo - hourFrom) + 24) * 60;
					limit = (hourTo - hourFrom) + 24;
				} else if (hourFrom >= 0 && hourTo <= 23) {
					if (hourFrom >= hourTo) {
						out.println(
								"<h3><b>Προσοχή!</b>Η ώρα έναρξης πρέπει να ειναι μικρότερη απο την ώρα λήξης.</h3>");
						out.println(returnbt);
						response.setHeader("Refresh", "5;url=./");
						return;
					} else {
						duration = (hourTo - hourFrom) * 60;
						limit = hourTo - hourFrom;
					}
				}
				if (limit > 5) {
					out.println("<h3><b>Προσοχή!</b>Η διάρκεια δεν μπορεί να είναι μεγαλύτερη από 5 ώρες.</h3>");
					out.println(returnbt);
					response.setHeader("Refresh", "5;url=./");
					return;
				} else if (c.get(Calendar.DAY_OF_WEEK) == day && c.get(Calendar.HOUR_OF_DAY) == hourFrom) {
					if (setNow(duration)) {
						out.println("<h3>Ένα πότισμα ξεκίνησε με διάρκεια: " + duration + " Λεπτά.</h3>");
						out.println(stopbt);
					} else {
						out.println(info);
					}
					out.println(returnbt);
				} else {
					String status = "off";
					if (insertInDatabase(day, hourFrom, 00, hourTo, duration, status) == 1) {
						response.sendRedirect("./FutureSchedule?duration=" + duration + "&hourFrom=" + hourFrom
								+ "&hourTo=" + hourTo + "&day=" + day + "&status=" + status);
						response.setHeader("Refresh", "8;url=./");
					} else {
						out.println(info);
						out.println(returnbt);
					}
				}
			}
		} catch (NumberFormatException e) {
			out.println("<h3><b>Προσοχή!</b> Όλες οι παράμετροι θα πρέπει να είναι ακέραιοι.</h3>");
			response.setHeader("Refresh", "8;url=./");
			out.println(returnbt);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		out.println("</div></body></html>");
	}

	public static void initialiseDatabase()
			throws SchedulerException, SQLException, InterruptedException, AddressException {
		log.info("Initialize Database");
		String results = null;
		Calendar c = Calendar.getInstance();
		conn = DBConnection.createDBConnection();
		String query = "SELECT * FROM wateringtable;";
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery(query);
		WateringScheduler watS = new WateringScheduler();
		Subscribe manageToday = new Subscribe();
		while (rs.next()) {
			int day = rs.getInt("Day");
			int hourFrom = rs.getInt("hourFrom");
			int hourTo = rs.getInt("hourTo");

			if (day == c.get(Calendar.DAY_OF_WEEK) && hourFrom == c.get(Calendar.HOUR_OF_DAY)) {
				log.info("Event for watering found!");
				int duration = rs.getInt("Duration");
				String update = "UPDATE wateringtable SET Status='on' WHERE Day=" + day + " and hourFrom=" + hourFrom
						+ " and hourTo=" + hourTo + ";";

				st.executeQuery(update);
				results = "Ωρα ποτίσματος : " + rs.getInt("hourFrom") + " Διάρκεια Ποτίσματος : "
						+ rs.getInt("Duration")+"\n";
				log.info(results);
				DBConnection.closeConnection(conn);
				watS.startExecutionAt(duration);
				TimeUnit.SECONDS.sleep(30);
				manageToday.sendEmailDaily(results, 1);
			}
		}
	}

	public int sameDayCollision(int day, int hour1, double hour2, int duration) throws SQLException {
		conn = DBConnection.createDBConnection();
		log.info("Checking For collision");
		String query = "SELECT * FROM wateringtable";
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery(query);
		int hourFDB = 0;
		double hourTDB = 0;
		int dayDB = 0;

		while (rs.next()) {
			dayDB = rs.getInt("Day");
			hourFDB = rs.getInt("hourFrom");
			hourTDB = rs.getDouble("hourTo");

			if (day == dayDB && hour1 >= 19 && hour2 <= 4 && hourFDB >= 19 && hourTDB <= 4) { 
				if (((hour1 <= hourFDB) && (hour2 >= hourTDB)) || ((hour1 >= hourFDB) && (hour2 <= hourTDB))) {
					log.info("Day and duration collision----1");
					return 0;
				}
				if (((hour1 >= hourFDB) && (hour2 >= hourTDB)) || ((hour1 <= hourFDB) && (hour2 <= hourTDB))) {
					log.info("Day and duration collision----2");
					return 0;
				}
			}
			if ((day == dayDB && ((hour1 < 19 && hourFDB <= 19) || (hour1 >= 18 && hour2 >= 19)))) {
				if (((hour1 <= hourFDB) && (hour2 > hourFDB)) || ((hour1 >= hourFDB) && (hour2 <= hourTDB))) {
					log.info("Day and duration collision----3");
					return 0;
				}
				if ((hour1 < hourTDB) && (hour2 >= hourTDB)) {
					log.info("Day and duration collision----4");
					return 0;
				}
			}
			if (day == dayDB && hour1 >= 19 && hour2 <= 4 && hourFDB >= 14 && hourTDB > hour1) { 
				log.info("Finishing Time collision's to same days starting time----1");
				return 0;
			}
			if (day == dayDB && (hour1 >= 19 || hour1 < 19) && hour2 >= 19 && hourFDB >= 19 && hourTDB <= 4
					&& hour2 > hourFDB) {
				log.info("Finishing Time collision's to same days starting time----2");
				return 0;
			}
			if (day + 1 == 8 && 1 == dayDB && hour1 >= 19 && hour2 <= 5 && hourFDB <= 5 && hourTDB <= 9
					&& hour2 > hourFDB) {
				log.info("Finishing Time collision's to nexts days starting time");
				return 0;
			}
			if (day + 1 == dayDB && hour1 >= 19 && hour2 <= 5 && hourFDB <= 5 && hourTDB <= 9 && hourFDB < hour2) {
				log.info("Finishing Time collision's to nexts days starting time");
				return 0;
			}
			if (day - 1 == 0 && 7 == dayDB && hour1 <= 4 && hourFDB >= 19 && hourTDB <= 4 && hourTDB > hour1) {
				log.info("Starting Time collision's to before's days ending time");
				return 0;
			}
			if (day - 1 == dayDB && hour1 <= 4 && hourFDB >= 19 && hourTDB <= 5 && hourTDB > hour1) {
				log.info("Starting Time collision's to before's days ending time");
				return 0;
			}
		}
		return 1;
	}

	public int insertInDatabase(int day, int hourFrom, int hourFromMin, double hourTo, int duration, String status)
			throws SQLException, AddressException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		int check = sameDayCollision(day, hourFrom, hourTo, duration);
		if (check == 1) {
			String query = " insert into wateringtable (Day, hourFrom, hourFromMin, hourTo, duration, Status, Date)"
					+ " values (?, ?, ?, ?, ?, ?, ?)";
			Calendar c = Calendar.getInstance();
			java.sql.Date startDate = new java.sql.Date(c.getTime().getTime());
			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setInt(1, day);
			preparedStmt.setInt(2, hourFrom);
			preparedStmt.setInt(3, hourFromMin);
			preparedStmt.setDouble(4, hourTo);
			preparedStmt.setInt(5, duration);
			preparedStmt.setString(6, status);
			preparedStmt.setDate(7, startDate);
			preparedStmt.execute();
			log.info("Insertion Completed");
			DBConnection.closeConnection(conn);
			return 1;
		} else {
			log.info("Υπαρχει ηδη προγραμματισμενο πότισμα");
			DBConnection.closeConnection(conn);
			return 0;
		}
	}

	public int disablePin() {
		pina.low();
		return 1;
	}

	public int enablePin() {
		pina.high();
		return 1;
	}

	public static void startScheduleJobForDBCheck() throws SchedulerException {
		log.info("Schedule will start for checking database at the start of each hour");
		JobDetail job = JobBuilder.newJob(View.class).withIdentity("view").build();
		
		@SuppressWarnings("rawtypes")
		ScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0 0 * * * ?");

		@SuppressWarnings("unchecked")
		Trigger trigger = TriggerBuilder.newTrigger().withSchedule(scheduleBuilder).build();
		
		CronScheduler = CronchedulerFactory.getScheduler();
		CronScheduler.scheduleJob(job, trigger);
		CronScheduler.start();
	}

	public void stopScheduler() throws SchedulerException {
		CronScheduler.shutdown(true);
	}

	public void execute(JobExecutionContext jExeCtx) throws JobExecutionException {
		try {
			log.info("Check database insertions!");
			initialiseDatabase();
		} catch (SchedulerException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (AddressException e) {
			e.printStackTrace();
		}
	}

	public boolean setNow(int duration) throws AddressException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException {
		Calendar c = Calendar.getInstance();
		int today = c.get(Calendar.DAY_OF_WEEK);
		int hourFrom = c.get(Calendar.HOUR_OF_DAY);
		int hourMinutes = c.get(Calendar.MINUTE);
		double hourSeconds = (double) c.get(Calendar.SECOND) / 3600;
		log.info(hourSeconds);
		double hourTo = 0;
		double computehourMinute = (double) hourMinutes / 60;
		double computeDuration = (double) duration / 60;
		double computeDelay = computehourMinute + computeDuration + hourSeconds;

		if (computeDelay >= 1) {
			hourTo = (double) (hourFrom + computeDelay);
			if (hourTo >= 24) {
				int hourDif = hourFrom - 24;
				hourTo = 0;
				hourTo = (double) (hourDif + computeDelay);
				log.info(hourTo);
			}
		} else if (computeDelay < 1) {
			hourTo = (double) hourFrom + computeDelay;
		}
		String status = "on";
		if (insertInDatabase(today, hourFrom, hourMinutes, hourTo, duration, status) == 1 && pina.isLow()) {
			WateringScheduler watS = new WateringScheduler();
			watS.startExecutionAt(duration);
			return true;
		} else
			return false;
	}
}
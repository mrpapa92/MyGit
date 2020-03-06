package watering;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.mail.internet.AddressException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

@WebServlet("/scheduler")
public class WateringScheduler extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(WateringScheduler.class);
	static ScheduledExecutorService executorService;

	public void startExecutionAt(int duration) {
		executorService = Executors.newScheduledThreadPool(1);
		final Runnable thread = new Runnable() {
			public void run() {

				Subscribe manageToday = new Subscribe();
				View View = new View();
				ForceShutdown ForceShutdown = new ForceShutdown();
				try {
					log.info("Execution started! Duration: " + duration + "!");
					View.enablePin();
					TimeUnit.SECONDS.sleep(duration * 60 - 2);
					ForceShutdown.closeWaterNow();
					manageToday.sendEmailDaily(
							"Το πότισμα με διάρκεια " + duration + " λεπτά ολοκληρώθηκε με επιτυχία!", 2);
					log.info("Execution finished normally");
				} catch (Exception ex) {
					log.error("Force Shutdown Thread " + ex.getMessage());
					try {
						manageToday.sendEmailDaily(
								"Το πότισμα με διάρκεια " + duration + " διακόπηκε από εσάς ή από κάποιο σφάλμα", 3);
					} catch (AddressException e) {
						e.printStackTrace();
					}
				}
			}
		};
		executorService.schedule(thread, 0, TimeUnit.MILLISECONDS);
	}

	public void stop() {
		executorService.shutdownNow();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		doPost(request, response);
	}
}
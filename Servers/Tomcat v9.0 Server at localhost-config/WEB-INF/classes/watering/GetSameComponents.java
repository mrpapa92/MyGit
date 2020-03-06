package watering;

import javax.mail.internet.AddressException;

public interface GetSameComponents {
	
	public String renameDays(int i);

	public void sendEmailDaily(String text, int flag) throws AddressException;
}

package client;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.mail.Message;

import utils.GmailUtils;

public class Gmail {
	
	public static void main(String[] args) {
	        try {
	            String userName = "gmail_uname";
	            String password = "gmail_pwd";
	            
	            GmailUtils gmailHelper = new GmailUtils(userName, password);
	            
	            // search keyword for subject
	            String keyword = "Forgot Password";
	            
	            // date after which messages need to be read
	            String dateStr = "Wed April 29 17:00:00 IST 2020";
	            DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
	            Date date = (Date)formatter.parse(dateStr);
	            System.out.println("Mail send time : " + date);

	            // get email
	            List<Message> inboxMessageList = gmailHelper.searchEmail(0, "Inbox", keyword, date, 15);
	            
	            // get body of the email
	            Message firstMessage = inboxMessageList.get(0);
	            String body = gmailHelper.getText(true, firstMessage);
	            
	            System.out.println("Message Subject : " + firstMessage.getSubject());
	            System.out.println("----------------------------------------------------------------------");
	            System.out.println("Message Content : " + body);
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	

}

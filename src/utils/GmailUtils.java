package utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import javax.mail.search.SearchTerm;

public class GmailUtils {
	private static String imapHost = "imap.gmail.com";
	private static String imapPort = "993";
	private String userName = null;
	private String password = null;

	public GmailUtils(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	// Set IMAP session
	private Session setIMAPSession() {
		// IMAP settings
		Properties properties = new Properties();

		// server setting
		properties.put("mail.imap.host", imapHost);
		properties.put("mail.imap.port", imapPort);

		// SSL setting
		properties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.setProperty("mail.imap.socketFactory.fallback", "false");
		properties.setProperty("mail.imap.socketFactory.port", String.valueOf(imapPort));
		return Session.getInstance(properties);
	}

	/*
	 * Searches for e-mail messages containing the specified keyword in Subject
	 * 
	 * @param no of seconds to wait before checking for mails
	 * 
	 * @param folderName (e.g. INBOX)
	 * 
	 * @param keyword Search with this Keyword in subject line
	 * 
	 * @param Date object after which emails to be searched. Null for any date
	 * 
	 * @param range Range of emails to be searched. -1 for all emails
	 * 
	 * @throws InterruptedException
	 */
	public List<Message> searchEmail(int noOfSecToWait, String folderName, String keyword, Date aDate, int range)
			throws InterruptedException {
		Thread.sleep(noOfSecToWait * 1000); // wait for email

		List<Message> returnList = new ArrayList<>();
		Folder folder = null;
		Store store = null;
		
		try {
			Thread.sleep(noOfSecToWait * 1000); // wait for email

			//Connects to the store via IMAP session
			Session session = setIMAPSession();

			System.out.println("Connects to Message Store");
			store = session.getStore("imap");
			store.connect(userName, password);

			System.out.println("Opens folder : " + folderName);
			folder = store.getFolder(folderName);
			folder.open(Folder.READ_ONLY);

			System.out.println("Performs search through the folder: " + folderName);
			int endIndex = folder.getMessageCount();
			int startIndex;
			if (range != -1)
				startIndex = endIndex - range;
			else
				startIndex = 0;

			System.out.println("Creating search condition: Searching message with Subject line: " + keyword
					+ ", within message range: " + startIndex + "," + endIndex + " sent after date: " + aDate);
			SearchTerm searchCondition = getSearchTerm(keyword, aDate);

			System.out.println("Performs search through the folder:");
			Message[] messages = folder.search(searchCondition, folder.getMessages(startIndex, endIndex));
			System.out.println("Number of messages found: " + messages.length);

			for (int i = 0; i < messages.length; i++) {
				Message message = messages[i];
				// assuming you retrieved 'message' from your folder object
				Message copyOfMessage = new MimeMessage((MimeMessage) message);
				returnList.add(copyOfMessage);
			}

		} catch (NoSuchProviderException ex) {
			System.out.println("No provider found.\n" + ex.getMessage());
			ex.printStackTrace();

		} catch (MessagingException ex) {
			System.out.println("Could not connect to the message store.\n" + ex.getMessage());
			ex.printStackTrace();

		} finally {
			System.out.println("Disconnecting...");
			if (folder != null) {
				try {
					folder.close(true);
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}
			if (store != null) {
				try {
					store.close();
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}

		}
		return returnList;
	}

	/*
	 * Create SearchTerm according to keyword and date
	 */
	private SearchTerm getSearchTerm(String keyword, Date aDate) {
		return new SearchTerm() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean match(Message message) {
				try {
					if (message.getSubject().contains(keyword)
							&& (aDate == null || message.getSentDate().after(aDate))) {
						return true;
					}
				} catch (MessagingException ex) {
					System.out.println(ex.getMessage());
					ex.printStackTrace();
				}
				return false;
			}
		};
	}

	/*
	 * Returns the primary text content of the email body
	 */
	public String getText(boolean textIsHtml, Part p) throws MessagingException, IOException {
		if (p.isMimeType("text/*")) {
			String s = (String) p.getContent();
			textIsHtml = p.isMimeType("text/html");
			return s;
		}

		if (p.isMimeType("multipart/alternative")) {
			System.out.println("prefer html text over plain text");

			// prefer html text over plain text
			Multipart mp = (Multipart) p.getContent();
			String text = null;
			for (int i = 0; i < mp.getCount(); i++) {
				Part bp = mp.getBodyPart(i);

				if (bp.isMimeType("text/plain")) {
					if (text == null)
						text = getText(textIsHtml, bp);
					continue;

				} else if (bp.isMimeType("text/html")) {
					String s = getText(textIsHtml, bp);
					if (s != null)
						return s;

				} else {
					return getText(textIsHtml, bp);
				}
			}
			return text;

		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) p.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				String s = getText(textIsHtml, mp.getBodyPart(i));

				if (s != null)
					return s;
			}
		}
		return null;
	}
}

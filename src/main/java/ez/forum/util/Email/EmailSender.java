package ez.forum.util.Email;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

/**
 * This class is able to send emails using configuration file "config.cfg".
 * @author matafokka
 *
 */
public class EmailSender {
	private static String pathToConfigFile = "/WEB-INF/classes/ez/forum/util/Email/config.cfg";
	private static HashMap<String, String> params = new HashMap<String, String>();
	private static Session session;
	
	// Read file with parameters
	static {
		String downloadFileStuff = "\nDownload this file again and put it in a root directory of the project.\nOr create this file manually.";
		try {
			init();
		} catch (FileNotFoundException e) {
			System.err.println("ERROR: File \"" + pathToConfigFile + "\" not found." + downloadFileStuff);
			System.exit(-1);
		} catch (IllegalArgumentException e) {
			System.err.println("ERROR: File \"" + pathToConfigFile + "\" does not contain all the needed parameters.\n" + e.getMessage() + downloadFileStuff);
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("ERROR: Can't read file \"" + pathToConfigFile + "\" because I/O error has occured. Please, deal with that file.\n"
					+ "Following error has occured: " + e.getMessage());
			System.exit(-1);
		}
	}
	
	/**
	 * Reads configuration file
	 * 
	 * @throws FileNotFoundException If config.cfg does not exist.
	 * @throws IOException If an I/O error occurs.
	 * @throws IllegalArgumentException If config.cfg does not contain all needed parameters.
	 */
	private static void init() throws FileNotFoundException, IOException, IllegalArgumentException {
		
		Properties props = new Properties();
		props.put("mail.smtp.starttls.enable", true);
		props.put("mail.smtp.auth", true);
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		
		HashMap<String, String> paramsList = new HashMap<String, String>();
		paramsList.put("server", "mail.smtp.host");
		paramsList.put("port", "mail.smtp.port");
		paramsList.put("login", "mail.smtp.user");
		paramsList.put("password", "mail.smtp.password");
		paramsList.put("subject_prefix", "subject_prefix");
		paramsList.put("footer", "footer");
		paramsList.put("ezforum_address", "ezforum_address");
		
		BufferedReader reader;
		reader = new BufferedReader(
				new FileReader(
							FacesContext.
							getCurrentInstance().
							getExternalContext().
							getRealPath(pathToConfigFile)
						)
				);
		
		// Parse lines
		String line = reader.readLine();
		while (paramsList.size() != 0 || line != null) {
			StringBuffer param = new StringBuffer("");
			// Parse text in line
			for (int i = 0; i < line.length() - 3; i++) {
				param.append(line.charAt(i)); // Fill param name
				
				// If param has been found
				if (line.substring(i + 1, i + 3).equals(": ")) {
					// If this param hasn't been found before, add it.
					String p = param.toString();
					if (paramsList.containsKey(p) && i + 4 < line.length()) {
						String value = line.substring(i + 3, line.length());
						props.put(paramsList.get(p), value);
						params.put(p, value);
						paramsList.remove(p);
					}
					break;
				}
			}
			line = reader.readLine();
		}
		reader.close();
		
		// If some paramsList are missing, throw an exception
		if (paramsList.size() != 0) {
			StringBuffer missingParams = new StringBuffer("");
			for (String p: paramsList.keySet()) {
				missingParams.append("\t" + p + "\n");
			}
			throw new IllegalArgumentException("Following parameters are missing:\n" + missingParams);
		}
		
		// Create an email session
		props.put("mail.smtp.socketFactory.port", params.get("port"));
		session = Session.getDefaultInstance(props);
	}
	
	/**
	 * Sends an email to the recipient with given subject and message.
	 * 
	 * @param recipient - email address of the recipient.
	 * @param subject - subject of a message.
	 * @param message - text of the message.
	 * 
	 * @return
	 * true - if message has been sent successfully.<br>
	 * false - otherwise.
	 */
	public static Boolean send(String recipient, String subject, String message) {
		MimeMessage msg = new MimeMessage(session);
		try {
			msg.setHeader("Content-Type", "text/plain; charset=UTF-8");
			msg.setFrom(params.get("login"));
			msg.addRecipients(MimeMessage.RecipientType.TO, recipient);
			msg.setSubject(params.get("subject_prefix") + " " + subject);
			msg.setContent(message + "<br><br>_________________________<br>" + params.get("footer"), "text/html; charset=UTF-8");
		} catch (MessagingException e) {
			// This shouldn't happen, all the passed stuff is valid.
			System.err.println("ERROR: Can't construct message. This shouldn't normally happen.\n"
					+ "Please, fill out an issue on github and attach an error text below to the issue."
					);
			e.printStackTrace();
			System.exit(-1);
		}
		
		// Create transport
		Transport transport;
		try {
			transport = session.getTransport("smtp");
		} catch (NoSuchProviderException e) {
			System.err.println("ERROR: Somehow, your Java implementation doesn't support SMTP protocol.\n"
					+ "Please, use a decent implementation.");
			System.exit(-1);
			return false;
		}
		
		try {
			// Connect to the server
			transport.connect(
					params.get("server"),
					params.get("login"),
					params.get("password")
					);
			// Send email
			transport.sendMessage(msg, msg.getAllRecipients());
			// Close transport
			transport.close();
		} catch (MessagingException e) {
			System.err.println("ERROR: Can't send message. Following error has occured:");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Returns a user-specified address to the ezforum.
	 * @return
	 */
	public static String getEzForumAddress() {
		return params.get("ezforum_address");
	}
	
	/**
	 * Sends registration confirmation message to the given email address.
	 * @param email - email of the recipient
	 * @param code - confirmation code
	 * 
	 * @return
	 * true - if message has been sent successfully.<br>
	 * false - otherwise.
	 */
	public static Boolean sendConfirmationMessage(String email, String code) {
		return send(email, "Registration confirmation",
				"Your confirmation code is: " + code + "<br><br>" +
				"Please, follow this link to confirm your registration: " + EmailSender.getEzForumAddress() + "confirm.xhtml?submit-code=submit&code=" + code);
	}
}

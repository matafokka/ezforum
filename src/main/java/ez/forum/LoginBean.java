package ez.forum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpSession;

import ez.forum.entities.User;
import ez.forum.util.BasicUserValidator;
import ez.forum.util.Redirecter;
import ez.forum.util.SHARED_OBJECTS;

@Named
@RequestScoped
public class LoginBean {
	private EntityManager em = SHARED_OBJECTS.emfactory.createEntityManager();
	
	private User user;
	private Boolean userNotFound = false;
	private Boolean userIsBanned = false;
	private String banReason;
	private ExternalContext context;
	private Map<String, String> contextParams;
	private HttpSession session;
	
	private String loginEmail;
	private String loginPassword;
	
	private String registerEmail;
	private String registerNickname;
	private String registerPassword;
	private String registerRepeatPassword;
	private ArrayList<String> registerErrors = new ArrayList<String>();
	
	@PostConstruct
	public void init() {
		this.context = FacesContext.
				getCurrentInstance().
				getExternalContext();
		this.contextParams = context.getRequestParameterMap();
		session = (HttpSession) context.getSession(true);
		
		// Find out if user is already logged in
		User u = (User) session.getAttribute("user");
		if (u != null) {
			this.banReason = u.getBanReason();
			// If banned user tries to sign in again, give him some pain
			if (!this.banReason.isEmpty()) {
				return;
			}
			Redirecter.redirect("index.xhtml");
			return;
		}
		
		// Log in or register user depending on pressed button
		if (contextParams.containsKey("login-button")) { login(); }
		else if (contextParams.containsKey("register-button")) { register(); }
	}
	
	/**
	 * Logs user in
	 */
	public void login() {
		// Read data from context
		loginEmail = contextParams.get("login-email");
		loginPassword = contextParams.get("login-password");
		if (loginEmail.isEmpty() && loginPassword.isEmpty()) { return; }
		
		// Try to find user in the database
		List<User> users = em.createNamedQuery("User.findAll", User.class).getResultList();
		User user = null;
		for (User u: users) {
			if (u.getDecryptedEmail().equals(loginEmail) && u.getDecryptedPassword(loginPassword).equals(loginPassword)) {
				user = u;
				break;
			}
		}
		
		if (user == null) {
			userNotFound = true;
			return;
		}
		
		// Check if user is banned
		if (!user.getBanReason().isEmpty()) {
			userIsBanned = true;
			banReason = user.getBanReason();
			return;
		}
		session.setAttribute("user", user);
		Redirecter.redirect("me.xhtml");
	}
	
	/**
	 * Registers user
	 */
	public void register() {
		// Read data from context
		registerEmail = contextParams.get("register-email");
		registerNickname = contextParams.get("register-nickname");
		registerPassword = contextParams.get("register-password");
		registerRepeatPassword = contextParams.get("register-repeat-password");
		
		// Validate data
		registerErrors = BasicUserValidator.validate(registerNickname, registerEmail, registerPassword, registerRepeatPassword);
		
		if (!registerErrors.isEmpty()) {
			return;
		}
		
		// Create a new user
		EntityTransaction trans = em.getTransaction();
		trans.begin();
		
		user = new User();
		user.setRank(4);
		user.setName(registerNickname);
		user.setEmail(new byte[1]);
		user.setAndEncryptPassword(registerPassword);
		em.persist(user);
		
		trans.commit();
		
		// Set new user unconfirmed or remove it, if confirmation message hasn't been sent
		trans = em.getTransaction();
		trans.begin();
		if (user.setAndEncryptEmail(registerEmail)) {
			em.merge(user);
			Redirecter.redirect("confirm.xhtml");
		}
		else {
			registerErrors.add("Sorry, we can't send a confirmation message to you right now."
					+ "Probably, your email doesn't exist."
					+ "If it does, please, try again later.");
			em.remove(user);
		}
		trans.commit();
		
	}
	
	@PreDestroy
	public void destroy() {
		em.close();
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Boolean getUserNotFound() {
		return userNotFound;
	}

	public void setUserNotFound(Boolean userNotFound) {
		this.userNotFound = userNotFound;
	}

	public Boolean getUserIsBanned() {
		return userIsBanned;
	}

	public void setUserIsBanned(Boolean userIsBanned) {
		this.userIsBanned = userIsBanned;
	}

	public String getBanReason() {
		return banReason;
	}

	public void setBanReason(String banReason) {
		this.banReason = banReason;
	}

	public String getLoginEmail() {
		return loginEmail;
	}

	public void setLoginEmail(String loginEmail) {
		this.loginEmail = loginEmail;
	}

	public String getLoginPassword() {
		return loginPassword;
	}

	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}

	public String getRegisterEmail() {
		return registerEmail;
	}

	public void setRegisterEmail(String registerEmail) {
		this.registerEmail = registerEmail;
	}

	public String getRegisterNickname() {
		return registerNickname;
	}

	public void setRegisterNickname(String registerNickname) {
		this.registerNickname = registerNickname;
	}

	public String getRegisterPassword() {
		return registerPassword;
	}

	public void setRegisterPassword(String registerPassword) {
		this.registerPassword = registerPassword;
	}

	public String getRegisterRepeatPassword() {
		return registerRepeatPassword;
	}

	public void setRegisterRepeatPassword(String registerRepeatPassword) {
		this.registerRepeatPassword = registerRepeatPassword;
	}

	public ArrayList<String> getRegisterErrors() {
		return registerErrors;
	}

	public void setRegisterErrors(ArrayList<String> registerErrors) {
		this.registerErrors = registerErrors;
	}
}

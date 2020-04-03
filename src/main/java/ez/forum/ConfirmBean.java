package ez.forum;

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
import ez.forum.util.SHARED_OBJECTS;

@Named
@RequestScoped
public class ConfirmBean {
	private EntityManager em = SHARED_OBJECTS.emfactory.createEntityManager();
	
	private Map<String, String> contextParams;
	private HttpSession session;
	
	private int errorCode = -1;
	private String email;
	
	@PostConstruct
	public void init() {
		ExternalContext context = FacesContext.
				getCurrentInstance().
				getExternalContext();
		contextParams = context.getRequestParameterMap();
		session = (HttpSession) context.getSession(true);
		
		// Find user in session
		if (session.getAttribute("user") != null) { return; }
		
		// Perform actions
		if (contextParams.containsKey("submit-code")) { confirm(); }
		else if (contextParams.containsKey("submit-email")) { resendCode(); }
	}
	
	/**
	 * Confirms given code
	 */
	public void confirm() {
		// Get confirmation code
		String code = contextParams.get("code");
		if (code == null) { return; }
		
		// Get user id from code.
		StringBuffer idBuffer = new StringBuffer("");
		for (int i=0; i < code.length() - 1; i++) {
			idBuffer.append(code.charAt(i));
			if (code.charAt(i+1) == '_') { break; }
		}
		
		errorCode = 0;
		
		long id;
		try { id = Long.parseLong(idBuffer.toString()); }
		catch (NumberFormatException e) { return; }
		
		// Validate given code
		User user = em.find(User.class, id);
		if (user == null) { return; }
		
		String userCode = user.getConfirmationCode();
		if (!userCode.equals(code)) { return; }
		
		// Set user
		EntityTransaction trans = em.getTransaction();
		trans.begin();
		user.setBanReason(null);
		em.merge(user);
		trans.commit();
		
		errorCode = -3;
		session.setAttribute("user", user);
	}
	
	/**
	 * Resends confirmation code
	 */
	public void resendCode() {
		email = contextParams.get("email");
		if (email == null) { return; }
		
		// Find user with given email
		List<User> users = em.createNamedQuery("User.findAll", User.class).getResultList();
		User user = null;
		for (User u: users) {
			if (u.getDecryptedEmail().equals(email)) {
				user = u;
				break;
			}
		}

		if (user == null || user.getConfirmationCode().isEmpty()) {
			errorCode = 1;
			return;
		}
		
		// Send an email with confirmation code.
		EntityTransaction trans = em.getTransaction();
		trans.begin();
		if (!user.setAndEncryptEmail(email)) {
			errorCode = 2;
			trans.rollback();
			return;
		}
		em.merge(user);
		trans.commit();
		
		errorCode = -2;
	}
	
	@PreDestroy
	public void destroy() {
		em.close();
	}

	/* Setters and getters */

	/**
	 * Returns error code
	 * @return
	 * 	-3 - User has been confirmed.
	 * 	-2 - Code has been resent.
	 * 	-1 - No operation has been performed.
	 * 	0 - Entered code is wrong.
	 * 	1 - User with given email doesn't exist.
	 * 	2 - Email hasn't been sent
	 */
	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
}

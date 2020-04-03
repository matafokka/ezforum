package ez.forum;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import ez.forum.entities.User;
import ez.forum.util.BasicUserValidator;
import ez.forum.util.Redirecter;
import ez.forum.util.SHARED_OBJECTS;

@Named
@RequestScoped
public class MeBean {
	private EntityManager em = SHARED_OBJECTS.emfactory.createEntityManager();
	
	private ExternalContext context;
	private Map<String, String> contextParams;
	private HttpSession session;
	private User user;
	private ArrayList<String> avatarErrors = new ArrayList<String>();
	private ArrayList<String> infoUpdateErrors = new ArrayList<String>();
	private String[] allowedExtensions = {"jpg", "jpeg", "png", "gif"};
	private Boolean infoUpdateAttempted = false;
	
	@PostConstruct
	public void init() {
		this.context = FacesContext.
				getCurrentInstance().
				getExternalContext();
		this.contextParams = context.getRequestParameterMap();
		session = (HttpSession) context.getSession(true);
		
		// Find out if user is logged in
		user = (User) session.getAttribute("user");
		if (user == null) {
			Redirecter.redirect("login.xhtml");
			return;
		}
		// If banned user tries to see his profile, don't do anything.
		// He shouldn't be here anyway.
		else if (!user.getBanReason().isEmpty()) {
			return;
		}
		
		// Update avatar and info
		if (contextParams.containsKey("change-avatar")) {
			changeAvatar();
			Redirecter.redirect("me.xhtml");
		}
		else if (contextParams.containsKey("change-info")) { changeInfo(); }
		else if (contextParams.containsKey("signout")) {
			session.removeAttribute("user");
			Redirecter.redirect("index.xhtml");
		}
	}
	
	/**
	 * Changes user avatar
	 */
	private void changeAvatar() {
		// Load and validate the image
		HttpServletRequest request = (HttpServletRequest) context.getRequest();
		Part part;
		try {
			part = request.getPart("avatar-upload");
		} catch (Exception e) {
			avatarErrors.add("me.avatar.errors.unknown");
			return;
		}
		if (part.getSize() > 1024*1024) {
			avatarErrors.add("me.avatar.errors.sizeIsTooBig.");
		}
		
		// Check extension
		String[] nameParts = part.getSubmittedFileName().split("\\.");
		String ext = nameParts[nameParts.length - 1];
		Boolean found = false;
		for (String e: allowedExtensions) {
			if (e.equals(ext)) {
				found = true;
				break;
			}
		}
		// At this point we should return because it's meaningless to check an actual content of the file.
		if (!found) {
			avatarErrors.add("me.avatar.errors.unsupportedType");
			return;
		}
		
		String err = "me.avatar.errors.notAnImage";
		try {
			if (ImageIO.read(part.getInputStream()) == null) {
				avatarErrors.add(err);
			}
		} catch (IOException e) {
			avatarErrors.add(err);
		}
		
		// Validation is done
		if (!avatarErrors.isEmpty()) { return; }
		
		// Replace previous avatar with a new one
		String root = context.getRealPath("/") + "/";
		new File(root + user.getAvatarPath()).delete();
		try {
			part.write(root + "uploads/avatars/" + user.getId() + "." + ext);
		} catch (IOException e) {
			System.err.println("ERROR: Users can't upload avatar because:");
			e.printStackTrace();
			avatarErrors.add("me.avatar.errors.serverError");
		}
	}
	
	/**
	 * Changes user information
	 */
	private void changeInfo() {
		infoUpdateAttempted = true;
		
		String newName = contextParams.get("change-name");
		String newEmail = contextParams.get("change-email");
		String newPassword = contextParams.get("change-password");
		String newRepeatPassword = contextParams.get("change-repeat-password");
		
		String emailToCheck = newEmail;
		String passwordToCheck = newPassword;
		if (user.getDecryptedEmail().equals(newEmail)) { emailToCheck = ""; }
		if (newPassword.isEmpty()) { passwordToCheck = ""; }
		
		infoUpdateErrors = BasicUserValidator.validate(
				"",
				emailToCheck,
				passwordToCheck,
				newRepeatPassword
				);
		if (!infoUpdateErrors.isEmpty()) { return; }
		
		EntityTransaction trans = em.getTransaction();
		trans.begin();
		
		user.setRealName(newName);

		if (!emailToCheck.isEmpty()) {
			if (user.setAndEncryptEmail(newEmail)) {
				session.removeAttribute("user");
				Redirecter.redirect("confirm.xhtml");
			} else {
				infoUpdateErrors.add("misc.userValidationErrors.cantUpdateEmail");
			}
		}
		
		if (!newPassword.isEmpty()) { user.setAndEncryptPassword(newPassword); }
		
		em.merge(user);
		trans.commit();
	}

	/* Setters and getters */
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public ArrayList<String> getAvatarErrors() {
		return avatarErrors;
	}

	public void setAvatarErrors(ArrayList<String> avatarErrors) {
		this.avatarErrors = avatarErrors;
	}

	public ArrayList<String> getInfoUpdateErrors() {
		return infoUpdateErrors;
	}

	public void setInfoUpdateErrors(ArrayList<String> infoUpdateErrors) {
		this.infoUpdateErrors = infoUpdateErrors;
	}

	public Boolean getInfoUpdateAttempted() {
		return infoUpdateAttempted;
	}

	public void setInfoUpdateAttempted(Boolean infoUpdateAttempted) {
		this.infoUpdateAttempted = infoUpdateAttempted;
	}
}

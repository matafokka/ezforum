package ez.forum.util;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import ez.forum.entities.User;

/**
 * Validates given credentials
 * @author matafokka
 *
 */
public class BasicUserValidator {	
	
	/**
	 * Validates given credentials.
	 * If you want to exclude some variables, pass an empty string.
	 * If you need to pass an empty string and NOT to exclude a variable, pass null.
	 * 
	 * !!! Do NOT encrypt any passed data, this method will do everything for you !!!
	 * 
	 * @return
	 * List of errors containing their descriptions in English. If it's empty, credentials are valid.
	 */
	public static ArrayList<String> validate(String nickname, String email, String password, String repeatPassword) {
		ArrayList<String> errors = new ArrayList<String>();
		
		if (nickname == null || email == null || password == null || repeatPassword == null) {
			errors.add("misc.userValidationErrors.emptyFields");
			return errors;
		}
		Boolean nicknameAdded = false, emailAdded = false;
		
		EntityManager em = SHARED_OBJECTS.emfactory.createEntityManager();
		List<User> users = em.createNamedQuery("User.findAll", User.class).getResultList();
		for (User u: users) {
			if (!nicknameAdded && u.getName().equals(nickname)) {
				nicknameAdded = false;
				errors.add("misc.userValidationErrors.nicknameExists");
			}
			if (!emailAdded && u.getDecryptedEmail().equals(email)) {
				emailAdded = false;
				errors.add("misc.userValidationErrors.emailExists");
			}
			if (nicknameAdded && emailAdded) { break; }
		}
		
		if (!email.isEmpty() && !SHARED_OBJECTS.emailPattern.matcher(email).matches()) {
			errors.add("misc.userValidationErrors.emailIsNotValid");
		}
		
		if (password.isEmpty()) { return errors; }
			
		if (password.length() < 8) {
			errors.add("misc.userValidationErrors.passwordIsTooShort");
		}
		
		if (!password.equals(repeatPassword)) {
			errors.add("misc.userValidationErrors.passwordsDoNotMatch");
		}
		
		return errors;
	}
}

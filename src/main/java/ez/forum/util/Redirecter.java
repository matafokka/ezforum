package ez.forum.util;

import java.io.IOException;
import javax.faces.context.FacesContext;

/**
 * A shorthand for ExternalContext.redirect(url);
 */
public class Redirecter {
	/**
	 * A shorthand for ExternalContext.redirect(url);
	 */
	public static void redirect(String url) {
		try {
			 FacesContext.
				getCurrentInstance().
				getExternalContext().
				redirect(url);
		} catch (IOException e) {}
		return;
	}
}

package ez.forum.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import javax.faces.context.FacesContext;

public class Locales {
	public static HashMap<String, String> locales = new HashMap<String, String>();
	
	static {
		Iterator<Locale> localesIter = FacesContext.
				getCurrentInstance().
				getApplication().
				getSupportedLocales();
		while (localesIter.hasNext()) {
			Locale l = localesIter.next();
			String display = l.getDisplayLanguage(l); 
			display = Character.toUpperCase(display.charAt(0)) + display.substring(1, display.length());
			locales.put(display, l.getLanguage());
		}
	}
}

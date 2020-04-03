package ez.forum.util;

import java.io.File;
import java.util.HashMap;

import javax.faces.context.FacesContext;

/**
 * Contains HashMap with themes, where:
 * 	Key - A title of the theme
 * 	Value - A path to the theme
 * 
 * @author matafokka
 *
 */
public class Themes {
	public static HashMap<String, String> themes = new HashMap<String, String>();
	
	static {
		File themesDir = new File(FacesContext.
		getCurrentInstance().
		getExternalContext().
		getRealPath("/themes/"));
		
		for (File theme: themesDir.listFiles()) {
			// Check if it's a directory, and theme.css exists
			String file = theme.getPath() + "/theme.css";
			if (theme.isFile() || !new File(file).exists()) { continue; }
			
			String themeDirName = theme.getName();
			
			// Replace underscores with spaces and convert to camel case
			StringBuffer themeName = new StringBuffer(themeDirName);
			themeName.setCharAt(0, Character.toUpperCase(themeName.charAt(0))); // Replace first char
			for (int i = 1; i < themeName.length(); i++) {
				char previous = themeName.charAt(i - 1);
				char current = themeName.charAt(i);
				
				if (current == '_') { themeName.setCharAt(i, ' '); }
				else if (previous == ' ') { themeName.setCharAt(i, Character.toUpperCase(current)); }
			}
			
			themes.put(themeName.toString(), themeDirName);
		}
	}
}

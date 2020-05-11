package ez.forum;

import java.util.Calendar;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import ez.forum.entities.User;
import ez.forum.util.Locales;
import ez.forum.util.Themes;

@Named
@RequestScoped
public class Middleware {
	private String locale;
	private String theme = "rainy";
	private User user;
	private Boolean isMobile = false;
	private int currentYear = Calendar.getInstance().get(Calendar.YEAR);
	private String userLink = "login.xhtml";
	private String userLinkText = "template.icons.register";
	
	private ExternalContext context;
	private Map<String, String> contextParams;
	
	
	@PostConstruct
	public void init() {
		context = FacesContext.getCurrentInstance().getExternalContext();
		contextParams = context.getRequestHeaderMap();
		
		// Determine if user is using mobile device
		String userAgent = contextParams.get("User-Agent");
		if (userAgent.contains("Android") || userAgent.contains("iphone")) {
			isMobile = true;
		}
		
		// Get user from session
		HttpSession session = (HttpSession) context.getSession(true);
		user = (User) session.getAttribute("user");
		
		// Get locale and theme from cookies or user if one is logged in
		if (user == null) {
			Map<String, Object> cookies = context.getRequestCookieMap();
			try { theme = ((Cookie) cookies.get("theme")).getValue(); }
			catch (NullPointerException e) { theme = null; }
			try { locale = ((Cookie) cookies.get("locale")).getValue(); }
			catch (NullPointerException e) { locale = null; }
		} else {
			theme = user.getTheme();
			locale = user.getLocale();
			
			this.userLink = "me.xhtml";
			this.userLinkText = "template.icons.me";
		}

		if (theme != null && Themes.themes.containsKey(theme)) { theme = Themes.themes.get(theme); }
		else { theme = "rainy"; }
		
		if (locale == null || !Locales.locales.containsValue(locale)) { locale = "en"; }
	}

	public String getTheme() {
		return theme;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Boolean getIsMobile() {
		return isMobile;
	}

	public void setIsMobile(Boolean isMobile) {
		this.isMobile = isMobile;
	}

	public int getCurrentYear() {
		return currentYear;
	}

	public void setCurrentYear(int currentYear) {
		this.currentYear = currentYear;
	}

	public String getUserLink() {
		return userLink;
	}

	public void setUserLink(String userLink) {
		this.userLink = userLink;
	}

	public String getUserLinkText() {
		return this.userLinkText;
	}

	public void setUserLinkText(String userLinkText) {
		this.userLinkText = userLinkText;
	}
}

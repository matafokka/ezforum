package ez.forum;

import java.util.HashMap;
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

import ez.forum.entities.Section;
import ez.forum.entities.Subsection;
import ez.forum.entities.User;
import ez.forum.util.Locales;
import ez.forum.util.Redirecter;
import ez.forum.util.SHARED_OBJECTS;
import ez.forum.util.Themes;

@Named
@RequestScoped
public class IndexBean {
	private List<Section> sections;
	private Boolean isAdmin = true;
	private EntityManager em = SHARED_OBJECTS.emfactory.createEntityManager();
	
	private User user;
	
	private ExternalContext context;
	private Map<String, String> contextParams;
	private HttpSession session;
	
	@PostConstruct
	public void init() {
		context = FacesContext.
				getCurrentInstance().
				getExternalContext();
		contextParams = context.getRequestParameterMap();
		session = (HttpSession) context.getSession(true);
		
		// If user is not a super admin or creator, return
		user = (User) session.getAttribute("user");
		
		if (contextParams.containsKey("change-info")) {
			changeInfo();
			Redirecter.redirect("index.xhtml");
			return;
		}
		
		if (user == null || user.getRank() > 1) {
			isAdmin = false;
			renderView();
			return;
		}
		
		if (contextParams.containsKey("new-section")) { createSection(); }
		else if (contextParams.containsKey("new-subsection")) { createSubsection(); }
		else {
			removeSection();
			renderView();
			return;
		}
		
		Redirecter.redirect("index.xhtml");
	}
	
	/**
	 * Sets theme and locale
	 */
	public void changeInfo() {
		// Change theme
		String theme = contextParams.get("change-theme");
		if (theme == null || !Themes.themes.containsKey(theme)) { theme = "rainy"; }
		
		String locale = contextParams.get("change-locale");
		if (locale == null || !Locales.locales.containsKey(locale)) { locale = "en"; }
		else { locale = Locales.locales.get(locale); }
		
		if (user == null) {
			context.addResponseCookie("theme", theme, null);
			context.addResponseCookie("locale", locale, null);
			return;
		}
		
		user = em.find(User.class, user.getId());
		EntityTransaction trans = em.getTransaction();
		trans.begin();
		user.setTheme(theme);
		user.setLocale(locale);
		trans.commit();
		
		session.setAttribute("user", user);
	}
	
	/**
	 * Creates section
	 */
	public void createSection() {
		String name = contextParams.get("new-section-name");
		if (name == null || name.isEmpty()) { return; }
		
		EntityTransaction trans = em.getTransaction();
		trans.begin();
		Section sec = new Section();
		sec.setName(name);
		em.persist(sec);
		trans.commit();
	}
	
	/**
	 * Creates subsection
	 */
	public void createSubsection() {
		String name = contextParams.get("new-subsection-name");
		if (name == null || name.isEmpty()) { return; }
		
		Long parentId;
		try { parentId = Long.parseLong(contextParams.get("section-id")); }
		catch (NumberFormatException | NullPointerException e) { return; }
		
		Section section = em.find(Section.class, parentId);
		if (section == null) { return; }
		
		EntityTransaction trans = em.getTransaction();
		trans.begin();
		Subsection sec = new Subsection();
		sec.setName(name);
		sec.setSectionBean(section);
		em.persist(sec);
		trans.commit();
	}
	
	/**
	 * Removes sections and subsections.
	 */
	public void removeSection() {
		long id;
		try { id = Long.parseLong(contextParams.get("section-id")); }
		catch (NumberFormatException | NullPointerException e) { return; }
		

		EntityTransaction trans = em.getTransaction();
		trans.begin();
		
		if (contextParams.containsKey("remove-section")) {
			em.remove(em.find(Section.class, id));
		} else if (contextParams.containsKey("remove-subsection")) {
			em.remove(em.find(Subsection.class, id));
		}
		
		trans.commit();
	}
	
	private void renderView() {
		this.sections = em.createNamedQuery("Section.findAll", Section.class).getResultList();
	}
	
	@PreDestroy
	public void destroy() {
		em.close();
	}

	// Setters and getters
	
	public HashMap<String, String> getLocales() {
		return Locales.locales;
	}
	
	public HashMap<String, String> getThemes() {
		return Themes.themes;
	}
	
	public List<Section> getSections() {
		return sections;
	}

	public void setSections(List<Section> sections) {
		this.sections = sections;
	}

	public Boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(Boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
	
}

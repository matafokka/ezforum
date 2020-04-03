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
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpSession;

import ez.forum.entities.Complaint;
import ez.forum.entities.User;
import ez.forum.util.Redirecter;
import ez.forum.util.SHARED_OBJECTS;

@Named
@RequestScoped
public class ProfileBean {
	private User user;
	private User currentUser;
	private Boolean canComplain = false;
	private Complaint complaint;
	private EntityManager em = SHARED_OBJECTS.emfactory.createEntityManager();
	private Map<String, String> contextParams;
	
	@PostConstruct
	public void init() {
		Long userID;
		try {
			userID = Long.parseLong(
					FacesContext.
					getCurrentInstance().
					getExternalContext().
					getRequestParameterMap().
					get("id")
					);
		} catch (NumberFormatException | NullPointerException e) {
			return;
		}
		user = em.find(User.class, userID);
		
		ExternalContext context = FacesContext.
				getCurrentInstance().
				getExternalContext();
		contextParams = context.getRequestParameterMap();
		
		HttpSession session = (HttpSession) context.getSession(true);
		currentUser = (User) session.getAttribute("user");
		
		// If user is current user, redirect him to his own page.
		if (currentUser != null && userID == currentUser.getId()) {
			Redirecter.redirect("me.xhtml");
			return;
		}

		if (currentUser == null) { return; }

		dispatchComplaint();
		
		// Admin actions
		int currentRank = currentUser.getRank();
		if (currentRank >= user.getRank()) { return; }
		
		EntityTransaction trans = em.getTransaction();
		trans.begin();

		// Change rank
		if (contextParams.containsKey("change-rank")) {
			System.out.println(contextParams.get("new-rank"));
			int newRank;
			try { newRank = Integer.parseInt(contextParams.get("new-rank")); }
			catch (NumberFormatException | NullPointerException e) { return; }
			if (currentRank != 0 && newRank <= currentRank) { return; }
			
			user.setRank(newRank);
		}
		
		// Ban user
		else if (contextParams.containsKey("ban")) {
			String banReason = contextParams.get("ban-reason");
			if (banReason == null) { return; }
			user.ban(banReason);
			em.merge(user);
		}
		
		// Unban user
		else if (contextParams.containsKey("unban")) {
			user.setBanReason(null);
			em.merge(user);
		}
		
		// Remove all posts
		else if (contextParams.containsKey("remove-all-posts")) {
			em.createQuery(
					"DELETE FROM Post p WHERE p.user.id = " + user.getId()
					).executeUpdate();
		}
		
		// Remove all topics
		else if (contextParams.containsKey("remove-all-topics")) {
			em.createQuery(
					"DELETE FROM Topic t WHERE t.user.id = " + user.getId()
					).executeUpdate();
		}
		
		else { return; }
		
		trans.commit();
		Redirecter.redirect("profile.xhtml?id=" + user.getId());
	}
	
	private void dispatchComplaint() {
		// Can't complain about banned users and creators
		String banReason = user.getBanReason();
		if (user.getRank() == 0 || (banReason != null && !banReason.isEmpty())) { return; }
		
		TypedQuery<Complaint> query = em.createQuery(
				"SELECT c FROM Complaint c WHERE c.sender.id = ?1 AND c.receiver.id = ?2",
				Complaint.class
				);
		query.setParameter(1, currentUser.getId());
		query.setParameter(2, user.getId());
		List<Complaint> compls = query.getResultList();
		
		if (!compls.isEmpty()) { complaint = compls.get(0); }
		else { canComplain = true; }
		
		// Process user's complaint
		EntityTransaction trans = em.getTransaction();
		trans.begin();
		
		if (canComplain && contextParams.containsKey("complain")) {
			String reason = contextParams.get("reason");
			if (reason == null || reason == "") {
				trans.rollback();
				return;
			}
			
			Complaint compl = new Complaint();
			compl.setSender(currentUser);
			compl.setReceiver(user);
			compl.setReason(reason);
			complaint = em.merge(compl);
			canComplain = false;
		}
		
		// Remove user's complaint
		else if (!canComplain && contextParams.containsKey("remove-complaint")) {
			em.remove(complaint);
			canComplain = true;
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

	public Boolean getCanComplain() {
		return canComplain;
	}

	public void setCanComplain(Boolean canComplain) {
		this.canComplain = canComplain;
	}

	public Complaint getComplaint() {
		return complaint;
	}

	public void setComplaint(Complaint complaint) {
		this.complaint = complaint;
	}
	
}

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
import ez.forum.entities.PostReviewRequest;
import ez.forum.entities.TopicReviewRequest;
import ez.forum.entities.User;
import ez.forum.util.Redirecter;
import ez.forum.util.SHARED_OBJECTS;


/*
 * This class is different from the others because it contains really small operations.
 * It's better to pack them into conditions than to split into methods.
 */


@Named
@RequestScoped
public class AdminBean {
	private ExternalContext context;
	private Map<String, String> contextParams;
	private HttpSession session;
	private User user;
	private int userRank;
	private EntityManager em = SHARED_OBJECTS.emfactory.createEntityManager();
	
	private String view = "";
	private List<Complaint> complaints;
	private List<TopicReviewRequest> topicReviewRequests;
	private List<PostReviewRequest> postReviewRequests;
	private List<User> bannedUsers;
	
	@PostConstruct
	public void init() {
		this.context = FacesContext.
				getCurrentInstance().
				getExternalContext();
		this.contextParams = context.getRequestParameterMap();
		session = (HttpSession) context.getSession(true);
		
		// If user is not logged in or not an admin, return
		user = (User) session.getAttribute("user");
		if (user == null || user.getRank() == 4 ) { return; }
		
		// Perform admin's actions
		long id;
		try { id = Long.parseLong(contextParams.get("id")); }
		catch (NumberFormatException | NullPointerException e) {
			renderView();
			return;
		}
		
		EntityTransaction trans = em.getTransaction();
		trans.begin();
		
		// Complaints about users
		if (contextParams.containsKey("decline-complaint")) {
			Complaint compl = validateComplaint(id);
			if (compl != null) { em.remove(compl); }
		}
		
		else if (contextParams.containsKey("ban-user")) {
			String banReason = contextParams.get("ban-reason");
			if (banReason == null) {
				trans.rollback();
				return;
			}
			
			Complaint compl = validateComplaint(id);
			if (compl == null) {
				trans.rollback();
				return;
			}
			
			User u = compl.getReceiver();
			u.ban(banReason);
			em.merge(u);
		}
		
		// Topics
		else if (contextParams.containsKey("decline-topic-review-request")) {
			TopicReviewRequest req = validateTopicRequest(id);
			if (req != null) { em.remove(req); }
		}
		
		else if (contextParams.containsKey("remove-topic-by-request")) {
			TopicReviewRequest req = validateTopicRequest(id);
			if (req != null) { em.remove(req.getTopicBean()); }
		}
		
		// Posts
		else if (contextParams.containsKey("decline-post-review-request")) {
			PostReviewRequest req = validatePostRequest(id);
			if (req != null) { em.remove(req); }
		}
		
		else if (contextParams.containsKey("remove-post-by-request")) {
			PostReviewRequest req2 = validatePostRequest(id);
			if (req2 != null) { em.remove(req2.getPostBean()); }
		}
		
		// Banned users
		else if (contextParams.containsKey("unban-user")) {
			User u = em.find(User.class, id);
			if (u != null && u.getRank() > userRank) {
				u.setBanReason(null);
				em.merge(u);
			}
		}
		
		else {
			trans.rollback();
			return;
		}
		
		trans.commit();
		renderView();
		Redirecter.redirect("admin.xhtml?view=" + view);
	}
	
	private void renderView() {
		// Get data depending on request. This speeds performance up.
		view = contextParams.get("view");
		if (view == null) { view = ""; }
		
		userRank = user.getRank();
		switch(view) {
		case "topic_review":
			TypedQuery<TopicReviewRequest> query1 = em.createQuery(
					"SELECT req FROM TopicReviewRequest req WHERE "
					+ "?1 = 0 OR (req.user.rank < ?1 AND req.topicBean.user.rank < ?1)",
					TopicReviewRequest.class);
			query1.setParameter(1, userRank);
			topicReviewRequests = query1.getResultList();
			break;
		case "post_review":
			TypedQuery<PostReviewRequest> query2 = em.createQuery(
					"SELECT req FROM PostReviewRequest req WHERE "
					+ "?1 = 0 OR (req.user.rank < ?1 AND req.postBean.user.rank < ?1)",
					PostReviewRequest.class);
			query2.setParameter(1, userRank);
			postReviewRequests = query2.getResultList();
			break;
		case "banned_users":
			TypedQuery<User> query3 = em.createQuery(
					"SELECT u FROM User u WHERE "
					+ "(?1 = 0 OR u.rank < ?1) AND (u.banReason != NULL AND u.banReason != '')",
					User.class);
			query3.setParameter(1, userRank);
			bannedUsers = query3.getResultList();
			break;
		default:
			TypedQuery<Complaint> query4 = em.createQuery(
					"SELECT c FROM Complaint c WHERE "
					+ "?1 = 0 OR (c.sender.rank < ?1 AND c.receiver.rank < ?1)",
					Complaint.class);
			query4.setParameter(1, userRank);
			complaints = query4.getResultList();
			break;
		}
	}

	private Complaint validateComplaint(long id) {
		Complaint compl = em.find(Complaint.class, id);
		if (compl == null || compl.getSender().getRank() <= userRank || compl.getReceiver().getRank() <= userRank) { return null; }
		return compl;
	}
	
	private TopicReviewRequest validateTopicRequest(long id) {
		TopicReviewRequest req = em.find(TopicReviewRequest.class, id);
		if (req == null || (req.getUser().getRank() <= userRank || req.getTopicBean().getUser().getRank() <= userRank) && userRank != 0) { return null; }
		return req;
	}
	
	private PostReviewRequest validatePostRequest(long id) {
		PostReviewRequest req = em.find(PostReviewRequest.class, id);
		if (req == null || (req.getUser().getRank() <= userRank || req.getPostBean().getUser().getRank() <= userRank) && userRank != 0) { return null; }
		return req;
	}
	
	@PreDestroy
	public void destroy() {
		em.close();
	}

	// Setters and getters
	
	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}
	
	public List<Complaint> getComplaints() {
		return complaints;
	}

	public void setComplaints(List<Complaint> complaints) {
		this.complaints = complaints;
	}

	public List<TopicReviewRequest> getTopicReviewRequests() {
		return topicReviewRequests;
	}

	public void setTopicReviewRequests(List<TopicReviewRequest> topicReviewRequests) {
		this.topicReviewRequests = topicReviewRequests;
	}

	public List<PostReviewRequest> getPostReviewRequests() {
		return postReviewRequests;
	}

	public void setPostReviewRequests(List<PostReviewRequest> postReviewRequests) {
		this.postReviewRequests = postReviewRequests;
	}

	public List<User> getBannedUsers() {
		return bannedUsers;
	}

	public void setBannedUsers(List<User> bannedUsers) {
		this.bannedUsers = bannedUsers;
	}
}

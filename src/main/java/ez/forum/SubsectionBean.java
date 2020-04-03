package ez.forum;

import java.sql.Timestamp;
import java.time.Instant;
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

import ez.forum.entities.Subsection;
import ez.forum.entities.Topic;
import ez.forum.entities.TopicReviewRequest;
import ez.forum.entities.User;
import ez.forum.util.PagesHelper;
import ez.forum.util.Redirecter;
import ez.forum.util.ReviewRequestDispatcher;
import ez.forum.util.SHARED_OBJECTS;

@Named
@RequestScoped
public class SubsectionBean {
	private EntityManager em = SHARED_OBJECTS.emfactory.createEntityManager();
	private ExternalContext context;
	private Map<String, String> contextParams;
	private User user;
	
	private int page;
	private int pagesCount = 1;
	private long subsectionID;
	private List<Topic> topics;
	private Subsection subsection;
	private Instant currentTime = Instant.now();

	@PostConstruct
	public void init() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		context = facesContext.getExternalContext();
		contextParams = context.getRequestParameterMap();
		HttpSession session = (HttpSession) context.getSession(true);

		try { subsectionID = Long.parseLong(contextParams.get("id")); }
		catch (NumberFormatException | NullPointerException e) { return; }
		subsection = em.find(Subsection.class, subsectionID);
		
		try { page = Integer.parseInt(contextParams.get("page")); }
		catch (NumberFormatException | NullPointerException e) { page = 1; }
		
		// Find out if user is logged in
		user = (User) session.getAttribute("user");
		if (user == null || !user.getBanReason().isEmpty()) {
			renderView();
			return;
		}
		
		if (contextParams.containsKey("complain")) { createComplaint(); }
		else if (contextParams.containsKey("remove-complaint")) { removeComplaint(); }
		else if (contextParams.containsKey("edit")) { editTopic(); }
		else if (contextParams.containsKey("post-publish")) { publishTopic(); }
		else if (contextParams.containsKey("remove-topic")) { removeTopic(); }
		else if (contextParams.containsKey("lock")) { lockTopic(); }
		else if (contextParams.containsKey("unlock")) { unlockTopic(); }
		else {
			renderView();
			return;
		}
		
		Redirecter.redirect("subsection.xhtml?id=" + subsectionID + "&page=" + page);
	}
	
	/**
	 * Creates complaint
	 */
	public void createComplaint() {
		long topicId;
		try {
			topicId = Long.parseLong(contextParams.get("topicId"));
		} catch (NumberFormatException | NullPointerException e) { return; }
		String reason = contextParams.get("reason");
		if (reason == null || reason.isEmpty() || ReviewRequestDispatcher.getReviewRequestByEntityId(topicId, user, TopicReviewRequest.class) != null) { return; }
		Topic t = em.find(Topic.class, topicId);
		if (t.getUser().getId() == user.getId()) { return; }
		
		EntityTransaction trans = em.getTransaction();
		trans.begin();
		TopicReviewRequest request = new TopicReviewRequest();
		request.setReason(reason);
		request.setUser(em.find(User.class, user.getId())); // Doing this because user is detached from this em
		request.setTopicBean(t);
		request.setReason(reason);
		em.persist(request);
		trans.commit();
	}
	
	/**
	 * Removes complaint
	 */
	public void removeComplaint() {
		long reqId;
		try { reqId = Long.parseLong(contextParams.get("requestId")); }
		catch (NumberFormatException | NullPointerException e) { return; }
		ReviewRequestDispatcher.removeReviewRequest(reqId, user, TopicReviewRequest.class);
	}
	
	/**
	 * Publishes topic
	 */
	public void publishTopic() {
		String name = contextParams.get("topic-name");
		String text = contextParams.get("post-text");

		if (name == null || text == null || name.isEmpty() || text.isEmpty()) { return; }
		
		EntityTransaction trans = em.getTransaction();
		trans.begin();
		Topic topic = new Topic();
		topic.setName(name);
		topic.setText(text);
		topic.setUser(em.find(User.class, user.getId()));
		topic.setSubsectionBean(subsection);
		topic.setDate(new Timestamp(System.currentTimeMillis()));
		em.persist(topic);
		trans.commit();
	}
	
	/**
	 * Changes text and name of the topic
	 */
	public void editTopic() {
		long topicId;
		try { topicId = Long.parseLong(contextParams.get("editor-id")); }
		catch (NumberFormatException | NullPointerException e) { return; }
		
		String name = contextParams.get("topic-name");
		String text = contextParams.get("post-text");
		if (name == null || text == null || name.isEmpty() || text.isEmpty()) { return; }
		
		Topic topic = em.find(Topic.class, topicId);
		
		// I'm too lazy to reverse this XD
		if (topic.getLockedBy() == null && (user.getRank() <= 2 || (user.getId() == topic.getUser().getId() && topic.getDate().toInstant().plusSeconds(36000).isAfter(currentTime)))) {
			EntityTransaction trans = em.getTransaction();
			trans.begin();
			topic.setName(name);
			topic.setText(text);
			trans.commit();
		}
	}
	
	/**
	 * Removes topic
	 */
	public void removeTopic() {
		long topicId;
		try {
			topicId = Long.parseLong(contextParams.get("section-id"));
		} catch (NumberFormatException | NullPointerException e) { return; }
		
		Topic t = em.find(Topic.class, topicId);
		int rank = user.getRank();
		if (rank != 0 && rank >= t.getUser().getRank()) { return; }
		
		EntityTransaction trans = em.getTransaction();
		trans.begin();
		em.remove(t);
		trans.commit();
	}
	
	/**
	 * Locks topic
	 */
	public void lockTopic() {
		Topic topic = findTopic();
		if (topic == null || topic.getLockedBy() != null) { return; }
		
		long rank = user.getRank();
		if (rank != 0 && rank >= topic.getUser().getRank()) { return; }
		
		EntityTransaction trans = em.getTransaction();
		trans.begin();
		topic.setLockedBy(user);
		em.merge(topic);
		trans.commit();
	}
	
	/**
	 * Unlocks topic
	 */
	public void unlockTopic() {
		Topic topic = findTopic();
		if (topic == null || topic.getLockedBy() == null) { return; }
		
		long rank = user.getRank();
		if (rank != 0 && rank >= topic.getUser().getRank() && rank >= topic.getLockedBy().getRank()) { return; }
		
		EntityTransaction trans = em.getTransaction();
		trans.begin();
		topic.setLockedBy(null);
		em.merge(topic);
		trans.commit();
	}
	
	/**
	 * Finds and returns topic specified by "topic-id" URL parameter
	 */
	private Topic findTopic() {
		long topicId;
		try { topicId = Long.parseLong(contextParams.get("topic-id"));}
		catch (NumberFormatException | NullPointerException e) { return null; }
		return em.find(Topic.class, topicId);
	}
		
	private void renderView() {
		topics = subsection.getTopics();
		pagesCount = PagesHelper.calculatePagesCount(topics.size());
		topics = PagesHelper.sublistForPage(topics, page);
	}
	
	@PreDestroy
	public void destroy() {
		em.close();
	}
	
	// Setters and getters
	
	public List<Topic> getTopics() {
		return topics;
	}

	public void setTopics(List<Topic> topics) {
		this.topics = topics;
	}

	public Subsection getSubsection() {
		return subsection;
	}

	public void setSubsection(Subsection subsection) {
		this.subsection = subsection;
	}

	public Long getSubsectionID() {
		return subsectionID;
	}

	public void setSubsectionID(Long subsectionID) {
		this.subsectionID = subsectionID;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPagesCount() {
		return pagesCount;
	}

	public void setPagesCount(int pagesCount) {
		this.pagesCount = pagesCount;
	}

	public Instant getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(Instant currentTime) {
		this.currentTime = currentTime;
	}
	
}

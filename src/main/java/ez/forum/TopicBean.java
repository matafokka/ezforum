package ez.forum;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpSession;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import ez.forum.entities.Topic;
import ez.forum.entities.User;
import ez.forum.util.PagesHelper;
import ez.forum.util.Redirecter;
import ez.forum.util.ReviewRequestDispatcher;
import ez.forum.util.SHARED_OBJECTS;
import ez.forum.entities.Post;
import ez.forum.entities.PostReviewRequest;

@Named
@RequestScoped
public class TopicBean {
	private EntityManager em = SHARED_OBJECTS.emfactory.createEntityManager();
	private ExternalContext context;
	private Map<String, String> contextParams;
	private User user;
	
	private int page;
	private int pagesCount = 1;
	
	private Long topicId;
	private Topic topic;
	private List<Post> posts;
	private Instant currentTime = Instant.now();
	
	@PostConstruct
	public void init() {
		context = FacesContext.
				getCurrentInstance().
				getExternalContext();
		contextParams = context.getRequestParameterMap();
		
		try { topicId = Long.parseLong(contextParams.get("id")); }
		catch (NumberFormatException | NullPointerException e) { return; }
		topic = em.find(Topic.class, topicId);
		
		try { page = Integer.parseInt(contextParams.get("page")); }
		catch (NumberFormatException | NullPointerException e) { page = 1; }

		HttpSession session = (HttpSession) context.getSession(true);
		
		// Find out if user is logged in
		user = (User) session.getAttribute("user");
		if (user == null || !user.getBanReason().isEmpty()) {
			renderView();
			return;
		}
		
		if (contextParams.containsKey("complain")) { createComplaint(); }
		else if (contextParams.containsKey("remove-complaint")) { removeComplaint(); }
		else if (contextParams.containsKey("remove-post")) { removePost(); }
		else if (contextParams.containsKey("edit")) { editPost(); }
		else if (contextParams.containsKey("post-publish") && topic.getLockedBy() == null) { publishPost(); }
		else {
			renderView();
			return;
		}
		
		Redirecter.redirect("topic.xhtml?id=" + topic.getId() + "&page=" + page);
	}
	
	/**
	 * Creates complaint
	 */
	public void createComplaint() {
		long postId;
		try {
			postId = Long.parseLong(contextParams.get("postId"));
		} catch (NumberFormatException | NullPointerException e) {
			return;
		}
		String reason = contextParams.get("reason");
		if (reason == null || reason.isEmpty() || ReviewRequestDispatcher.getReviewRequestByEntityId(postId, user, PostReviewRequest.class) != null) { return; }
		Post p = em.find(Post.class, postId);
		if (p.getUser().getId() == user.getId()) { return; }
		
		EntityTransaction trans = em.getTransaction();
		trans.begin();
		PostReviewRequest request = new PostReviewRequest();
		request.setReason(reason);
		request.setUser(em.find(User.class, user.getId())); // Doing this because user is detached from this em
		request.setPostBean(p);
		request.setReason(reason);
		em.persist(request);
		trans.commit();
	}
	
	/**
	 * Removes complaint
	 */
	public void removeComplaint() {
		long reqId;
		try {
			reqId = Long.parseLong(contextParams.get("requestId"));
		} catch (NumberFormatException | NullPointerException e) { return; }
		ReviewRequestDispatcher.removeReviewRequest(reqId, user, PostReviewRequest.class);
	}
	
	/**
	 * Removes post
	 */
	public void removePost() {
		long postId;
		try {
			postId = Long.parseLong(contextParams.get("section-id"));
		} catch (NumberFormatException | NullPointerException e) { return; }
		
		Post p = em.find(Post.class, postId);
		int rank = user.getRank();
		if (rank != 0 && rank >= p.getUser().getRank()) { return; }
		
		EntityTransaction trans = em.getTransaction();
		trans.begin();
		em.remove(p);
		trans.commit();
	}
	
	/**
	 * Changes text of the post
	 */
	public void editPost() {
		long postId;
		try { postId = Long.parseLong(contextParams.get("editor-id")); }
		catch (NumberFormatException | NullPointerException e) { return; }
		
		String text = contextParams.get("post-text");
		if (text == null || text.isEmpty()) { return; }
		
		Post post = em.find(Post.class, postId);
		
		// I'm too lazy to reverse this XD
		if (user.getRank() <= 2 || (user.getId() == post.getUser().getId() && post.getDate().toInstant().plusSeconds(36000).isAfter(currentTime))) {
			EntityTransaction trans = em.getTransaction();
			trans.begin();
			post.setText(text);
			trans.commit();
		}
	}
	
	/**
	 * Publishes topic
	 */
	public void publishPost() {
		String text = contextParams.get("post-text");
		if (text == null) { return; }
		
		EntityTransaction trans = em.getTransaction();
		trans.begin();
		Post post = new Post();
		post.setText(text);
		post.setUser(em.find(User.class, user.getId())); // Doing this because user is detached from this em
		post.setTopicBean(topic);
		post.setDate(new Timestamp(System.currentTimeMillis()));
		em.persist(post);
		trans.commit();
	}
	
	private void renderView() {
		posts = topic.getPosts();
		pagesCount = PagesHelper.calculatePagesCount(posts.size());
		posts = PagesHelper.sublistForPage(posts, page);
	}

	@PreDestroy
	public void destroy() {
		em.close();
	}
	
	/* Setters and getters */
	
	public Topic getTopic() {
		return topic;
	}

	public void setTopic(Topic topic) {
		this.topic = topic;
	}

	public List<Post> getPosts() {
		return posts;
	}

	public void setPosts(List<Post> posts) {
		this.posts = posts;
	}

	public Long getTopicId() {
		return topicId;
	}

	public void setTopicId(Long topicId) {
		this.topicId = topicId;
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

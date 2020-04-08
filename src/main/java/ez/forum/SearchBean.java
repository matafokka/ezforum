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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ez.forum.entities.Post;
import ez.forum.entities.Topic;
import ez.forum.entities.User;
import ez.forum.util.PagesHelper;
import ez.forum.util.SHARED_OBJECTS;

@Named
@RequestScoped
public class SearchBean {
	private EntityManager em = SHARED_OBJECTS.emfactory.createEntityManager();
	private Map<String, String> contextParams;
	
	private Predicate pTopic, pPost;
	private String queryText;
	// Using HashMap instead of HashSet because HashSet uses HashMap. This leads to slower performance than HashMap.
	private HashMap <String, String> checkedSections = new HashMap<String, String>();
	private HashMap <String, String> checkedSubsections = new HashMap<String, String>();
	private HashMap <String, String> checkedBoxes = new HashMap<String, String>();
	private List<Topic> topics;
	private List<Post> posts;
	private List<User> users;
	private String url;
	private int pagesCount;
	private int page;
	
	/**
	 * 0 -- Topics and posts
	 * 1 -- Topics
	 * 2 -- Posts
	 * 3 -- Users
	 */
	private int action = 0;
	
	@PostConstruct
	public void init() {
		ExternalContext context = FacesContext.
				getCurrentInstance().
				getExternalContext();
		contextParams = context.getRequestParameterMap();
		
		// Get query text
		queryText = contextParams.get("search-query");
		if (queryText == null) { return; }
		
		// Get page number
		try { page = Integer.parseInt(contextParams.get("page")); }
		catch (NumberFormatException | NullPointerException e) { page = 1; }
		
		// Create url string used in page
		StringBuffer urlBuffer = new StringBuffer("search.xhtml?");
		for (String param: contextParams.keySet()) {
			if (param.equals("page")) { continue; }
			urlBuffer.append(param + "=" + contextParams.get(param) + "&");
		}
		int len = urlBuffer.length() - 1;
		if (urlBuffer.charAt(len) == '&') { urlBuffer.deleteCharAt(len); }
		url = urlBuffer.toString();
		
		// Get where to search
		String searchIn = contextParams.get("search-in");
		if (searchIn == null) { searchIn = ""; }
		
		// Define search locations
		
		CriteriaBuilder cb = em.getCriteriaBuilder();
		
		CriteriaQuery<Topic> topicQuery = cb.createQuery(Topic.class);
		CriteriaQuery<Post> postQuery = cb.createQuery(Post.class);
		CriteriaQuery<User> userQuery = cb.createQuery(User.class);
		
		Root<Topic> topicRoot = topicQuery.from(Topic.class);
		Root<Post> postRoot = postQuery.from(Post.class);
		Root<User> userRoot = userQuery.from(User.class);
		
		pTopic = cb.disjunction();
		pPost = cb.disjunction();
		
		for (String key: contextParams.keySet()) {
			String name = contextParams.get(key);
			if (key.contains("search-in-sections-section")) {
				checkedSections.put(name, null);
			} else if (key.contains("search-in-subs-sub-")) {
				checkedSubsections.put(name, null);
				pTopic = cb.or(pTopic,
						cb.equal(topicRoot.get("subsectionBean").get("name"), name)
						);
				pPost = cb.or(pPost,
						cb.equal(postRoot.get("topicBean").get("subsectionBean").get("name"), name)
						);
			} else if (key.contains("include-") || key.contains("match-")) {
				checkedBoxes.put(key, "checked");
			}
		}
		
		// Do the search
		switch(searchIn) {
		case "topics":
			action = 1;
			topics = createQuery(Topic.class, cb, topicQuery, topicRoot);
			
			pagesCount = PagesHelper.calculatePagesCount(topics.size());
			topics = PagesHelper.sublistForPage(topics, page);
			break;
		case "posts":
			action = 2;
			posts = createQuery(Post.class, cb, postQuery, postRoot);
			
			pagesCount = PagesHelper.calculatePagesCount(posts.size());
			posts = PagesHelper.sublistForPage(posts, page);
			break;
		case "users":
			action = 3;
			users = createQuery(User.class, cb, userQuery, userRoot);

			pagesCount = PagesHelper.calculatePagesCount(users.size());
			users = PagesHelper.sublistForPage(users, page);
			break;
		default:
			action = 0;
			topics = createQuery(Topic.class, cb, topicQuery, topicRoot);
			posts = createQuery(Post.class, cb, postQuery, postRoot);
			
			pagesCount = PagesHelper.calculatePagesCount(topics.size() + posts.size()) / 2;
			topics = PagesHelper.sublistForPage(topics, page);
			posts = PagesHelper.sublistForPage(posts, page);
			break;
		}
	}
	
	/**
	 * Generates and executes query depending on given class
	 */
	@SuppressWarnings("unchecked")
	private <T> List<T> createQuery(Class<T> cls, CriteriaBuilder cb, CriteriaQuery<?> query, Root<?> root) {
		HashMap <String, String> map = new HashMap<String, String>();
		
		// Fill map with values depending on a given class
		if (cls == User.class) {
			map.put("include-user-nickname", "name");
			map.put("include-user-real-name", "realName");
			query = query.orderBy(cb.desc(root.get("name")));
		} else {
			if (cls == Topic.class) { map.put("include-topic-name", "name"); }
			map.put("include-content", "text");
			map.put("include-author-name", "user");
			query = query.orderBy(cb.desc(root.get("date")));
		}
		
		// Define predicates
		Predicate p1 = cb.disjunction(); // For case when we've got some checked fields
		Predicate p2 = cb.disjunction(); // For case when we've got no checked fields
		
		// Prepare query text: apply regex and convert case if needed
		String qText;
		if (contextParams.containsKey("match-whole-query")) { qText = queryText; }
		else { qText = "%" + queryText + "%"; } // Like *queryText* regex
		
		Boolean ignoreCase = false;
		if(!contextParams.containsKey("match-case")) {
			ignoreCase = true;
			qText = qText.toUpperCase();
		}
		
		// Make actual predicates
		for (String key: map.keySet()) {
			Expression<String> toCompare;
			if (key.equals("include-author-name")) { toCompare = root.get("user").get("name"); } // We need to make an exception for this one
			else { toCompare = root.get(map.get(key)); }
			if(ignoreCase) { toCompare = cb.upper(toCompare); }
			
			Predicate p = cb.like(toCompare, qText);
			p2 = cb.or(p2, p);
			if (contextParams.containsKey(key)) { p1 = cb.or(p1, p); }
		}
		
		if (p1.getExpressions().size() == 0) { p1 = p2; }
		
		if (pTopic.getExpressions().size() != 0) {
			if (cls == Topic.class) { p1 = cb.and(pTopic, p1); }
			else if (cls == Post.class) { p1 = cb.and(pPost, p1); }
		}
		
		return (List<T>) em.createQuery(query.where(p1)).getResultList();
	}
	
	@PreDestroy
	public void destroy() {
		em.close();
	}

	/* Getters and setters */
	
	public String getQueryText() {
		return queryText;
	}

	public void setQueryText(String queryText) {
		this.queryText = queryText;
	}

	public List<Topic> getTopics() {
		return topics;
	}

	public void setTopics(List<Topic> topics) {
		this.topics = topics;
	}

	public List<Post> getPosts() {
		return posts;
	}

	public void setPosts(List<Post> posts) {
		this.posts = posts;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public HashMap<String, String> getCheckedSections() {
		return checkedSections;
	}

	public void setCheckedSections(HashMap<String, String> checkedSections) {
		this.checkedSections = checkedSections;
	}

	public HashMap<String, String> getCheckedSubsections() {
		return checkedSubsections;
	}

	public void setCheckedSubsections(HashMap<String, String> checkedSubsections) {
		this.checkedSubsections = checkedSubsections;
	}

	public HashMap<String, String> getCheckedBoxes() {
		return checkedBoxes;
	}

	public void setCheckedBoxes(HashMap<String, String> checkedBoxes) {
		this.checkedBoxes = checkedBoxes;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getPagesCount() {
		return pagesCount;
	}

	public void setPagesCount(int pagesCount) {
		this.pagesCount = pagesCount;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}
	
}

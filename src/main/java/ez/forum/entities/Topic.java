package ez.forum.entities;

import java.io.Serializable;
import javax.persistence.*;
import ez.forum.util.BBCodeConverter;
import ez.forum.util.PagesHelper;
import ez.forum.util.SHARED_OBJECTS;
import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the topics database table.
 * 
 */
@Entity
@Table(name="topics")
@NamedQuery(name="Topic.findAll", query="SELECT t FROM Topic t")
public class Topic implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	private Timestamp date;

	private String name;

	private String text;

	//bi-directional many-to-one association to Post
	@OneToMany(fetch=FetchType.LAZY, mappedBy="topicBean", cascade=CascadeType.ALL)
	@OrderBy("date DESC")
	private List<Post> posts;

	//bi-directional many-to-one association to TopicReviewRequest
	@OneToMany(fetch=FetchType.LAZY, mappedBy="topicBean", cascade=CascadeType.ALL)
	@OrderBy("id DESC")
	private List<TopicReviewRequest> topicReviewRequests;

	//bi-directional many-to-one association to Subsection
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="subsection")
	private Subsection subsectionBean;

	//bi-directional many-to-one association to User
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="author")
	private User user;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="locked_by")
	private User lockedBy;
	
	@Transient
	private BBCodeConverter conv = SHARED_OBJECTS.bbConverter;

	public Topic() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Timestamp getDate() {
		return this.date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return this.text;
	}
	
	/**
	 * Returns getText() with escaped HTML entities and formatted BBCode.
	 */
	public String getFormattedText() {
		return conv.toHtml(this.text);
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<Post> getPosts() {
		return this.posts;
	}

	public void setPosts(List<Post> posts) {
		this.posts = posts;
	}

	public Post addPost(Post post) {
		getPosts().add(post);
		post.setTopicBean(this);

		return post;
	}

	public Post removePost(Post post) {
		getPosts().remove(post);
		post.setTopicBean(null);

		return post;
	}

	public List<TopicReviewRequest> getTopicReviewRequests() {
		return this.topicReviewRequests;
	}

	public void setTopicReviewRequests(List<TopicReviewRequest> topicReviewRequests) {
		this.topicReviewRequests = topicReviewRequests;
	}
	
	public TopicReviewRequest getTopicReviewByUserId(Long id) {
		if (id == null) { return null; }
		for (TopicReviewRequest request: this.getTopicReviewRequests()) {
			if (request.getUser().getId() == id) { return request; }
		}
		return null;
	}

	public Subsection getSubsectionBean() {
		return this.subsectionBean;
	}

	public void setSubsectionBean(Subsection subsectionBean) {
		this.subsectionBean = subsectionBean;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public User getLockedBy() {
		return this.lockedBy;
	}
	
	public void setLockedBy(User lockedBy) {
		this.lockedBy = lockedBy;
	}
	
	/**
	 * @return Page number on which this topic should be.
	 */
	public int getPageNumber() {
		List<Topic> topics = this.getSubsectionBean().getTopics();
		for (int i = 0; i < topics.size(); i++) {
			if (topics.get(i).id == this.id) { return PagesHelper.getPageOfItem(i); }
		}
		return 0;
	}

}
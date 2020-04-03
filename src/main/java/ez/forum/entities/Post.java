package ez.forum.entities;

import java.io.Serializable;
import javax.persistence.*;
import ez.forum.util.BBCodeConverter;
import ez.forum.util.PagesHelper;
import ez.forum.util.SHARED_OBJECTS;
import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the posts database table.
 * 
 */
@Entity
@Table(name="posts")
@NamedQuery(name="Post.findAll", query="SELECT p FROM Post p")
public class Post implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	private Timestamp date;

	private String text;

	//bi-directional many-to-one association to PostReviewRequest
	@OneToMany(fetch=FetchType.LAZY, mappedBy="postBean", cascade=CascadeType.ALL)
	@OrderBy("id DESC")
	private List<PostReviewRequest> postReviewRequests;

	//bi-directional many-to-one association to Topic
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="topic")
	private Topic topicBean;

	//bi-directional many-to-one association to User
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="author")
	private User user;
	
	@Transient
	private BBCodeConverter conv = SHARED_OBJECTS.bbConverter;

	public Post() {
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

	public String getText() {
		return this.text;
	}
	
	/*
	 * Returns getText() with escaped HTML entities and formatted BBCode.
	 */
	public String getFormattedText() {
		return conv.toHtml(this.text);
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<PostReviewRequest> getPostReviewRequests() {
		return this.postReviewRequests;
	}

	public void setPostReviewRequests(List<PostReviewRequest> postReviewRequests) {
		this.postReviewRequests = postReviewRequests;
	}
	
	public PostReviewRequest getPostReviewByUserId(Long id) {
		if (id == null) { return null; }
		for (PostReviewRequest request: this.getPostReviewRequests()) {
			if (request.getUser().getId() == id) { return request; }
		}
		return null;
	}

	public Topic getTopicBean() {
		return this.topicBean;
	}

	public void setTopicBean(Topic topicBean) {
		this.topicBean = topicBean;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * @return Page number on which this post should be.
	 */
	public int getPageNumber() {
		List<Post> posts = this.getTopicBean().getPosts();
		for (int i = 0; i < posts.size(); i++) {
			if (posts.get(i).id == this.id) { return PagesHelper.getPageOfItem(i); }
		}
		return 0;
	}

}
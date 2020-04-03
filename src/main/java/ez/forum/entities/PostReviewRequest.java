package ez.forum.entities;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the post_review_request database table.
 * 
 */
@Entity
@Table(name="post_review_requests")
@NamedQuery(name="PostReviewRequest.findAll", query="SELECT p FROM PostReviewRequest p")
public class PostReviewRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	private String reason;

	//bi-directional many-to-one association to Post
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="post")
	private Post postBean;

	//bi-directional many-to-one association to User
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="sender")
	private User user;

	public PostReviewRequest() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getReason() {
		return this.reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Post getPostBean() {
		return this.postBean;
	}

	public void setPostBean(Post postBean) {
		this.postBean = postBean;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
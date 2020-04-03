package ez.forum.entities;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the topic_review_requests database table.
 * 
 */
@Entity
@Table(name="topic_review_requests")
@NamedQuery(name="TopicReviewRequest.findAll", query="SELECT t FROM TopicReviewRequest t")
public class TopicReviewRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	private String reason;

	//bi-directional many-to-one association to Topic
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="topic")
	private Topic topicBean;

	//bi-directional many-to-one association to User
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="sender")
	private User user;

	public TopicReviewRequest() {
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

}
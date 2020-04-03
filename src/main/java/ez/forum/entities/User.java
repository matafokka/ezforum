package ez.forum.entities;

import java.io.File;
import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.persistence.*;

import ez.forum.util.SHARED_OBJECTS;
import ez.forum.util.AES.AES;
import ez.forum.util.Email.EmailSender;

import java.util.LinkedHashMap;
import java.util.List;


/**
 * The persistent class for the users database table.
 * 
 */
@Entity
@Table(name="users")
@NamedQuery(name="User.findAll", query="SELECT u FROM User u ORDER BY u.name DESC")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Transient
	public static String confirmationText = "Confirmation in progress.";
	
	@Transient
	private LinkedHashMap<Integer, String> rankList = new LinkedHashMap<Integer, String>();

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(name="ban_reason")
	private String banReason;

	private byte[] email;

	private String name;

	private byte[] password;

	private Integer rank;

	@Column(name="real_name")
	private String realName;
	
	private String theme;
	
	private String locale;

	//bi-directional many-to-one association to Complaint
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="sender")
	private List<Complaint> sendedComplaints;

	//bi-directional many-to-one association to Complaint
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="receiver")
	private List<Complaint> receivedComplaints;

	//bi-directional many-to-one association to PostReviewRequest
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="user")
	private List<PostReviewRequest> postReviewRequests;

	//bi-directional many-to-one association to Post
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="user")
	private List<Post> posts;

	//bi-directional many-to-one association to TopicReviewRequest
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="user")
	private List<TopicReviewRequest> topicReviewRequests;

	//bi-directional many-to-one association to Topic
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="user")
	private List<Topic> topics;

	public User() {
		rankList.put(4, "misc.ranks.user");
		rankList.put(3, "misc.ranks.moderator");
		rankList.put(2, "misc.ranks.admin");
		rankList.put(1, "misc.ranks.superAdmin");
		rankList.put(0, "misc.ranks.creator");
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * 
	 * @return
	 * Ban reason if it's set or empty string (not null) otherwise.
	 */
	public String getBanReason() {
		if (this.banReason == null) { return ""; }
		if (this.banReason.contains(User.confirmationText)) { return User.confirmationText; }
		return this.banReason;
	}

	/**
	 * Sets ban reason.
	 * 
	 * Does NOT remove complaints.
	 * 
	 * @param banReason
	 */
	public void setBanReason(String banReason) {
		this.banReason = banReason;
	}
	
	/**
	 * Sets ban reason AND removes complaints
	 * @param banReason - reason to ban this user
	 */
	public void ban(String banReason) {
		this.setBanReason(banReason);

		EntityManager em = SHARED_OBJECTS.emfactory.createEntityManager();
		
		EntityTransaction trans = em.getTransaction();
		trans.begin();
		em.createQuery("DELETE FROM Complaint c WHERE c.receiver = " + this.id).executeUpdate();
		trans.commit();
		
		trans = em.getTransaction();
		trans.begin();
		
		trans.commit();
		
		em.close();
	}

	public byte[] getEmail() {
		return this.email;
	}

	public void setEmail(byte[] email) {
		this.email = email;
	}
	
	/**
	 * Returns decrypted email.
	 * @return
	 */
	public String getDecryptedEmail() {
		return AES.decrypt(this.email, AES.masterKey);
	}
	
	/**
	 * Encrypts and sets given email. Makes this user unconfirmed by generating and setting confirmation code, and temporarily banning this user.
	 * <br>
	 * Also sends confirmation message to the given email.
	 * <br><br>
	 * !!! <b>WARNING:</b> Do NOT use this method for unpersisted users !!!
	 * 
	 * @param email - email
	 * 
	 * @return
	 * true - if a confirmation message has been successfully sent.<br>
	 * false - otherwise. In this case, new email is not being set.
	 */
	public Boolean setAndEncryptEmail(String email) {
		// Generate code
		String code = String.valueOf(this.id) + "_" + String.valueOf(
				Math.random() * 9000000 + 1000000
				);
		byte[] codeEnc = AES.encrypt(code, AES.masterKey);
		
		// Convert encrypted code to string literally
		// Because some RDBMS can't stand \0 in strings :/
		StringBuffer codeBuffer = new StringBuffer(";");
		for (byte c: codeEnc) {
			codeBuffer.append(c);
			codeBuffer.append(';');
		}
		
		if (email == null || !EmailSender.sendConfirmationMessage(email, code)) { return false; }
		
		this.setEmail(AES.encrypt(email, AES.masterKey));
		// Use ban reason for holding confirmation code.
		// Dirty, but kinda effective than keeping a whole column for that.
		this.setBanReason(User.confirmationText + codeBuffer.toString());
		return true;
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getPassword() {
		return this.password;
	}

	public void setPassword(byte[] password) {
		this.password = password;
	}
	
	/**
	 * Returns password if providedPassword is valid.
	 * @param providedPassword - password provided by a user.
	 * @return
	 */
	public String getDecryptedPassword(String providedPassword) {
		return AES.decrypt(this.password, providedPassword + AES.masterKey);
	}
	
	/**
	 * Encrypts and sets password.
	 * @param password - password.
	 */
	public void setAndEncryptPassword(String password) {
		this.setPassword(AES.encrypt(password, password + AES.masterKey));
	}

	public Integer getRank() {
		return this.rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}
	
	public String getRankName() {
		return this.rankList.get(this.rank);
	}
	
	public String getRankStyle() {
		String style = "";
		switch (this.rank) {
			case 0:
				style = "creator";
				break;
			case 1:
				style = "super-admin";
				break;
			case 2:
				style = "admin";
				break;
			case 3:
				style = "moderator";
				break;
			case 4:
				style = "user";
				break;
		}
		return style;
	}

	public String getRealName() {
		return this.realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}
	
	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public List<Complaint> getSendedComplaints() {
		return this.sendedComplaints;
	}

	public void setSendedComplaints(List<Complaint> sendedComplaints) {
		this.sendedComplaints = sendedComplaints;
	}

	public Complaint addSendedComplaints(Complaint sendedComplaint) {
		getSendedComplaints().add(sendedComplaint);
		sendedComplaint.setSender(this);

		return sendedComplaint;
	}

	public Complaint removeSendedComplaints(Complaint sendedComplaint) {
		getSendedComplaints().remove(sendedComplaint);
		sendedComplaint.setSender(null);

		return sendedComplaint;
	}

	public List<Complaint> getReceivedComplaints() {
		return this.receivedComplaints;
	}

	public void setReceivedComplaints(List<Complaint> receivedComplaints) {
		this.receivedComplaints = receivedComplaints;
	}

	public Complaint addreceivedComplaints(Complaint receivedComplaint) {
		getReceivedComplaints().add(receivedComplaint);
		receivedComplaint.setReceiver(this);

		return receivedComplaint;
	}

	public Complaint removereceivedComplaints(Complaint receivedComplaint) {
		getReceivedComplaints().remove(receivedComplaint);
		receivedComplaint.setReceiver(null);

		return receivedComplaint;
	}

	public List<PostReviewRequest> getPostReviewRequests() {
		return this.postReviewRequests;
	}

	public void setPostReviewRequests(List<PostReviewRequest> postReviewRequests) {
		this.postReviewRequests = postReviewRequests;
	}

	public PostReviewRequest addPostReviewRequest(PostReviewRequest postReviewRequest) {
		getPostReviewRequests().add(postReviewRequest);
		postReviewRequest.setUser(this);

		return postReviewRequest;
	}

	public PostReviewRequest removePostReviewRequest(PostReviewRequest postReviewRequest) {
		getPostReviewRequests().remove(postReviewRequest);
		postReviewRequest.setUser(null);

		return postReviewRequest;
	}

	public List<Post> getPosts() {
		return this.posts;
	}

	public void setPosts(List<Post> posts) {
		this.posts = posts;
	}

	public Post addPost(Post post) {
		getPosts().add(post);
		post.setUser(this);

		return post;
	}

	public Post removePost(Post post) {
		getPosts().remove(post);
		post.setUser(null);

		return post;
	}

	public List<TopicReviewRequest> getTopicReviewRequests() {
		return this.topicReviewRequests;
	}

	public void setTopicReviewRequests(List<TopicReviewRequest> topicReviewRequests) {
		this.topicReviewRequests = topicReviewRequests;
	}

	public TopicReviewRequest addTopicReviewRequest(TopicReviewRequest topicReviewRequest) {
		getTopicReviewRequests().add(topicReviewRequest);
		topicReviewRequest.setUser(this);

		return topicReviewRequest;
	}

	public TopicReviewRequest removeTopicReviewRequest(TopicReviewRequest topicReviewRequest) {
		getTopicReviewRequests().remove(topicReviewRequest);
		topicReviewRequest.setUser(null);

		return topicReviewRequest;
	}

	public List<Topic> getTopics() {
		return this.topics;
	}

	public void setTopics(List<Topic> topics) {
		this.topics = topics;
	}

	public Topic addTopic(Topic topic) {
		getTopics().add(topic);
		topic.setUser(this);

		return topic;
	}

	public Topic removeTopic(Topic topic) {
		getTopics().remove(topic);
		topic.setUser(null);

		return topic;
	}
	
	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	/*
	 * Returns path to avatar if one exists.
	 * Returns path to the default avatar otherwise.
	 */
	public String getAvatarPath() {
		String part = "uploads/avatars/"; // Actual path to the avatar used in markup
		File path = new File(
				FacesContext.
				getCurrentInstance().
				getExternalContext().
				getRealPath("/") // Get path to the app's deployment
				+ "/" + part // "uploads" directory will be in root's directory
				);
		
		for (File f : path.listFiles()) {
			String name = f.getName();
			if (name.contains(this.id.toString())) {
				return part + name;
			}
		}
		return "img/avatar.svg";
	}
	
	/**
	 * @returns
	 * Confirmation code for this user.<br>
	 * Or empty string, if this user is confirmed.
	 */
	public String getConfirmationCode() {
		// Get code from ban reason
		String code;
		try { code = this.banReason.substring(User.confirmationText.length()); }
		catch (IndexOutOfBoundsException e) { return ""; }
		
		// Parse bytes from code
		char[] codeChars = code.toCharArray();
		byte[] codeBytes = new byte[codeChars.length];
		
		int lastPos = 0;
		int index = 0;
		for (int i = 1; i < codeChars.length; i++) {
			if (codeChars[i] == ';') {
				try {codeBytes[index] = Byte.valueOf(code.substring(lastPos + 1, i)); }
				catch (NumberFormatException e) { return ""; }
				lastPos = i;
				index++;
			}
		}
		
		// Truncate array
		byte[] finalArr = new byte[index];
		System.arraycopy(codeBytes, 0, finalArr, 0, index);
		
		return AES.decrypt(finalArr, AES.masterKey);
	}
	
	/**
	 * Yeah, I know, it's replicating arrays. But profile page needs to iterate over this data.
	 * 
	 * @return HashMap where:
	 * 	Key - actual rank.
	 * 	Value - name (representation) of this rank.
	 */
	public LinkedHashMap<Integer, String> getRankList() {
		return this.rankList;
	}

}
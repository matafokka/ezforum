package ez.forum.entities;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the complaints database table.
 * 
 */
@Entity
@Table(name="complaints")
@NamedQuery(name="Complaint.findAll", query="SELECT c FROM Complaint c")
public class Complaint implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	private String reason;

	//bi-directional many-to-one association to User
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="sender")
	private User sender;

	//bi-directional many-to-one association to User
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="usr")
	private User receiver;

	public Complaint() {
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

	public User getSender() {
		return this.sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	public User getReceiver() {
		return this.receiver;
	}

	public void setReceiver(User receiver) {
		this.receiver = receiver;
	}

}
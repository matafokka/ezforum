package ez.forum.entities;

import java.io.Serializable;
import javax.persistence.*;

import java.util.List;


/**
 * The persistent class for the subsections database table.
 * 
 */
@Entity
@Table(name="subsections")
@NamedQuery(name="Subsection.findAll", query="SELECT s FROM Subsection s")
public class Subsection implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	private String name;

	//bi-directional many-to-one association to Section
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="section")
	private Section sectionBean;

	//bi-directional many-to-one association to Topic
	@OneToMany(fetch=FetchType.LAZY, mappedBy="subsectionBean", cascade=CascadeType.ALL)
	@OrderBy("date DESC")
	private List<Topic> topics;

	public Subsection() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Section getSectionBean() {
		return this.sectionBean;
	}

	public void setSectionBean(Section sectionBean) {
		this.sectionBean = sectionBean;
	}

	public List<Topic> getTopics() {
		return this.topics;
	}

	public void setTopics(List<Topic> topics) {
		this.topics = topics;
	}

	public Topic addTopic(Topic topic) {
		getTopics().add(topic);
		topic.setSubsectionBean(this);

		return topic;
	}

	public Topic removeTopic(Topic topic) {
		getTopics().remove(topic);
		topic.setSubsectionBean(null);

		return topic;
	}

}
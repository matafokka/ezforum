package ez.forum.entities;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * Entity for sections
 * 
 */
@Entity
@Table(name="sections")
@NamedQuery(name="Section.findAll", query="SELECT s FROM Section s ORDER BY s.id ASC")
public class Section implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	private String name;

	//bi-directional many-to-one association to Subsection
	@OneToMany(fetch=FetchType.LAZY, mappedBy="sectionBean", cascade=CascadeType.ALL)
	@OrderBy("id ASC")
	private List<Subsection> subsections;

	public Section() {
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

	public List<Subsection> getSubsections() {
		return this.subsections;
	}

	public void setSubsections(List<Subsection> subsections) {
		this.subsections = subsections;
	}

	public Subsection addSubsection(Subsection subsection) {
		getSubsections().add(subsection);
		subsection.setSectionBean(this);

		return subsection;
	}

	public Subsection removeSubsection(Subsection subsection) {
		getSubsections().remove(subsection);
		subsection.setSectionBean(null);

		return subsection;
	}

}
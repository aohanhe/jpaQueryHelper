package ao.jpaQueryHelper;

import java.io.Serializable;
import javax.persistence.*;



import java.util.Date;
import java.util.List;
import java.util.Optional;


/**
 * The persistent class for the author database table.
 * 
 */
@Entity
@NamedQuery(name="Author.findAll", query="SELECT a FROM Author a")
public class Author implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	@Temporal(TemporalType.DATE)
	@Column(name="date_of_birth")
	private Date dateOfBirth;

	private Integer distinguished;

	@Column(name="first_name")
	private String firstName;

	@Column(name="last_name")
	private String lastName;

	@Column(name="year_of_birth")
	private int yearOfBirth;

	

	public Author() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getDateOfBirth() {
		return this.dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public Integer getDistinguished() {
		return this.distinguished;
	}

	public void setDistinguished(Integer distinguished) {
		this.distinguished = distinguished;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getYearOfBirth() {
		return this.yearOfBirth;
	}

	public void setYearOfBirth(int yearOfBirth) {
		this.yearOfBirth = yearOfBirth;
	}


}
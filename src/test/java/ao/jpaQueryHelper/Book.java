package  ao.jpaQueryHelper;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the book database table.
 * 
 */
@Entity
@NamedQuery(name="Book.findAll", query="SELECT b FROM Book b")
public class Book implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	@Column(name="language_id")
	private int languageId;

	@Column(name="published_in")
	private int publishedIn;

	private String title;

	//bi-directional many-to-one association to Author
	@ManyToOne(fetch=FetchType.LAZY)	
	private Author author;

	public Book() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLanguageId() {
		return this.languageId;
	}

	public void setLanguageId(int languageId) {
		this.languageId = languageId;
	}

	public int getPublishedIn() {
		return this.publishedIn;
	}

	public void setPublishedIn(int publishedIn) {
		this.publishedIn = publishedIn;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Author getAuthor() {
		return this.author;
	}

	public void setAuthor(Author author) {
		this.author = author;
	}
	
	@Override
	public String toString() {
		return String.format("id:%d title:%s ", this.id,this.title);
	}

}
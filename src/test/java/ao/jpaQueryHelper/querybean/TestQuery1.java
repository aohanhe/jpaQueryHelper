package ao.jpaQueryHelper.querybean;

import ao.jpaQueryHelper.BaseJpaQueryBean;
import ao.jpaQueryHelper.Book;
import ao.jpaQueryHelper.annotations.CanNull;
import ao.jpaQueryHelper.annotations.Expression;
import ao.jpaQueryHelper.annotations.JpaQueryBean;
import ao.jpaQueryHelper.annotations.Or;

@JpaQueryBean(entityClass = Book.class, entityName = "Book")
public class TestQuery1 extends BaseJpaQueryBean{
	
	//@CanNull
	@Or
	private Integer id;
	@Expression("o.title like :title")
	@Or
	private String title;
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	

}

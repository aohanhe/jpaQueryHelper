package ao.jpaQueryHelper;

import io.swagger.annotations.ApiModelProperty;

/**
 * 带分页的查询Bean
 * @author aohanhe
 *
 */
public class PageJpaQueryBean extends BaseJpaQueryBean{	
	
	@ApiModelProperty(name="页码号",value="页码号，从1开始")
	private int page=1;
	@ApiModelProperty(name="返回记录数",value="返回记录数")
	private int limit=20;
	
	
	
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	
}

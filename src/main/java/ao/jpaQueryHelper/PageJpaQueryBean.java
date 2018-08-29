package ao.jpaQueryHelper;

/**
 * 带分页的查询Bean
 * @author aohanhe
 *
 */
public class PageJpaQueryBean extends BaseJpaQueryBean{
	private PageInfo pager=new PageInfo();

	public PageInfo getPager() {
		return pager;
	}

	public void setPager(PageInfo pager) {
		this.pager = pager;
	}
	
}

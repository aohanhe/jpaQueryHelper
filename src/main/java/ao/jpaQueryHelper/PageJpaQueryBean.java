package ao.jpaQueryHelper;

/**
 * ����ҳ�Ĳ�ѯBean
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

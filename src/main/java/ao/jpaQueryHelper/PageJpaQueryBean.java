package ao.jpaQueryHelper;

import io.swagger.annotations.ApiModelProperty;

/**
 * ����ҳ�Ĳ�ѯBean
 * @author aohanhe
 *
 */
public class PageJpaQueryBean extends BaseJpaQueryBean{	
	
	@ApiModelProperty(name="ҳ���",value="ҳ��ţ���1��ʼ")
	private int page=1;
	@ApiModelProperty(name="���ؼ�¼��",value="���ؼ�¼��")
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

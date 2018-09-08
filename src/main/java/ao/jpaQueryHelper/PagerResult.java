package ao.jpaQueryHelper;

import java.util.List;

import reactor.core.publisher.Flux;

/**
 * 取得页面结果
 * @author aohanhe
 *
 */
public class PagerResult <T> {
	private long total;
	private int pageCount;
	private int currentPage;
	private Flux<T> dataList;
	
	
	public PagerResult(int page,int pageSize,long total,Flux<T> list) {
		this.currentPage=page;
		this.total = total;
		this.pageCount=(int) Math.ceil((double)total/(double)pageSize);
		this.dataList=list;
	}
	
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	
	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public Flux<T> getDataList() {
		return dataList;
	}

	public void setDataList(Flux<T> dataList) {
		this.dataList = dataList;
	}

}

package ao.jpaQueryHelper;

import com.querydsl.core.types.Expression;

public interface IRootPathsManger {
	/**
	 * ͨ��Id ȡ�� ʵ��ı��ʽ
	 * @param id
	 * @return
	 */
	public  Expression<?> getRootPathById(int id);

}

package ao.jpaQueryHelper;

import com.querydsl.core.types.Expression;

public interface IRootPathsManger {
	/**
	 * 通过Id 取得 实体的表达式
	 * @param id
	 * @return
	 */
	public  Expression<?> getRootPathById(int id);

}

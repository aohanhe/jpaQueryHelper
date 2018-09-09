package ao.jpaQueryHelper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Jpa��ѯBean,���ڱ�������һ��Jpa�Ĳ�ѯBean
 * @author aohan
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JpaQueryBean {
	/**
	 * ��Ӧ��ʵ����
	 * @return
	 */
	Class<?> entityClass();	
	/**
	 * ��IRootPathsManger ������������ ʵ��·��
	 * @return
	 */
	int mainRootPath() default 0;
}

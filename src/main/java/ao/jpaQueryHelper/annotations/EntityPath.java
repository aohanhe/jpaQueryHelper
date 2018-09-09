package ao.jpaQueryHelper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * �ֶι���entity��path
 * @author aohan
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityPath {
	/**
	 * ·��ֵ ���� author.name
	 * @return
	 */
	String name() default "";
	/**
	 * ��IRootPathsManger ������������ ʵ��·��
	 * @return
	 */
	int rootPath() default 0;
}

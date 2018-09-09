package ao.jpaQueryHelper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Jpa查询Bean,用于标明类是一个Jpa的查询Bean
 * @author aohan
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JpaQueryBean {
	/**
	 * 对应的实体类
	 * @return
	 */
	Class<?> entityClass();	
}

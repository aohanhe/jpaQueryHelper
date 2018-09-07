package ao.jpaQueryHelper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * dsl查询表达式方法
 * @author aohanhe
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DslPredicateMehtod {
	String value();
	/**
	 * 查询条件的返回函数
	 * @return
	 */
	String orderMehtod();
}

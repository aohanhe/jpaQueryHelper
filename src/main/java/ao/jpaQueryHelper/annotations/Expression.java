package ao.jpaQueryHelper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 条件表达式  例如(o.name like ${author.name})
 * 需要进行加入参数赋值的位置以${}标识，系统会在构建参数时自动构建parameter
 * @author aohan
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Expression {
	String value();
}

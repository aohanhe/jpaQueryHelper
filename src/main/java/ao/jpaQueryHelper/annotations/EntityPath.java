package ao.jpaQueryHelper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段关联entity的path
 * @author aohan
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityPath {
	/**
	 * 路径值 例如 author.name
	 * @return
	 */
	String name() default "";
	/**
	 * 与IRootPathsManger 中主健关联的 实体路径
	 * @return
	 */
	int rootPath() default 0;
}

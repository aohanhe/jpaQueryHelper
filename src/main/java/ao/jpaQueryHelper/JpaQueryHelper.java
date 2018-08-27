package ao.jpaQueryHelper;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import org.springframework.util.StringUtils;

import ao.jpaQueryHelper.annotations.CanNull;
import ao.jpaQueryHelper.annotations.EntityPath;
import ao.jpaQueryHelper.annotations.Expression;
import ao.jpaQueryHelper.annotations.JpaQueryBean;
import ao.jpaQueryHelper.annotations.Or;

/**
 * jpa查询助手
 * @author aohanhe
 *
 */
public class JpaQueryHelper {
	
	
	public static void createQueryFromBean(BaseJpaQueryBean queryBean) throws JpaQueryHelperException {
		//1 取得关联的实体对象的类及名称
		if(queryBean==null)
			throw new JpaQueryHelperException("参数queryBean不允许为空");
		
		var beanInfo=queryBean.getClass().getAnnotation(JpaQueryBean.class);
		if(beanInfo==null)
			throw new JpaQueryHelperException(
					String.format("bean类%s没有添加@JpaQueryBean注解", queryBean.getClass().getName()));
		
		Class<?> entityClass=beanInfo.entityClass();
		String entityName=beanInfo.entityName();
		
		if(StringUtils.isEmpty(entityName))
			entityName=entityClass.getSimpleName();
		
		//2 创建查询主体
		String query=String.format("select o from %s as o ", entityName);
		
		//3 添加查询条件
		
		
	}
	
	private static String createWhereFromBean(BaseJpaQueryBean queryBean) {
		
		var classInfo=queryBean.getClass();
		Stream.of(classInfo.getDeclaredFields())
			.map(JpaQueryHelper::createConditionFromField);
		
		
	}
	
	/**
	 * 从字段配置中取得条件字串
	 * @param field
	 * @return
	 */
	private static String createConditionFromField(Field field) {
		boolean isAnd=!field.isAnnotationPresent(Or.class);
		boolean isCanNull=field.isAnnotationPresent(CanNull.class);
		String name=field.getName();
		String path=field.getName();
		
		EntityPath entityPath=field.getAnnotation(EntityPath.class);
		if(entityPath!=null) {
			path=entityPath.value();
		}
		
		String exp=null;
		Expression expression=field.getAnnotation(Expression.class);
		if(expression!=null) {
			exp=expression.value().trim();
		}else {
			exp=String.format("(%s=:%s)", name,name);
		}
		
		
		if(!exp.startsWith("(")) exp=String.format("(%s)", exp);
		
		exp=isAnd?" and ":" or "+exp;
		
		return exp;				
	}

}

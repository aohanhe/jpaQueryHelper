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
 * jpa��ѯ����
 * @author aohanhe
 *
 */
public class JpaQueryHelper {
	
	
	public static void createQueryFromBean(BaseJpaQueryBean queryBean) throws JpaQueryHelperException {
		//1 ȡ�ù�����ʵ�������༰����
		if(queryBean==null)
			throw new JpaQueryHelperException("����queryBean������Ϊ��");
		
		var beanInfo=queryBean.getClass().getAnnotation(JpaQueryBean.class);
		if(beanInfo==null)
			throw new JpaQueryHelperException(
					String.format("bean��%sû�����@JpaQueryBeanע��", queryBean.getClass().getName()));
		
		Class<?> entityClass=beanInfo.entityClass();
		String entityName=beanInfo.entityName();
		
		if(StringUtils.isEmpty(entityName))
			entityName=entityClass.getSimpleName();
		
		//2 ������ѯ����
		String query=String.format("select o from %s as o ", entityName);
		
		//3 ��Ӳ�ѯ����
		
		
	}
	
	private static String createWhereFromBean(BaseJpaQueryBean queryBean) {
		
		var classInfo=queryBean.getClass();
		Stream.of(classInfo.getDeclaredFields())
			.map(JpaQueryHelper::createConditionFromField);
		
		
	}
	
	/**
	 * ���ֶ�������ȡ�������ִ�
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

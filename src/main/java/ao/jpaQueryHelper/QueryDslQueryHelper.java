package ao.jpaQueryHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import java.util.stream.Collectors;


import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;

import com.querydsl.core.types.Constant;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;

import ao.jpaQueryHelper.annotations.CanNull;
import ao.jpaQueryHelper.annotations.DslPredicateMehtod;

import ao.jpaQueryHelper.annotations.JpaQueryBean;
import ao.jpaQueryHelper.annotations.Or;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * ����querydsl�Ĳ�ѯ����
 * 
 * @author aohanhe
 *
 */
public class QueryDslQueryHelper {
	private static Logger logger = LoggerFactory.getLogger(QueryDslQueryHelper.class);

	/**
	 * ͨ����ѯ�������ɲ�ѯ������
	 * @param query
	 * @param queryBean
	 * @return
	 * @throws JpaQueryHelperException
	 */
	public static JPAQuery<Tuple> initPredicateAndSortFromQueryBean(JPAQuery<Tuple> query,BaseJpaQueryBean queryBean) throws JpaQueryHelperException {
		
		var beanInfo = queryBean.getClass().getAnnotation(JpaQueryBean.class);
		if (beanInfo == null)
			throw new JpaQueryHelperException(
					String.format("bean��%sû�����@JpaQueryBeanע��", queryBean.getClass().getName()));

		Class<?> entityClass = beanInfo.entityClass();
		String entityName = beanInfo.entityName();
		

		// ͨ����ѯbean���ɲ�ѯ����
		var pre=createWhereFromQueryBean(queryBean, entityClass, entityName);
		
		var reQuery=query.where(pre);
		
		// ͨ����ѯbean������������
		var orders=createOrderFromQueryBean(queryBean, entityClass, entityName);
		if(orders!=null)
			reQuery=reQuery.orderBy((OrderSpecifier[]) orders.toArray());
		
		// ��ӷ�ҳ����
		if(queryBean instanceof PageJpaQueryBean) {
			var page=((PageJpaQueryBean)queryBean).getPage();
			var size=((PageJpaQueryBean)queryBean).getLimit();
			
			int skip = (page - 1) * size;

			logger.debug("set pagesize=" + size + " page=" + page);
			reQuery=reQuery.offset(skip).limit(size);
		}
				
		
		return reQuery;		
	}

	/**
	 * ���ɲ�ѯ����
	 * @param queryBean
	 * @param entityType
	 * @param entityName
	 * @return
	 */
	private static BooleanBuilder createWhereFromQueryBean(BaseJpaQueryBean queryBean,Class<?> entityType,String entityName) {
		
		//���������ɲ�ѯ����
		var classInfo = queryBean.getClass();
		var flux = Flux.fromArray(classInfo.getDeclaredFields())
				.map(v -> createConditionFromField(queryBean, v,entityType,entityName))
				.sort((item1,item2)->item1.block().getLeft().compareTo(item2.block().getLeft()));
		
		BooleanBuilder builder=new BooleanBuilder();
		
		//�������������
		flux.subscribe(v->{
			var item=v.block();
			boolean isAnd=item.getLeft().equals('a');
			
			if(isAnd)
				builder.and(item.getRight());
			else
				builder.or(item.getRight());	
			
		});
		
		return builder;
	}
	
	/**
	 * ͨ��bean������������
	 * @param queryBean
	 * @param entityType
	 * @param entityName
	 * @return 
	 */
	private static List<OrderSpecifier> createOrderFromQueryBean(BaseJpaQueryBean queryBean,Class<?> entityType,String entityName) {
		if (queryBean.getOrders() == null)
			return null;

		return Flux.fromIterable(queryBean.getOrders())
				.map(v -> createOrderProperty(v,queryBean,entityType,entityName))
				.collect(Collectors.toList()).block();		
	}
	
	/**
	 * ����һ����ѯ����
	 * @param classInfo
	 * @param order
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static OrderSpecifier createOrderProperty(Order order,BaseJpaQueryBean queryBean,Class<?> entityType, String entityName) {
		try {
			Class<?> classType=queryBean.getClass();
			var field = classType.getDeclaredField(order.getProperty());
			field.setAccessible(true);
			
			var dslPr=field.getAnnotation(DslPredicateMehtod.class);
			//����û�û�ж��壬��ʹ��Ĭ�ϵĶ���
			OrderSpecifier re=null;
			if(dslPr==null||Strings.isBlank(dslPr.orderMehtod())) {
				Path root=Expressions.path(entityType, entityName);
				Path nodePath=Expressions.path(field.getType(),root, order.getProperty());
				re=new OrderSpecifier(order.getDirection()==Direction.ASC?
						com.querydsl.core.types.Order.ASC:com.querydsl.core.types.Order.DESC
						, nodePath);
			}else {
				var method=classType.getMethod(dslPr.orderMehtod());
				re=(OrderSpecifier) method.invoke(queryBean);
			}
				
			return re;

		} catch (Exception ex) {
			logger.error("����������������:" + ex.getMessage(), ex);
			throw Exceptions.propagate(new IllegalStateException("����������������:" + ex.getMessage(), ex));
		}
	}
	

	@SuppressWarnings("rawtypes")
	private static Mono<Pair<Character, Predicate>>  createConditionFromField(BaseJpaQueryBean queryBean, 
			Field field,Class<?> entityType,String entityName) {

		try {
			boolean isAnd = !field.isAnnotationPresent(Or.class);
			boolean isCanNull = field.isAnnotationPresent(CanNull.class);
			String name = field.getName();
			String path = field.getName();

			field.setAccessible(true);// ���ó������
			Object value = field.get(queryBean);
			
			if (value == null && !isCanNull)
				return Mono.never();
			
			DslPredicateMehtod method=field.getAnnotation(DslPredicateMehtod.class);
			
			
			Predicate re=null;
			
			//���û�����û������������ʽ
			if(method==null) {
				Path root=Expressions.path(entityType, entityName);				
				Path nodePath=Expressions.path(field.getType(),root, name);
				Constant constant=(Constant) Expressions.constant(value);
				re=Expressions.predicate(Ops.EQ, nodePath,constant);
			}else {
				//��������˱��ʽ����������ú������ɱ��ʽ
				Method preMethod = queryBean.getClass().getMethod(method.value());
				re=(Predicate) preMethod.invoke(queryBean);
			}
			
			return Mono.just(Pair.of(isAnd?'a':'o', re));

		} catch (Exception ex) {
			logger.error("����queryBean�Ĳ�ѯ����ʱ����:" + ex.getMessage(), ex);
			throw Exceptions.propagate(ex);
		}

	}

}

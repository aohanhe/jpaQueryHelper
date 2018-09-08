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
 * 基于querydsl的查询助手
 * 
 * @author aohanhe
 *
 */
public class QueryDslQueryHelper {
	private static Logger logger = LoggerFactory.getLogger(QueryDslQueryHelper.class);

	/**
	 * 通过查询对象生成查询处理器
	 * @param query
	 * @param queryBean
	 * @return
	 * @throws JpaQueryHelperException
	 */
	public static JPAQuery<Tuple> initPredicateAndSortFromQueryBean(JPAQuery<Tuple> query,BaseJpaQueryBean queryBean) throws JpaQueryHelperException {
		
		var beanInfo = queryBean.getClass().getAnnotation(JpaQueryBean.class);
		if (beanInfo == null)
			throw new JpaQueryHelperException(
					String.format("bean类%s没有添加@JpaQueryBean注解", queryBean.getClass().getName()));

		Class<?> entityClass = beanInfo.entityClass();
		String entityName = beanInfo.entityName();
		

		// 通过查询bean生成查询条件
		var pre=createWhereFromQueryBean(queryBean, entityClass, entityName);
		
		var reQuery=query.where(pre);
		
		// 通过查询bean生成排序条件
		var orders=createOrderFromQueryBean(queryBean, entityClass, entityName);
		if(orders!=null)
			reQuery=reQuery.orderBy((OrderSpecifier[]) orders.toArray());
		
		// 添加分页处理
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
	 * 生成查询条件
	 * @param queryBean
	 * @param entityType
	 * @param entityName
	 * @return
	 */
	private static BooleanBuilder createWhereFromQueryBean(BaseJpaQueryBean queryBean,Class<?> entityType,String entityName) {
		
		//从属性生成查询条件
		var classInfo = queryBean.getClass();
		var flux = Flux.fromArray(classInfo.getDeclaredFields())
				.map(v -> createConditionFromField(queryBean, v,entityType,entityName))
				.sort((item1,item2)->item1.block().getLeft().compareTo(item2.block().getLeft()));
		
		BooleanBuilder builder=new BooleanBuilder();
		
		//将所有条件组合
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
	 * 通过bean生成排序条件
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
	 * 创建一个查询条件
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
			//如果用户没有定义，则使用默认的定义
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
			logger.error("创建排序条件错误:" + ex.getMessage(), ex);
			throw Exceptions.propagate(new IllegalStateException("创建排序条件错误:" + ex.getMessage(), ex));
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

			field.setAccessible(true);// 设置充许访问
			Object value = field.get(queryBean);
			
			if (value == null && !isCanNull)
				return Mono.never();
			
			DslPredicateMehtod method=field.getAnnotation(DslPredicateMehtod.class);
			
			
			Predicate re=null;
			
			//如果没有配置基础的条件表达式
			if(method==null) {
				Path root=Expressions.path(entityType, entityName);				
				Path nodePath=Expressions.path(field.getType(),root, name);
				Constant constant=(Constant) Expressions.constant(value);
				re=Expressions.predicate(Ops.EQ, nodePath,constant);
			}else {
				//如果配置了表达式函数，则调用函数生成表达式
				Method preMethod = queryBean.getClass().getMethod(method.value());
				re=(Predicate) preMethod.invoke(queryBean);
			}
			
			return Mono.just(Pair.of(isAnd?'a':'o', re));

		} catch (Exception ex) {
			logger.error("构建queryBean的查询条件时出错:" + ex.getMessage(), ex);
			throw Exceptions.propagate(ex);
		}

	}

}

package ao.jpaQueryHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.Entity;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;

import com.querydsl.core.types.Constant;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;

import ao.jpaQueryHelper.annotations.CanNull;
import ao.jpaQueryHelper.annotations.DslPredicateMehtod;
import ao.jpaQueryHelper.annotations.EntityPath;
import ao.jpaQueryHelper.annotations.JpaQueryBean;
import ao.jpaQueryHelper.annotations.Like;
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
	 * 
	 * @param query
	 * @param queryBean
	 * @return
	 * @throws JpaQueryHelperException
	 */
	public static <T> JPAQuery<Tuple> initPredicateAndSortFromQueryBean(JPAQuery<Tuple> query,
			BaseJpaQueryBean queryBean, Expression<?> mainExpression) throws JpaQueryHelperException {

		var beanInfo = queryBean.getClass().getAnnotation(JpaQueryBean.class);
		if (beanInfo == null)
			throw new JpaQueryHelperException(
					String.format("bean��%sû�����@JpaQueryBeanע��", queryBean.getClass().getName()));

		Class<?> entityClass = beanInfo.entityClass();
		
		// ͨ����ѯbean���ɲ�ѯ����
		var pre = createWhereFromQueryBean(queryBean, entityClass,  mainExpression);

		var reQuery = query.where(pre);

		// ͨ����ѯbean������������
		var orders = createOrderFromQueryBean(queryBean, entityClass, mainExpression);
		if (orders != null)
			reQuery = reQuery.orderBy((OrderSpecifier[]) orders.toArray());

		// ��ӷ�ҳ����
		if (queryBean instanceof PageJpaQueryBean) {
			var page = ((PageJpaQueryBean) queryBean).getPage();
			var size = ((PageJpaQueryBean) queryBean).getLimit();

			int skip = (page - 1) * size;

			logger.debug("set pagesize=" + size + " page=" + page);
			reQuery = reQuery.offset(skip).limit(size);
		}

		return reQuery;
	}

	/**
	 * ���ɲ�ѯ����
	 * 
	 * @param queryBean
	 * @param entityType
	 * @param entityName
	 * @return
	 */
	private static BooleanBuilder createWhereFromQueryBean(BaseJpaQueryBean queryBean, Class<?> entityType,
			 Expression<?> mainExpression) {

		// ���������ɲ�ѯ����
		var classInfo = queryBean.getClass();

		var flux = Flux.fromArray(classInfo.getDeclaredFields())
				.map(v -> createConditionFromField(queryBean, v, entityType,  mainExpression))
				.filter(v -> v.isPresent())
				.sort((item1, item2) -> item1.get().getLeft().compareTo(item2.get().getLeft()));

		BooleanBuilder builder = new BooleanBuilder();

		// �������������
		flux.subscribe(v -> {
			if (v == null)
				return;
			var item = v.get();
			boolean isAnd = item.getLeft().equals('a');

			if (isAnd)
				builder.and(item.getRight());
			else
				builder.or(item.getRight());

		}, ex -> {
			System.out.println(ex.getMessage());
		});

		return builder;
	}

	/**
	 * ͨ��bean������������
	 * 
	 * @param queryBean
	 * @param entityType
	 * @param entityName
	 * @return
	 */
	private static List<OrderSpecifier> createOrderFromQueryBean(BaseJpaQueryBean queryBean, Class<?> entityType,
			Expression<?> mainExpression) {
		if (queryBean.getOrders() == null)
			return null;

		return Flux.fromIterable(queryBean.getOrders())
				.map(v -> createOrderProperty(v, queryBean, entityType, mainExpression)).collect(Collectors.toList())
				.block();
	}

	/**
	 * ����һ����ѯ����
	 * 
	 * @param classInfo
	 * @param order
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static OrderSpecifier createOrderProperty(Order order, BaseJpaQueryBean queryBean,
			Class<?> entityType,Expression<?> mainExpression) {
		try {
			Class<?> classType = queryBean.getClass();
			var field = classType.getDeclaredField(order.getProperty());
			field.setAccessible(true);
			
			String name=order.getProperty();

			var entityPath=field.getAnnotation(EntityPath.class);
			if(entityPath!=null)
				name=entityPath.value();
			

			var dslPr = field.getAnnotation(DslPredicateMehtod.class);
			// ����û�û�ж��壬��ʹ��Ĭ�ϵĶ���
			OrderSpecifier re = null;
			if (dslPr == null || Strings.isBlank(dslPr.orderMehtod())) {
				Path root = (Path)mainExpression;
				Path nodePath = Expressions.path(field.getType(), root, name);
				re = new OrderSpecifier(order.getDirection() == Direction.ASC ? com.querydsl.core.types.Order.ASC
						: com.querydsl.core.types.Order.DESC, nodePath);
			} else {
				var method = classType.getMethod(dslPr.orderMehtod());
				re = (OrderSpecifier) method.invoke(queryBean);
			}

			return re;

		} catch (Exception ex) {
			logger.error("����������������:" + ex.getMessage(), ex);
			throw Exceptions.propagate(new IllegalStateException("����������������:" + ex.getMessage(), ex));
		}
	}

	@SuppressWarnings("rawtypes")
	private static Optional<Pair<Character, Predicate>> createConditionFromField(BaseJpaQueryBean queryBean,
			Field field, Class<?> entityType,  Expression<?> mainExpression) {

		try {
			boolean isAnd = !field.isAnnotationPresent(Or.class);
			boolean isCanNull = field.isAnnotationPresent(CanNull.class);
			boolean isLike = false;
			boolean isStartWith=true;
			
			var like=field.getAnnotation(Like.class);
			if(like!=null) {
				isLike=true;
				isStartWith=like.isStartWith();
			}
			
			String name = field.getName();
			String path = field.getName();
			
			// ���·�����޸��ˣ�ʹ���޸ĵ�·����
			var entityPath=field.getAnnotation(EntityPath.class);
			if(entityPath!=null)
				name=entityPath.value();

			field.setAccessible(true);// ���ó������
			Object value = field.get(queryBean);

			if (value == null && !isCanNull)
				return Optional.empty();

			boolean isStringAndEmpty = (value instanceof String) && Strings.isBlank(value.toString());

			if (isStringAndEmpty && !isCanNull)
				return Optional.empty();

			DslPredicateMehtod method = field.getAnnotation(DslPredicateMehtod.class);

			Predicate re = null;

			// ���û�����û������������ʽ
			if (method == null) {
				Path nodePath = Expressions.path(field.getType(), (Path) mainExpression, name);

				if (value == null || isStringAndEmpty) {
					//����ǿ�ֵ ʹ��is nullָ��
					re = Expressions.predicate(Ops.IS_NULL, nodePath);
				} else {
					Constant constant = (Constant) Expressions.constant(value);
					var op=isStartWith?Ops.STARTS_WITH:Ops.STRING_CONTAINS;
					re = Expressions.predicate(isLike ? op : Ops.EQ, nodePath, constant);
				}
			} else {
				// ��������˱��ʽ����������ú������ɱ��ʽ
				Method preMethod = queryBean.getClass().getMethod(method.value());
				re = (Predicate) preMethod.invoke(queryBean);
			}

			return Optional.of(Pair.of(isAnd ? 'a' : 'o', re));

		} catch (Exception ex) {
			logger.error("����queryBean�Ĳ�ѯ����ʱ����:" + ex.getMessage(), ex);
			throw Exceptions.propagate(ex);
		}

	}

}

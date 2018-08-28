package ao.jpaQueryHelper;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.util.StringUtils;

import com.jayway.jsonpath.Option;

import ao.jpaQueryHelper.annotations.CanNull;
import ao.jpaQueryHelper.annotations.EntityPath;
import ao.jpaQueryHelper.annotations.Expression;
import ao.jpaQueryHelper.annotations.JpaQueryBean;
import ao.jpaQueryHelper.annotations.Or;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

/**
 * jpa��ѯ����
 * 
 * @author aohanhe
 *
 */

public class JpaQueryHelper {

	private static Logger logger = LoggerFactory.getLogger(JpaQueryHelper.class);

	/**
	 * ͨ����ѯbean������ѯ����
	 * 
	 * @param em
	 * @param queryBean
	 * @param reClass
	 * @return
	 * @throws JpaQueryHelperException
	 */
	public static <T> TypedQuery<T> createQueryFromBean(EntityManager em, BaseJpaQueryBean queryBean, Class<T> reClass)
			throws JpaQueryHelperException {
		// 1 ȡ�ù�����ʵ�������༰����
		if (queryBean == null)
			throw new JpaQueryHelperException("����queryBean������Ϊ��");

		var beanInfo = queryBean.getClass().getAnnotation(JpaQueryBean.class);
		if (beanInfo == null)
			throw new JpaQueryHelperException(
					String.format("bean��%sû�����@JpaQueryBeanע��", queryBean.getClass().getName()));

		Class<?> entityClass = beanInfo.entityClass();
		String entityName = beanInfo.entityName();

		if (StringUtils.isEmpty(entityName))
			entityName = entityClass.getSimpleName();

		// 2 ������ѯ����
		String query = String.format("select o from %s as o ", entityName);

		// 3 ��Ӳ�ѯ����
		var condsTupe = createWhereFromBean(queryBean);
		String conds = condsTupe.getLeft().trim();
		if (!Strings.isEmpty(conds))
			conds = "where " + conds;

		String orders = createOrderStrFromBean(queryBean).trim();

		// ������ѯ����
		var queryStr = query + conds + orders;

		logger.debug("create query:" + queryStr);

		var res = em.createQuery(queryStr, reClass);
		condsTupe.getRight().forEach((path, value) -> {
			var hasParam=
					Flux.fromIterable(res.getParameters())
				.any(v->v.getName().equals(path)).block();
			
			if(hasParam) {
				res.setParameter(path, value);
			}
		});

		// ����Ƿ�ҳ��ѯ������ӷ�ҳ��Ϣ
		if (queryBean instanceof PageJpaQueryBean) {
			var pager = ((PageJpaQueryBean) queryBean).getPager();
			int skip = (pager.getPage() - 1) * pager.getSize();

			logger.debug("set pagesize=" + pager.getSize() + " page=" + pager.getPage());

			res.setMaxResults(pager.getSize());
			res.setFirstResult(skip);
		}

		return res;
	}

	/**
	 * ͨ��bean���󹹽���ѯ����
	 * 
	 * @param queryBean
	 * @return
	 * @throws Exception
	 */
	private static Pair<String, HashMap<String, Object>> createWhereFromBean(BaseJpaQueryBean queryBean) {

		HashMap<String, Object> pars = new HashMap<>();

		var classInfo = queryBean.getClass();
		var flux = Flux.fromArray(classInfo.getDeclaredFields()).map(v -> createConditionFromField(queryBean, v))
				.onErrorResume(v -> Flux.error(v)).filter(v -> v.isPresent());

		// ���ɲ�ѯ����
		var listCons = flux.map(v -> v.get().middle).collect(Collectors.toList()).block();
		listCons.sort((item1,item2)->item1.compareTo(item2));
		
		String strCons = Strings.join(listCons, ' ').trim();

		strCons = strCons.replaceFirst("(^or)|(^and)", "").trim();

		flux.subscribe(v -> {
			var item = v.get();
			pars.put(item.left, item.right);
		});

		return Pair.of(strCons, pars);
	}

	/**
	 * ���ֶ�������ȡ�������ִ�
	 * 
	 * @param field
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private static Optional<ImmutableTriple<String, String, Object>> createConditionFromField(
			BaseJpaQueryBean queryBean, Field field) {

		try {

			boolean isAnd = !field.isAnnotationPresent(Or.class);
			boolean isCanNull = field.isAnnotationPresent(CanNull.class);
			String name = field.getName();
			String path = field.getName();

			field.setAccessible(true);// ���ó������
			Object value = field.get(queryBean);

			if (value == null && !isCanNull)
				return Optional.empty();

			EntityPath entityPath = field.getAnnotation(EntityPath.class);
			if (entityPath != null) {
				path = entityPath.value();
			}

			String exp = value==null? String.format("(o.%s is null)", path) :String.format("(o.%s=:%s)", path, name);
			Expression expression = field.getAnnotation(Expression.class);
			if (expression != null) {
				exp = expression.value().trim();
			} 

			if (!exp.startsWith("("))
				exp = String.format("(%s)", exp);

			exp = String.format("%s %s", isAnd ? "and" : "or", exp);

			return Optional.of(ImmutableTriple.of(name, exp, value));
		} catch (Exception ex) {
			logger.error("����queryBean�Ĳ�ѯ����ʱ����:" + ex.getMessage(), ex);
			throw Exceptions.propagate(ex);
		}
	}

	/**
	 * ������������
	 * 
	 * @param queryBean
	 * @return
	 */
	private static String createOrderStrFromBean(BaseJpaQueryBean queryBean) {

		if (queryBean.getOrders() == null)
			return "";

		var list = Flux.fromIterable(queryBean.getOrders())
				.map(v -> JpaQueryHelper.createOrderProperty(queryBean.getClass(), v)).collect(Collectors.toList());
		

		return String.join(",", list.block());
	}

	/**
	 * ��������ؼ���
	 * 
	 * @param classInfo
	 * @param order
	 * @return
	 */
	private static String createOrderProperty(Class classInfo, Order order) {
		try {
			var field = classInfo.getField(order.getProperty());
			var entityPath = field.getAnnotation(EntityPath.class);
			String path = order.getProperty();
			if (entityPath != null)
				path = entityPath.value();

			return path + " " + order.getDirection().toString();

		} catch (Exception ex) {
			logger.error("����������������:" + ex.getMessage(), ex);
			throw Exceptions.propagate(new IllegalStateException("����������������:" + ex.getMessage(), ex));
		}
	}

}

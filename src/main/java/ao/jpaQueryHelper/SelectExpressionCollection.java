package ao.jpaQueryHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class SelectExpressionCollection <T>{
	private ArrayList<SelectExpressionItem<T>> list=new ArrayList<>();
	private Class<T> classType;
	protected Expression<T> mainExpression;
	
	
	/**
	 * 构造集合
	 * @param type
	 */
	public SelectExpressionCollection(Expression mainExpression,Class<T> type) {
		this.classType=type;
		this.mainExpression = mainExpression;
		
	}
	
	
	
	
	/**
	 * 选择表达式项
	 * @author aohanhe
	 *
	 */
	public static class SelectExpressionItem <K>{
		private String name;
		private Field field;
		private Expression express;
		
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Field getField() {
			return field;
		}
		public void setField(Field field) {
			this.field = field;
		}
		public Expression getExpress() {
			return express;
		}
		public void setExpress(Expression express) {
			this.express = express;
		}
		
		/**
		 * 选择表达式项构造
		 * @param name
		 * @param express
		 * @param classType
		 * @throws JpaQueryHelperException
		 * @throws NoSuchFieldException
		 * @throws SecurityException
		 */
		public SelectExpressionItem(String name,Expression express,Class<K> classType) 
				throws JpaQueryHelperException,  SecurityException {
			if(Strings.isBlank(name))
				throw new JpaQueryHelperException("参数name不允许为空");
			if(express==null)
				throw new JpaQueryHelperException("参数express不允许为空");
			if(classType==null)
				throw new JpaQueryHelperException("参数classType不允许为空");
			
			try {
				field=classType.getDeclaredField(name);
			} catch (NoSuchFieldException e) {
				throw new JpaQueryHelperException(String.format("实体类%s 没有这字段%s", classType.getName() ,name));
			}
			field.setAccessible(true);
			this.name=name;
			this.express=express;
		}
		
	}
	
	
	public void putItem(String name,Expression express) throws NoSuchFieldException, SecurityException, JpaQueryHelperException {
		
		this.list.add(new SelectExpressionItem<T>(name, express,this.classType));		
	}
	
	/**
	 * 处理扩展数据列表
	 * @return
	 */
	protected abstract Expression[] getExtendsExpression() ;
	
	/**
	 * 取得表达式的列表
	 * @return
	 */
	public Expression[] getExpressionArray() {
		var flux=Flux.fromIterable(this.list)
				.map(v->v.express).collect(Collectors.toList()).block();
		
		var exteds=this.getExtendsExpression();
		if(exteds==null)
			exteds=new Expression[] {};
		
		var res=Flux.concat(Flux.just(this.mainExpression),
				Flux.fromIterable(this.list)
					.map(v->v.express)
					,Flux.just(exteds))			
				.collect(Collectors.toList())
				.block();
		return res.toArray(new Expression[]{});
	}
	
	/**
	 * 处理扩展数据提取
	 * @param tuple
	 */
	protected abstract void onFectionExtendsDataItem(T item,Tuple tuple);
	
	/**
	 * 从选择列配置信息中提取赋值后的数据项
	 * @param tuple
	 * @param itemPression
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public T fectionDataInItem(Tuple tuple) 
			throws IllegalArgumentException, IllegalAccessException {
		T item=tuple.get(this.mainExpression);
		
		for(var dataItem:this.list) {
			
			dataItem.field.set(item, tuple.get(dataItem.express));
		}		
		
		onFectionExtendsDataItem(item, tuple);
		
		return item;
	}
	
	/**
	 * 添加扩展的联结查询
	 * @param query
	 * @return
	 */
	public JPAQuery<Tuple>  addExtendsLeftJoin(JPAQuery<Tuple> query) {
		return onAddExtendsLeftJoin(query);
	}
	
	/**
	 * 处理扩展联结查询
	 * @param query
	 * @return
	 */
	protected abstract JPAQuery<Tuple>   onAddExtendsLeftJoin(JPAQuery<Tuple> query);
	
}

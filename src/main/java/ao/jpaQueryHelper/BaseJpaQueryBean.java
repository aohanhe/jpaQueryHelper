package ao.jpaQueryHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import javax.persistence.Tuple;

import org.springframework.data.domain.Sort.Order;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import net.minidev.json.annotate.JsonIgnore;

/**
 * 基础jpa查询bean
 * @author aohan
 *
 */
public class BaseJpaQueryBean implements Serializable{
	
	/**
	 * 排序字段列表
	 */
	private String[] sorts;
	
	/**
	 * 处理后的排序字段
	 */
	@JsonIgnore
	private List<Order> orders;
	
	/**
	 * 对排序条件进行初始化
	 */
	public void init() {
		orders=new ArrayList<>();
		if(sorts!=null) {
			Stream.of(sorts)
				.map(v->{
					if(v.endsWith("+"))
						return v;
					return v;
				});
		}
	}

	public String[] getSorts() {
		return sorts;
	}

	public void setSorts(String[] sorts) {
		this.sorts = sorts;
	}

	public List<Order> getOrders() {
		return orders;
	}

	
	
}

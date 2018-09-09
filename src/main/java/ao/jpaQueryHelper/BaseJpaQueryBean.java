package ao.jpaQueryHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import org.springframework.data.domain.Sort.Order;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;


/**
 * 基础jpa查询bean
 * @author aohan
 *
 */
public class BaseJpaQueryBean implements Serializable{ 
	@ApiModelProperty(name="排序条件",value="排序条件 例+id,+表示升序 - 表示降序")
	private String sort;
	

	/**
	 * 处理后的排序字段
	 */
	@JsonIgnore
	private List<Order> orders;
	
	/**
	 * 对排序条件进行初始化
	 */
	@PostConstruct
	public void init() {
		orders=new ArrayList<>();
		if(sort!=null) {
			orders=Stream.of(sort.split(","))
				.map(v->{
					if(v.startsWith("+"))
						return Order.asc(v.substring(0,v.length()-1));
					if(v.startsWith("-"))
						return Order.desc(v.substring(0, v.length()-1));
					return Order.asc(v);
				}).collect(Collectors.toList());
		}		
	}

	

	public List<Order> getOrders() {
		return orders;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}
	
}

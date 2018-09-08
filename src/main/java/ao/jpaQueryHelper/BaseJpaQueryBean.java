package ao.jpaQueryHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import org.springframework.data.domain.Sort.Order;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * ����jpa��ѯbean
 * @author aohan
 *
 */
public class BaseJpaQueryBean implements Serializable{ 
	
	/**
	 * �����ֶ��б�
	 */
	private String[] sorts;
	
	/**
	 * �����������ֶ�
	 */
	@JsonIgnore
	private List<Order> orders;
	
	/**
	 * �������������г�ʼ��
	 */
	@PostConstruct
	public void init() {
		orders=new ArrayList<>();
		if(sorts!=null) {
			orders=Stream.of(sorts)
				.map(v->{
					if(v.endsWith("+"))
						return Order.asc(v.substring(0,v.length()-1));
					if(v.endsWith("-"))
						return Order.desc(v.substring(0, v.length()-1));
					return Order.asc(v);
				}).collect(Collectors.toList());
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

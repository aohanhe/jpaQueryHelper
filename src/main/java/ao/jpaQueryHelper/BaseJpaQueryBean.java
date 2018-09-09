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
 * ����jpa��ѯbean
 * @author aohan
 *
 */
public class BaseJpaQueryBean implements Serializable{ 
	@ApiModelProperty(name="��������",value="�������� ��+id,+��ʾ���� - ��ʾ����")
	private String sort;
	

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

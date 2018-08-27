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

package ao.jpaQueryHelper;

import java.io.IOException;
import java.util.Arrays;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import ao.jpaQueryHelper.querybean.TestQuery1;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JpaQueryHelperApplicationTests {
	@Autowired
	private EntityManager em;

	@Test
	public void contextLoads() {
	}
	
	@Test
	public void test1() throws JpaQueryHelperException, IOException, NoSuchFieldException, SecurityException {
		TestQuery1 querybean=new TestQuery1();
		
	
		
		querybean.setTitle("%");
		
		var orders="id-,title+";
		
		querybean.setSort(orders);
		
		querybean.init();
		
		var re=JpaQueryHelper.createQueryFromBean(em, querybean, Book.class);
		
		
		
		
	}

}

package ao.jpaQueryHelper;

/**
 * JpaQueryHelperException�쳣
 * @author aohanhe
 *
 */
public class JpaQueryHelperException extends Exception {
	public JpaQueryHelperException(String msg) {
		super(msg);
	}
	
	public JpaQueryHelperException(String msg,Throwable ex) {
		super(msg,ex);
	}

}

package ao.jpaQueryHelper;

/**
 * JpaQueryHelperException“Ï≥£
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

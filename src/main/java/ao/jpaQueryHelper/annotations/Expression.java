package ao.jpaQueryHelper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * �������ʽ  ����(o.name like ${author.name})
 * ��Ҫ���м��������ֵ��λ����${}��ʶ��ϵͳ���ڹ�������ʱ�Զ�����parameter
 * @author aohan
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Expression {
	String value();
}

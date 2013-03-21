package edu.swmed.qbrc.auth.cashmac.shared.util;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;
//import javax.ws.rs.NameBinding;

//@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Securable {
	String value() default "";
}
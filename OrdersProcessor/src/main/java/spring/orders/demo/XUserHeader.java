package spring.orders.demo;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

@Retention(RUNTIME)
@Target({ METHOD, ElementType.TYPE })
@Parameter(name = "x-USER",
	in = ParameterIn.HEADER,
	required = true,
	allowEmptyValue = false,
	description = "User identifier (username or email)")
public @interface XUserHeader {

}

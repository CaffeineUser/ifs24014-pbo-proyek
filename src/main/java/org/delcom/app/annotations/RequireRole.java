package org.delcom.app.annotations;

import org.delcom.app.entities.UserRole;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE}) 
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    UserRole[] value(); // List role yang diizinkan (misal: ADMIN, STAFF)
}
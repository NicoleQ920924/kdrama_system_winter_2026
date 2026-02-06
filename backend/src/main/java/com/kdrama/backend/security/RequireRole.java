package com.kdrama.backend.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.kdrama.backend.enums.Role;

@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    Role[] value();
}

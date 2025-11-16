package com.kay.music.enumeration;

import lombok.Getter;

/**
 * @author Kay
 * @date 2025/11/16 16:31
 */
@Getter
public enum RoleEnum {

    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER");

    private final String role;

    RoleEnum(String role) {
        this.role = role;
    }
}

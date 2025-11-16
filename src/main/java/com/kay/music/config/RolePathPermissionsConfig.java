package com.kay.music.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
/**
 * @Description: 把配置文件里的“角色-路径”权限表读进来，存到 Map<String, List<String>> 里
 * @Author: Kay
 * @date:   2025/11/16 17:14
 */
@Component
@ConfigurationProperties(prefix = "role-path-permissions")
public class RolePathPermissionsConfig {

    private Map<String, List<String>> permissions;

    public Map<String, List<String>> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, List<String>> permissions) {
        this.permissions = permissions;
    }
}

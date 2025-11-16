package com.kay.music.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Description: 角色权限管理器，根据当前角色和请求路径，判断有没有访问权限。
 * @Author: Kay
 * @date:   2025/11/16 17:13
 */
@Component
public class RolePermissionManager {

    private final RolePathPermissionsConfig rolePathPermissionsConfig;

    @Autowired
    public RolePermissionManager(RolePathPermissionsConfig rolePathPermissionsConfig) {
        this.rolePathPermissionsConfig = rolePathPermissionsConfig;
    }

    // 判断当前角色是否有权限访问请求的路径
    public boolean hasPermission(String role, String requestURI) {
        // 1. 从配置类中拿到整张权限表
        Map<String, List<String>> permissions = rolePathPermissionsConfig.getPermissions();
        // 2. 根据角色拿到这个角色允许访问的路径前缀列表
        List<String> allowedPaths = permissions.get(role);
        // 3. 如果有配置
        if (allowedPaths != null) {
            // 4. 遍历所有允许访问的路径
            for (String path : allowedPaths) {
                // 5. 用 startsWith 判断当前请求的路径是不是以某个允许路径为前缀
                if (requestURI.startsWith(path)) {
                    return true; // 匹配成功，有权限
                }
            }
        }
        // 6. 没匹配上或没配置，默认无权限
        return false;
    }
}


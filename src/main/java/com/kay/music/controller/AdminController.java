package com.kay.music.controller;

import com.kay.music.pojo.dto.AdminDTO;
import com.kay.music.result.Result;
import com.kay.music.service.IAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Kay
 * @date 2025/11/15 21:44
 */
@RestController
@RequestMapping("/admin")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "管理员接口")
public class AdminController {

    private final IAdminService adminService;

    @PostMapping("/register")
    @Operation(summary = "管理员注册")
    public Result register(@RequestBody @Valid AdminDTO adminDTO) {
        log.info("管理员注册：{}",adminDTO);
        return adminService.register(adminDTO);
    }
}

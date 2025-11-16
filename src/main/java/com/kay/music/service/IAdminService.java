package com.kay.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kay.music.pojo.dto.AdminDTO;
import com.kay.music.pojo.entity.Admin;
import com.kay.music.result.Result;

/**
 * @author Kay
 * @date 2025/11/16 13:05
 */
public interface IAdminService extends IService<Admin> {

    Result register(AdminDTO adminDTO);


    Result login(AdminDTO adminDTO);
}

package com.kay.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kay.music.pojo.dto.BannerDTO;
import com.kay.music.pojo.entity.Banner;
import com.kay.music.pojo.vo.BannerVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;

import java.util.List;

/**
 * @author Kay
 * @date 2025/11/22 15:31
 */
public interface IBannerService extends IService<Banner> {
    Result<PageResult<Banner>> getAllBanners(BannerDTO bannerDTO);

    Result addBanner(String bannerUrl);

    Result updateBanner(Long bannerId, String bannerUrl);

    Result updateBannerStatus(Long bannerId, Integer bannerStatus);

    Result deleteBanner(Long bannerId);

    Result deleteBanners(List<Long> bannerIds);

    Result<List<BannerVO>> getBannerList();
}

package com.kay.music.controller;

import com.kay.music.pojo.dto.BannerDTO;
import com.kay.music.pojo.entity.Banner;
import com.kay.music.pojo.vo.BannerVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;
import com.kay.music.service.IBannerService;
import com.kay.music.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Kay
 * @date 2025/11/22 15:30
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "轮播图接口")
public class BannerController {

    private final IBannerService bannerService;
    private final MinioService minioService;

    /**
     * 获取轮播图列表
     *
     * @return 轮播图列表
     */
    @Operation(summary = "获取轮播图列表")
    @PostMapping("/admin/getAllBanners")
    public Result<PageResult<Banner>> getAllBanners(@RequestBody BannerDTO bannerDTO) {
        return bannerService.getAllBanners(bannerDTO);
    }

    /**
     * 添加轮播图
     *
     * @param banner 轮播图
     * @return 结果
     */
    @Operation(summary = "添加轮播图")
    @PostMapping("/admin/addBanner")
    public Result addBanner(@RequestParam("banner") MultipartFile banner) {
        String bannerUrl = minioService.uploadFile(banner, "banners");
        return bannerService.addBanner(bannerUrl);
    }

    /**
     * 更新轮播图
     *
     * @param banner 轮播图
     * @return 结果
     */
    @Operation(summary = "更新轮播图")
    @PatchMapping("/admin/updateBanner/{id}")
    public Result updateBanner(@PathVariable("id") Long bannerId, @RequestParam("banner") MultipartFile banner) {
        String bannerUrl = minioService.uploadFile(banner, "banners");
        return bannerService.updateBanner(bannerId, bannerUrl);
    }

    /**
     * 更新轮播图状态
     *
     * @param bannerStatus 轮播图状态
     * @return 结果
     */
    @Operation(summary = "更新轮播图状态")
    @PatchMapping("/admin/updateBannerStatus/{id}")
    public Result updateBannerStatus(@PathVariable("id") Long bannerId, @RequestParam("status") Integer bannerStatus) {
        return bannerService.updateBannerStatus(bannerId, bannerStatus);
    }

    /**
     * 删除轮播图
     *
     * @param bannerId 轮播图id
     * @return 结果
     */
    @Operation(summary = "删除轮播图")
    @DeleteMapping("/admin/deleteBanner/{id}")
    public Result deleteBanner(@PathVariable("id") Long bannerId) {
        return bannerService.deleteBanner(bannerId);
    }

    /**
     * 批量删除轮播图
     *
     * @param bannerIds 轮播图id列表
     * @return 结果
     */
    @Operation(summary = "批量删除轮播图")
    @DeleteMapping("/admin/deleteBanners")
    public Result deleteBanners(@RequestBody List<Long> bannerIds) {
        return bannerService.deleteBanners(bannerIds);
    }

    /**
     * 获取轮播图列表（用户端）
     *
     * @return 轮播图列表
     */
    @Operation(summary = "获取轮播图列表（用户端）")
    @GetMapping("/banner/getBannerList")
    public Result<List<BannerVO>> getBannerList() {
        return bannerService.getBannerList();
    }


}

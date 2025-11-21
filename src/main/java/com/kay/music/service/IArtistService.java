package com.kay.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kay.music.pojo.dto.ArtistAddDTO;
import com.kay.music.pojo.dto.ArtistDTO;
import com.kay.music.pojo.dto.ArtistUpdateDTO;
import com.kay.music.pojo.entity.Artist;
import com.kay.music.pojo.vo.ArtistDetailVO;
import com.kay.music.pojo.vo.ArtistVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @author Kay
 * @date 2025/11/21 11:11
 */
public interface IArtistService extends IService<Artist> {
    Result<Long> getAllArtistsCount(Integer gender, String area);

    Result<PageResult<Artist>> getAllArtistsAndDetail(ArtistDTO artistDTO);

    Result addArtist(ArtistAddDTO artistAddDTO);

    Result updateArtist(ArtistUpdateDTO artistUpdateDTO);

    Result updateArtistAvatar(Long artistId, String avatarUrl);

    Result deleteArtist(Long artistId);

    Result deleteArtists(List<Long> artistIds);

    Result<PageResult<ArtistVO>> getAllArtists(ArtistDTO artistDTO);

    Result<List<ArtistVO>> getRandomArtists();

    Result<ArtistDetailVO> getArtistDetail(Long artistId);
}

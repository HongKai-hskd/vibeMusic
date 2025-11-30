package com.kay.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kay.music.pojo.dto.SongAddDTO;
import com.kay.music.pojo.dto.SongAndArtistDTO;
import com.kay.music.pojo.dto.SongDTO;
import com.kay.music.pojo.dto.SongUpdateDTO;
import com.kay.music.pojo.entity.Song;
import com.kay.music.pojo.vo.SongAdminVO;
import com.kay.music.pojo.vo.SongDetailVO;
import com.kay.music.pojo.vo.SongVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Kay
 * @date 2025/11/19 23:39
 */
public interface ISongService extends IService<Song> {

    Result<PageResult<SongVO>> getAllSongsForGuest(SongDTO songDTO);

    Result<PageResult<SongVO>> getAllSongsForUser(SongDTO songDTO, Long userId);

    Result<List<SongVO>> getRecommendedSongs();

    Result<SongDetailVO> getSongDetail(Long songId, HttpServletRequest request);

    Result<Long> getAllSongsCount();

    Result<PageResult<SongAdminVO>> getAllSongsByArtist(SongAndArtistDTO songDTO);

    Result addSong(SongAddDTO songAddDTO);

    Result updateSong(SongUpdateDTO songUpdateDTO);

    Result updateSongCover(Long songId, String coverUrl);

    Result updateSongAudio(Long songId, String audioUrl, String duration);

    Result deleteSong(Long songId);

    Result deleteSongs(List<Long> songIds);

    Result addSongByFile(MultipartFile audio);

    Result batchAddSongByFile(MultipartFile[] audios);
}

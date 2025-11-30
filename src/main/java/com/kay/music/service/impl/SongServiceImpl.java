package com.kay.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kay.music.constant.MessageConstant;
import com.kay.music.enumeration.LikeStatusEnum;
import com.kay.music.mapper.*;
import com.kay.music.pojo.dto.SongAddDTO;
import com.kay.music.pojo.dto.SongAndArtistDTO;
import com.kay.music.pojo.dto.SongDTO;
import com.kay.music.pojo.dto.SongUpdateDTO;
import com.kay.music.pojo.entity.*;
import com.kay.music.pojo.vo.SongAdminVO;
import com.kay.music.pojo.vo.SongDetailVO;
import com.kay.music.pojo.vo.SongVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;
import com.kay.music.service.ISongService;
import com.kay.music.service.MinioService;
import com.kay.music.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Kay
 * @date 2025/11/19 23:39
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "songCache")
@Slf4j
public class SongServiceImpl extends ServiceImpl<SongMapper, Song> implements ISongService {

    private final SongMapper songMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final MinioService minioService;
    private final ArtistMapper artistMapper;

    /**
     * @Description: 游客版：只需要歌曲列表 + 默认 likeStatus = DEFAULT , 结果对所有“未登录用户”通用，可以全局缓存
     * @Author: Kay
     * @date:   2025/11/20 20:06
     */
    @Cacheable(
            key = "#songDTO.pageNum + '-' + #songDTO.pageSize + '-' + " +
                    "#songDTO.songName + '-' + #songDTO.artistName + '-' + #songDTO.album"
    )
    public Result<PageResult<SongVO>> getAllSongsForGuest(SongDTO songDTO) {

        // 1. 查询歌曲列表（分页）
        //Page = 分页请求参数（包含当前页和每页数量）
        //IPage = 分页后的完整结果（包含列表 + 总条数 + 分页信息）
        Page<SongVO> page = new Page<>(songDTO.getPageNum(), songDTO.getPageSize());
        IPage<SongVO> songPage = songMapper.getSongsWithArtist(
                page,
                songDTO.getSongName(),
                songDTO.getArtistName(),
                songDTO.getAlbum()
        );
        // 1.2 没有查到
        if (songPage.getRecords().isEmpty()) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }
        // 2. 默认全部设置为 未点赞
        List<SongVO> songVOList = songPage.getRecords().stream()
                .peek(songVO -> songVO.setLikeStatus(LikeStatusEnum.DEFAULT.getId()))
                .toList();

        return Result.success(new PageResult<>(songPage.getTotal(), songVOList));
    }

    /**
     * @Description: 登录用户版：在游客基础上增加“喜欢状态” , 注意：这里的 key 一定要包含 userId，否则会串用户！
     * @Author: Kay
     * @date:   2025/11/20 20:07
     */
    @Cacheable(
            key = "#userId + '-' + #songDTO.pageNum + '-' + #songDTO.pageSize + '-' + " +
                    "#songDTO.songName + '-' + #songDTO.artistName + '-' + #songDTO.album"
    )
    public Result<PageResult<SongVO>> getAllSongsForUser(SongDTO songDTO, Long userId) {

        Page<SongVO> page = new Page<>(songDTO.getPageNum(), songDTO.getPageSize());
        IPage<SongVO> songPage = songMapper.getSongsWithArtist(
                page,
                songDTO.getSongName(),
                songDTO.getArtistName(),
                songDTO.getAlbum()
        );

        if (songPage.getRecords().isEmpty()) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }
        // 2. 默认全部设置为 未点赞
        List<SongVO> songVOList = songPage.getRecords().stream()
                .peek(songVO -> songVO.setLikeStatus(LikeStatusEnum.DEFAULT.getId()))
                .toList();
        // 3. 获取用户收藏的歌曲
        List<UserFavorite> favoriteSongs = userFavoriteMapper.selectList(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .eq(UserFavorite::getType, 0)
        );
        // 4. 找到自己点赞了的故去id
        Set<Long> favoriteSongIds = favoriteSongs.stream()
                .map(UserFavorite::getSongId)
                .collect(Collectors.toSet());

        for (SongVO songVO : songVOList) {
            if (favoriteSongIds.contains(songVO.getSongId())) {
                songVO.setLikeStatus(LikeStatusEnum.LIKE.getId());
            }
        }

        return Result.success(new PageResult<>(songPage.getTotal(), songVOList));
    }

    /**
     * @Description: 获取推荐歌曲, 推荐歌曲的数量为 20
     * @Author: Kay
     * @date:   2025/11/20 23:32
     */
    // TODO: 推荐方式需要修改，当前为随机推荐，后续可以基于用户行为、协同过滤等方式实现个性化推荐
    @Override
    public Result<List<SongVO>> getRecommendedSongs() {
        // 目前简化为随机推荐
        return Result.success(songMapper.getRandomSongsWithArtist());
    }

    /**
     * @Description: 获取歌曲详情，如果用户登录了，还需要关注是否点赞
     * @Author: Kay
     * @date:   2025/11/21 10:34
     */
    @Override
    public Result<SongDetailVO> getSongDetail(Long songId, HttpServletRequest request) {

        SongDetailVO songDetailVO = songMapper.getSongDetailById(songId);

        // 如果用户登录了，需要额外操作 ( 设置 这首歌 是否被用户 点赞)
        Long userId = ThreadLocalUtil.getUserId();
        if ( userId != null ) {
            // 获取用户收藏的歌曲
            UserFavorite favoriteSong = userFavoriteMapper.selectOne(new LambdaQueryWrapper<UserFavorite>()
                    .eq(UserFavorite::getUserId, userId)
                    .eq(UserFavorite::getType, 0)
                    .eq(UserFavorite::getSongId, songId));
            if (favoriteSong != null) {
                songDetailVO.setLikeStatus(LikeStatusEnum.LIKE.getId());
            }
        }

        return Result.success(songDetailVO);
    }

    /**
     * @Description: 获取所有歌曲的数量
     * @Author: Kay
     * @date:   2025/11/21 21:31
     */
    @Override
    public Result<Long> getAllSongsCount() {
        return Result.success(songMapper.selectCount(null));
    }

    /**
     * @Description: 获取歌手的所有歌曲
     * @Author: Kay
     * @date:   2025/11/21 21:36
     */
    @Override
    @Cacheable(key = "#songDTO.pageNum + '-' + #songDTO.pageSize + '-' + #songDTO.songName + '-' + #songDTO.album + '-' + #songDTO.artistId")
    public Result<PageResult<SongAdminVO>> getAllSongsByArtist(SongAndArtistDTO songDTO) {
        // 分页查询
        Page<SongAdminVO> page = new Page<>(songDTO.getPageNum(), songDTO.getPageSize());
        IPage<SongAdminVO> songPage = songMapper.getSongsWithArtistName(page, songDTO.getArtistId(), songDTO.getSongName(), songDTO.getAlbum());

        if (songPage.getRecords().isEmpty()) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }

        return Result.success(new PageResult<>(songPage.getTotal(), songPage.getRecords()));
    }

    /**
     * @Description: 添加歌曲信息
     * @Author: Kay
     * @date:   2025/11/21 21:41
     */
    @Override
    @CacheEvict(cacheNames = "songCache", allEntries = true)
    public Result addSong(SongAddDTO songAddDTO) {
        Song song = new Song();
        BeanUtils.copyProperties(songAddDTO, song);

        // 插入歌曲记录
        if (songMapper.insert(song) == 0) {
            return Result.error(MessageConstant.ADD + MessageConstant.FAILED);
        }
        // 获取刚插入的歌曲记录
        Song songInDB = songMapper.selectOne(new QueryWrapper<Song>()
                .eq("artist_id", songAddDTO.getArtistId())
                .eq("name", songAddDTO.getSongName())
                .eq("album", songAddDTO.getAlbum())
                .orderByDesc("id")
                .last("LIMIT 1"));

        if (songInDB == null) {
            return Result.error(MessageConstant.SONG + MessageConstant.NOT_FOUND);
        }

        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 更新歌曲信息
     * @Author: Kay
     * @date:   2025/11/21 21:48
     */
    @Override
    @CacheEvict(cacheNames = "songCache", allEntries = true)
    public Result updateSong(SongUpdateDTO songUpdateDTO) {

        // 查询数据库中是否存在该歌曲
        Song songInDB = songMapper.selectById(songUpdateDTO.getSongId());
        if (songInDB == null) {
            return Result.error(MessageConstant.SONG + MessageConstant.NOT_FOUND);
        }

        // 更新歌曲基本信息
        Song song = new Song();
        BeanUtils.copyProperties(songUpdateDTO, song);
        if (songMapper.updateById(song) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 更新歌曲封面
     * @Author: Kay
     * @date:   2025/11/22 15:01
     */
    @Override
    @CacheEvict(cacheNames = "songCache", allEntries = true)
    public Result updateSongCover(Long songId, String coverUrl) {
        Song song = songMapper.selectById(songId);
        String cover = song.getCoverUrl();
        if (cover != null && !cover.isEmpty()) {
            minioService.deleteFile(cover);
        }

        song.setCoverUrl(coverUrl);
        if (songMapper.updateById(song) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 更新歌曲音频
     * @Author: Kay
     * @date:   2025/11/22 15:02
     */
    @Override
    @CacheEvict(cacheNames = "songCache", allEntries = true)
    public Result updateSongAudio(Long songId, String audioUrl, String duration) {
        Song song = songMapper.selectById(songId);
        String audio = song.getAudioUrl();
        if (audio != null && !audio.isEmpty()) {
            minioService.deleteFile(audio);
        }

        song.setAudioUrl(audioUrl).setDuration(duration);
        if (songMapper.updateById(song) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 删除歌曲
     * @Author: Kay
     * @date:   2025/11/22 15:04
     */
    @Override
    @CacheEvict(cacheNames = "songCache", allEntries = true)
    public Result deleteSong(Long songId) {
        Song song = songMapper.selectById(songId);
        if (song == null) {
            return Result.error(MessageConstant.SONG + MessageConstant.NOT_FOUND);
        }
        String cover = song.getCoverUrl();
        String audio = song.getAudioUrl();

        if (cover != null && !cover.isEmpty()) {
            minioService.deleteFile(cover);
        }
        if (audio != null && !audio.isEmpty()) {
            minioService.deleteFile(audio);
        }

        if (songMapper.deleteById(songId) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 批量删除歌曲
     * @Author: Kay
     * @date:   2025/11/22 15:05
     */
    @Override
    @CacheEvict(cacheNames = "songCache", allEntries = true)
    public Result deleteSongs(List<Long> songIds) {
        // 1. 查询歌曲信息，获取歌曲封面 URL 列表
        List<Song> songs = songMapper.selectByIds(songIds);
        List<String> coverUrlList = songs.stream()
                .map(Song::getCoverUrl)
                .filter(coverUrl -> coverUrl != null && !coverUrl.isEmpty())
                .toList();
        List<String> audioUrlList = songs.stream()
                .map(Song::getAudioUrl)
                .filter(audioUrl -> audioUrl != null && !audioUrl.isEmpty())
                .toList();

        // 2. 先删除 MinIO 里的歌曲封面和音频文件
        for (String coverUrl : coverUrlList) {
            minioService.deleteFile(coverUrl);
        }
        for (String audioUrl : audioUrlList) {
            minioService.deleteFile(audioUrl);
        }

        // 3. 删除数据库中的歌曲信息
        if (songMapper.deleteByIds(songIds) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    @Override
    @CacheEvict(cacheNames = {"songCache", "artistCache"}, allEntries = true)
    public Result addSongByFile(MultipartFile audio) {
        if (audio.isEmpty()) {
            return Result.error("上传的文件为空");
        }
        addSongByFileFunction(audio);
        return Result.success("文件处理成功，信息已输出");
    }

    @Override
    @CacheEvict(cacheNames = {"songCache", "artistCache"}, allEntries = true)
    public Result batchAddSongByFile(MultipartFile[] audios) {
        for (MultipartFile audio : audios) {
            addSongByFileFunction(audio);
        }
        // 返回操作成功的结果
        return Result.success("文件处理成功，信息已输出");
    }

    private void addSongByFileFunction(MultipartFile audio){
        try {

            String audioUrl = minioService.uploadFile(audio, "songs");

            // 获取上传文件的原始文件名
            String originalFilename = audio.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return;
                // return Result.error("文件名为空");
            }

            // 获取文件的扩展名，确保是支持的音频格式（比如 mp3, flac, wav）
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            if (!extension.equalsIgnoreCase(".mp3") && !extension.equalsIgnoreCase(".flac") && !extension.equalsIgnoreCase(".wav")) {
                // return Result.error("不支持的文件格式");
                return;
            }

            // 1. 创建临时文件并添加正确的扩展名
            File tempFile = File.createTempFile("temp_audio", extension);

            // 2. 将上传的文件内容复制到临时文件
            audio.transferTo(tempFile);

            // 3. 使用 jaudiotagger 读取音频文件标签
            AudioFile audioFile = AudioFileIO.read(tempFile);
            org.jaudiotagger.tag.Tag tag = audioFile.getTag();

            // 4. 读取 ID3 标签（标题、艺术家、专辑等）
            if (tag != null) {
                String title = tag.getFirst(FieldKey.TITLE);
                String artistName = tag.getFirst(FieldKey.ARTIST);
                String album = tag.getFirst(FieldKey.ALBUM);
                String trackStr = tag.getFirst(FieldKey.TRACK);
                String yearStr = tag.getFirst(FieldKey.YEAR);

                // 如果 title 为空，则使用文件名去除扩展名作为标题
                if (title == null || title.isEmpty()) {
                    title = originalFilename.substring(0, originalFilename.lastIndexOf("."));
                }

                // 5. 使用 log 输出标签信息
                log.info("Title: {}", title != null ? title : "N/A");
                log.info("Artist: {}", artistName != null ? artistName : "N/A");
                log.info("Album: {}", album != null ? album : "N/A");
                log.info("Track: {}", trackStr != null ? trackStr : "N/A");
                log.info("Year: {}", yearStr != null ? yearStr : "N/A");

                // 6. 获取歌曲时长（秒数）
                long durationMillis = audioFile.getAudioHeader().getTrackLength() * 1000L;
                long minutes = durationMillis / 60000;
                long seconds = (durationMillis % 60000) / 1000;
                log.info("Duration: {} minutes {} seconds", minutes, seconds);


                // 1. 查找有无歌手信息 ， 获取 歌手 id
                Artist artist = artistMapper.selectOne(new LambdaQueryWrapper<Artist>().eq(Artist::getArtistName, artistName));
                // 1.1 有则 获取 id
                Long artistId;
                if ( artist != null ) {
                    artistId = artist.getArtistId();
                } else {
                    // 1.2 无则 手动插入 并获取 id
                    Artist newArtist = new Artist().setArtistName(artistName);
                    artistMapper.insert(newArtist);
                    artistId = newArtist.getArtistId();
                }

                // 2. 组装已有信息
                Song song = new Song()
                        .setSongName(title)
                        .setArtistId(artistId)
                        .setAlbum(album)
                        .setAudioUrl(audioUrl)
                        .setReleaseTime(parseYearToLocalDate(yearStr)) // 将年份转换为 LocalDate
                        .setDuration(formatDuration(durationMillis)); // 将时长格式化为字符串

                // 3. 插入信息
                songMapper.insert(song);



            } else {
                log.info("No ID3 tags found in the file.");
            }

            // 删除临时文件
            tempFile.delete();


        } catch (IOException | org.jaudiotagger.audio.exceptions.CannotReadException e) {
            e.printStackTrace();
        } catch (TagException | InvalidAudioFrameException | ReadOnlyFileException e) {
            throw new RuntimeException(e);
        }
    }

    // 将年份字符串转换为 LocalDate，若无效则返回当前日期
    private LocalDate parseYearToLocalDate(String yearStr) {
        try {
            if (yearStr != null && !yearStr.isEmpty()) {
                return LocalDate.parse(yearStr + "-01-01", DateTimeFormatter.ISO_DATE); // 使用默认的01-01作为日期
            }
        } catch (DateTimeParseException e) {
            // 如果解析失败，返回当前日期（可以根据需求修改为其他默认值）
            return LocalDate.now();
        }
        return LocalDate.now(); // 如果 yearStr 为 null 或空，则返回当前日期
    }

    // 将时长（毫秒）转换为字符串格式，保留两位小数
    private String formatDuration(long durationMillis) {
        double durationSeconds = durationMillis / 1000.0; // 转换为秒
        return String.format("%.2f", durationSeconds); // 格式化为字符串
    }

}

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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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
    private final RedisTemplate redisTemplate;
    private final StyleMapper styleMapper;
    private final GenreMapper genreMapper;
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
    // TODO：当前的 sortedStyleIds 只是根据风格出现频率排序，
    //       但实际 SQL 查询只使用了 IN (...)，不会根据排序影响候选歌曲权重。
    //       后续需要将“风格权重”真正应用到推荐逻辑中，例如：
    //       1) 按权重进行加权随机推荐，让风格出现概率与用户偏好一致。
    //       2) 或在 SQL 中使用 CASE/FIELD 手动排序，让高权重风格优先返回。
    //       3) 或分批按风格查询，按比例分配歌曲数量。
    //       当前逻辑仍然能跑，但没有真正体现个性化权重，需要之后优化。

    @Override
    public Result<List<SongVO>> getRecommendedSongs() {

        Long userId = ThreadLocalUtil.getUserId();
        // 1. 用户未登录，返回随机歌曲列表
        if ( userId == null ) {
            return Result.success(songMapper.getRandomSongsWithArtist());
        }
        // 2.1 查询用户收藏的歌曲 ID
        List<Long> favoriteSongIds = userFavoriteMapper.getFavoriteSongIdsByUserId(userId);
        // 2.2 如果查不到，则随机返回
        if (favoriteSongIds.isEmpty()) {
            return Result.success(songMapper.getRandomSongsWithArtist());
        }

        // 3. 根据用户收藏歌曲，查询用户收藏的歌曲风格，并统计频率
        List<Long> favoriteStyleIds = songMapper.getFavoriteSongStyles(favoriteSongIds);
        Map<Long, Long> styleFrequency = favoriteStyleIds.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // 4. 按风格出现次数降序排序
        List<Long> sortedStyleIds = styleFrequency.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 5. 从 Redis 获取缓存的推荐列表 ， range 0 , -1 :  “从头到尾所有元素”
        String redisKey = "recommended_songs:" + userId;
        List<SongVO> cachedSongs = redisTemplate.opsForList().range(redisKey, 0, -1);

        // 如果 Redis 没有缓存，则查询数据库并缓存
        if (cachedSongs == null || cachedSongs.isEmpty()) {
            // 根据排序后的风格推荐歌曲（排除已收藏歌曲）
            cachedSongs = songMapper.getRecommendedSongsByStyles(sortedStyleIds, favoriteSongIds, 80);
            redisTemplate.opsForList().rightPushAll(redisKey, cachedSongs);
            redisTemplate.expire(redisKey, 30, TimeUnit.MINUTES); // 设置过期时间 30 分钟
        }

        // 随机选取 20 首
        Collections.shuffle(cachedSongs);
        List<SongVO> recommendedSongs = cachedSongs.subList(0, Math.min(20, cachedSongs.size()));

        // 如果推荐的歌曲不足 20 首，则用随机歌曲填充
        if (recommendedSongs.size() < 20) {
            List<SongVO> randomSongs = songMapper.getRandomSongsWithArtist();
            Set<Long> addedSongIds = recommendedSongs.stream().map(SongVO::getSongId).collect(Collectors.toSet());
            for (SongVO song : randomSongs) {
                if (recommendedSongs.size() >= 20) break;
                if (!addedSongIds.contains(song.getSongId())) {
                    recommendedSongs.add(song);
                }
            }
        }

        return Result.success(recommendedSongs);
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
    public Result<Long> getAllSongsCount(String style) {
        LambdaQueryWrapper<Song> queryWrapper = new LambdaQueryWrapper<>();
        if (style != null) {
            queryWrapper.like(Song::getStyle, style);
        }

        return Result.success(songMapper.selectCount(queryWrapper));
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
        Long songId = songInDB.getSongId();
        // 解析风格字段（多个风格以逗号分隔）
        String styleStr = songAddDTO.getStyle();
        if (styleStr != null && !styleStr.isEmpty()) {
            List<String> styles = Arrays.asList(styleStr.split(","));

            // 查询风格 ID
            List<Style> styleList = styleMapper.selectList(new QueryWrapper<Style>().in("name", styles));

            // 插入到 tb_genre
            for (Style style : styleList) {
                Genre genre = new Genre();
                genre.setSongId(songId);
                genre.setStyleId(style.getStyleId());
                genreMapper.insert(genre);
            }
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

        Long songId = songUpdateDTO.getSongId();

        // 删除 tb_genre 中该歌曲的原有风格映射
        genreMapper.delete(new QueryWrapper<Genre>().eq("song_id", songId));

        // 解析新的风格字段（多个风格以逗号分隔）
        String styleStr = songUpdateDTO.getStyle();
        if (styleStr != null && !styleStr.isEmpty()) {
            List<String> styles = Arrays.asList(styleStr.split(","));

            // 查询风格 ID
            List<Style> styleList = styleMapper.selectList(new QueryWrapper<Style>().in("name", styles));

            // 插入新的风格映射到 tb_genre
            for (Style style : styleList) {
                Genre genre = new Genre();
                genre.setSongId(songId);
                genre.setStyleId(style.getStyleId());
                genreMapper.insert(genre);
            }
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
                String genre = tag.getFirst(FieldKey.GENRE);
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
                log.info("Genre: {}", genre != null ? genre : "N/A");
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

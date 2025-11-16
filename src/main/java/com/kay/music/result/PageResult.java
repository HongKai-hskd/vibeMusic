package com.kay.music.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: Kay
 * @date:   2025/11/16 12:44
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private Long total; //总条数
    private List<T> items; //当前页数据集合

}

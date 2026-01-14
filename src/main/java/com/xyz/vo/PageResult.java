package com.xyz.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应（支持传统分页和游标分页）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    
    /** 数据列表 */
    private List<T> list;
    
    /** 下一页游标（本页最后一条的时间戳），前端下次请求带上这个值（游标分页使用） */
    private Long nextCursor;
    
    /** 是否还有更多数据（游标分页使用） */
    private Boolean hasMore;
    
    /** 总记录数（传统分页使用） */
    private Long total;
    
    /** 数据列表（传统分页使用） */
    private List<T> records;
    
    /**
     * 构建分页结果
     */
    //hasMore = true：表示后端还有更多数据，前端可以继续请求下一页
    //hasMore = false：表示数据已经全部加载完毕，前端不需要再发起请求
    public static <T> PageResult<T> of(List<T> list, Long nextCursor, boolean hasMore) {
        return PageResult.<T>builder()
                .list(list)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }
    
    /**
     * 空结果
     */
    public static <T> PageResult<T> empty() {
        return PageResult.<T>builder()
                .list(List.of())
                .nextCursor(null)
                .hasMore(false)
                .build();
    }
}

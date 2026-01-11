package com.xyz.controller;

import com.xyz.dto.CommentDTO;
import com.xyz.service.CommentService;
import com.xyz.vo.CommentVO;
import com.xyz.vo.PageResult;
import com.xyz.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 */
@Slf4j
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Tag(name = "评论接口")
public class CommentController {

    private final CommentService commentService;

    /**
     * 发表评论
     */
    @PostMapping
    @Operation(summary = "发表评论")
    public Result<CommentVO> addComment(@Valid @RequestBody CommentDTO commentDTO) {
        log.info("发表评论: {}", commentDTO);
        CommentVO commentVO = commentService.addComment(commentDTO);
        return Result.success(commentVO);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{commentId}")
    @Operation(summary = "删除评论")
    public Result<String> deleteComment(@PathVariable Long commentId) {
        log.info("删除评论: {}", commentId);
        commentService.deleteComment(commentId);
        return Result.success("删除成功");
    }

    /**
     * 点赞/取消点赞评论
     */
    @PostMapping("/{commentId}/like")
    @Operation(summary = "点赞或取消点赞评论")
    public Result<String> likeComment(@PathVariable Long commentId) {
        log.info("点赞评论: {}", commentId);
        commentService.likeComment(commentId);
        return Result.success("操作成功");
    }

    /**
     * 获取商品的顶层评论列表（游标分页）
     */
    @GetMapping("/goods/{goodsId}")
    @Operation(summary = "获取商品的顶层评论列表")
    public Result<PageResult<CommentVO>> getTopComments(
            @PathVariable Long goodsId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("获取商品评论列表: goodsId={}, cursor={}, size={}", goodsId, cursor, size);
        PageResult<CommentVO> pageResult = commentService.getTopComments(goodsId, cursor, size);
        return Result.success(pageResult);
    }

    /**
     * 获取某条评论的所有回复（游标分页）
     */
    @GetMapping("/{parentId}/replies")
    @Operation(summary = "获取评论的回复列表")
    public Result<PageResult<CommentVO>> getReplies(
            @PathVariable Long parentId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        log.info("获取评论回复列表: parentId={}, cursor={}, size={}", parentId, cursor, size);
        PageResult<CommentVO> pageResult = commentService.getReplies(parentId, cursor, size);
        return Result.success(pageResult);
    }
}

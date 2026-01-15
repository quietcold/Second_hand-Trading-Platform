package com.xyz.controller.admin;

import com.xyz.service.CommentService;
import com.xyz.vo.CommentVO;
import com.xyz.vo.PageResult;
import com.xyz.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员-评论管理控制器
 */
@Slf4j
@RestController("adminCommentController")
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Tag(name = "评论管理")
public class CommentController {

    private final CommentService commentService;

    /**
     * 获取商品的顶层评论列表（游标分页）- 与用户端一致
     */
    @GetMapping("/goods/{goodsId}")
    @Operation(summary = "获取商品的顶层评论列表")
    public Result<PageResult<CommentVO>> getTopComments(
            @Parameter(description = "商品ID") @PathVariable Long goodsId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("管理员获取商品评论列表: goodsId={}, cursor={}, size={}", goodsId, cursor, size);
        PageResult<CommentVO> pageResult = commentService.getTopComments(goodsId, cursor, size);
        return Result.success(pageResult);
    }

    /**
     * 获取某条评论的所有回复（游标分页）- 与用户端一致
     */
    @GetMapping("/{parentId}/replies")
    @Operation(summary = "获取评论的回复列表")
    public Result<PageResult<CommentVO>> getReplies(
            @Parameter(description = "父评论ID") @PathVariable Long parentId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        log.info("管理员获取评论回复列表: parentId={}, cursor={}, size={}", parentId, cursor, size);
        PageResult<CommentVO> pageResult = commentService.getReplies(parentId, cursor, size);
        return Result.success(pageResult);
    }

    /**
     * 管理员删除评论（违规屏蔽）
     */
    @DeleteMapping("/{commentId}")
    @Operation(summary = "删除评论（违规屏蔽）")
    public Result<String> deleteCommentByAdmin(
            @Parameter(description = "评论ID") @PathVariable Long commentId,
            @Parameter(description = "删除原因（可选）") @RequestParam(required = false) String reason) {
        log.info("管理员删除评论: commentId={}, reason={}", commentId, reason);
        commentService.deleteCommentByAdmin(commentId, reason);
        return Result.success("评论已删除");
    }

    /**
     * 管理员恢复评论
     */
    @PutMapping("/{commentId}/restore")
    @Operation(summary = "恢复评论")
    public Result<String> restoreComment(
            @Parameter(description = "评论ID") @PathVariable Long commentId) {
        log.info("管理员恢复评论: commentId={}", commentId);
        commentService.restoreCommentByAdmin(commentId);
        return Result.success("评论已恢复");
    }
}

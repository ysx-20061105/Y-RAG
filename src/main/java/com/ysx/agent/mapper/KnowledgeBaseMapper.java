package com.ysx.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ysx.agent.domain.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {

    Page<KnowledgeBase> selectVisibleByOwnerAndFilters(@Param("ownerUserId") Long ownerUserId,
                                                       @Param("keyword") String keyword,
                                                       @Param("category") String category,
                                                       @Param("tag") String tag,
                                                       Page<KnowledgeBase> page);

    KnowledgeBase selectVisibleByIdAndOwner(@Param("id") Long id, @Param("ownerUserId") Long ownerUserId);

    int softDeleteByIdAndOwner(@Param("id") Long id,
                               @Param("ownerUserId") Long ownerUserId,
                               @Param("updatedAt") Date updatedAt);

    int updateByIdAndOwnerWithUpdatedAt(@Param("id") Long id,
                                        @Param("ownerUserId") Long ownerUserId,
                                        @Param("name") String name,
                                        @Param("description") String description,
                                        @Param("category") String category,
                                        @Param("tags") String tags,
                                        @Param("updatedAt") Date updatedAt,
                                        @Param("lastKnownUpdatedAt") java.time.LocalDateTime lastKnownUpdatedAt);
}

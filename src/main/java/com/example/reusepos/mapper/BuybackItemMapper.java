package com.example.reusepos.mapper;

import com.example.reusepos.model.BuybackItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BuybackItemMapper {
    List<BuybackItem> findAll();
    BuybackItem findById(@Param("id") Long id);
    List<BuybackItem> findByStatus(@Param("status") String status);
    List<BuybackItem> findByCategoryId(@Param("categoryId") Long categoryId);
    int insert(BuybackItem item);
    int update(BuybackItem item);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}

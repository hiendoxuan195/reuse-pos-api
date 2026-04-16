package com.example.reusepos.service;

import com.example.reusepos.mapper.BuybackItemMapper;
import com.example.reusepos.model.BuybackItem;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuybackItemService {

    private final BuybackItemMapper mapper;

    public BuybackItemService(BuybackItemMapper mapper) {
        this.mapper = mapper;
    }

    public List<BuybackItem> findAll() {
        return mapper.findAll();
    }

    public BuybackItem findById(Long id) {
        return mapper.findById(id);
    }

    public List<BuybackItem> findByStatus(String status) {
        return mapper.findByStatus(status);
    }

    public List<BuybackItem> findByCategoryId(Long categoryId) {
        return mapper.findByCategoryId(categoryId);
    }

    public BuybackItem create(BuybackItem item) {
        if (item.getCondition() == null) item.setCondition("B");
        if (item.getStatus() == null) item.setStatus("IN_STOCK");
        mapper.insert(item);
        return mapper.findById(item.getId());
    }

    public BuybackItem update(Long id, BuybackItem item) {
        item.setId(id);
        mapper.update(item);
        return mapper.findById(id);
    }

    public BuybackItem updateStatus(Long id, String status) {
        mapper.updateStatus(id, status);
        return mapper.findById(id);
    }
}

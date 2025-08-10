package com.needitnow.services;

import java.util.*;
import com.needitnow.entity.Item;
import com.needitnow.repositories.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }
    
    public List<Item> getItemsByCommunityId(Long communityId) {
        return itemRepository.findByCommunityId(communityId);
    }

    public List<Item> getItemsWithRequests(Long userId) {
        return itemRepository.findByUserIdAndRequesterIsNotNull(userId);
    }
    
    public Optional<Item> findById(Long itemId) {
        return itemRepository.findById(itemId);
    }
    
    /**
     * Get all items shared by a specific user in a specific community
     */
    public List<Item> getItemsSharedByUser(Long userId, Long communityId) {
        return itemRepository.findByUserIdAndCommunityId(userId, communityId);
    }
    
    /**
     * Get all items requested by a specific user in a specific community
     */
    public List<Item> getItemsRequestedByUser(Long userId, Long communityId) {
        return itemRepository.findByRequesterIdAndCommunityId(userId, communityId);
    }
    
    /**
     * Delete an item by its ID
     */
    public void deleteItem(Long itemId) {
        itemRepository.deleteById(itemId);
    }
}
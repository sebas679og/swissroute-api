package com.group4.swissrouteapi.services.processors;

import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.models.SearchHistoryEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.SearchHistoryRepository;
import com.group4.swissrouteapi.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SearchHistoryProcessor {

    private final UserRepository userRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    @Transactional
    public void saveSearchHistory(String from, String to, Integer resultCount, UUID userId){
        UserEntity user = searchUser(userId);

        searchHistoryRepository.save(
                SearchHistoryEntity.builder()
                        .user(user)
                        .origin(from)
                        .destination(to)
                        .resultCount(resultCount)
                        .build()
        );
    }

    private UserEntity searchUser(UUID userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}

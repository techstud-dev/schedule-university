package com.techstud.schedule_university.fetcher.service;

import com.techstud.schedule_university.fetcher.dto.GroupData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class GroupFetchServiceFacade {

    private final GroupFetchServiceFactory groupFetchServiceFactory;

    @Async
    public CompletableFuture<List<GroupData>> asyncParseGroups(String universityName) {
        log.info("Received task for fetching groups for university: {}", universityName);
        return CompletableFuture.supplyAsync(() -> {
            try {
                return groupFetchServiceFactory.getGroupFetcherService(universityName).fetchGroupsData();
            } catch (Exception e) {
                log.error("Error while fetching groups", e);
                return Collections.emptyList();
            }
        });
    }

}

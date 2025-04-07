package com.techstud.schedule_university.fetcher.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GroupFetchServiceFactory {

    private final Map<String, GroupFetcherService> groupFetcherServiceMap;

    @PostConstruct
    public void init() {
        log.info("Bean fetchGroupServiceFactory success loaded with fetchers: {}", groupFetcherServiceMap.keySet());
    }

    public GroupFetcherService getGroupFetcherService(String universityName) {
        GroupFetcherService groupFetcherService = groupFetcherServiceMap.get(universityName + "_GROUP_FETCHER");
        if (groupFetcherService == null) {
            throw new IllegalArgumentException("GroupFetcherService for university " + universityName + " not found");
        }
        return groupFetcherService;
    }
}

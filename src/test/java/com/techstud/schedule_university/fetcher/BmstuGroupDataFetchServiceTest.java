package com.techstud.schedule_university.fetcher;

import com.google.gson.Gson;
import com.techstud.schedule_university.fetcher.dto.GroupData;
import com.techstud.schedule_university.fetcher.service.GroupFetcherService;
import com.techstud.schedule_university.fetcher.service.impl.BmstuGroupDataFetchService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@Slf4j
@ActiveProfiles("dev")
@Disabled
public class BmstuGroupDataFetchServiceTest {

    private GroupFetcherService bmstuGroupDataFetchService;

    @BeforeEach
    public void setUp() {
        bmstuGroupDataFetchService = new BmstuGroupDataFetchService();
    }

    @Test
    public void testFetchGroupData() {
        List<GroupData> groupDataList = bmstuGroupDataFetchService.fetchGroupsData();
        String resultJson = new Gson().toJson(groupDataList);
        log.info("Group data list: {}", resultJson);
        Assertions.assertNotNull(groupDataList);
    }
}

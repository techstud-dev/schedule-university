package com.techstud.schedule_university.fetcher;

import com.google.gson.Gson;
import com.techstud.schedule_university.fetcher.dto.GroupData;
import com.techstud.schedule_university.fetcher.service.GroupFetcherService;
import com.techstud.schedule_university.fetcher.service.impl.MephiGroupDataFetchService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles("dev")
@Slf4j
@Disabled
public class MephiGroupDataFetchServiceTest {

    private GroupFetcherService mephiGroupDataFetchService;

    @BeforeEach
    public void setUp() {
        mephiGroupDataFetchService = new MephiGroupDataFetchService();
    }

    @Test
    public void testFetchGroupData() {
        List<GroupData> groupDataList = mephiGroupDataFetchService.fetchGroupsData();
        String resultJson = new Gson().toJson(groupDataList);
        System.out.println(resultJson);
        log.info("Group data list: {}", resultJson);
        Assertions.assertNotNull(groupDataList);
    }
}

package com.techstud.schedule_university.fetcher;

import com.techstud.schedule_university.fetcher.dto.GroupData;
import com.techstud.schedule_university.fetcher.service.GroupFetchServiceFacade;
import com.techstud.schedule_university.fetcher.service.GroupFetchServiceFactory;
import com.techstud.schedule_university.fetcher.service.GroupFetcherService;
import com.techstud.schedule_university.fetcher.service.impl.BmstuGroupDataFetchService;
import com.techstud.schedule_university.fetcher.service.impl.SsauGroupDataFetchService;
import com.techstud.schedule_university.fetcher.service.impl.SseuGroupDataFetchService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Description;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

@ActiveProfiles("dev")
@Disabled
public class GroupFetServiceFacadeTest {

    private GroupFetchServiceFacade groupFetchServiceFacade;

    @BeforeEach
    public void setUp() {
        Map<String, GroupFetcherService> groupFetcherServiceMap = Map.of(
                "BMSTU_GROUP_FETCHER", new BmstuGroupDataFetchService(),
                "SSAU_GROUP_FETCHER", new SsauGroupDataFetchService(),
                "SSEU_GROUP_FETCHER", new SseuGroupDataFetchService()
        );
        GroupFetchServiceFactory groupFetchServiceFactory = new GroupFetchServiceFactory(groupFetcherServiceMap);
        groupFetchServiceFacade = new GroupFetchServiceFacade(groupFetchServiceFactory);
    }

    @Test
    @Description("Тест на асинхронное получение групп")
    @SneakyThrows
    public void asyncParseGroups() {
        String universityName = "SSAU";

        Future<List<GroupData>> futureGroupData = groupFetchServiceFacade.asyncParseGroups(universityName);
        List<GroupData> result = futureGroupData.get();

        Assertions.assertNotNull(result);
    }
}

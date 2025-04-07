package com.techstud.schedule_university.fetcher.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.techstud.schedule_university.fetcher.dto.GroupData;
import com.techstud.schedule_university.fetcher.dto.api.response.SseuApiGroupDataResponse;
import com.techstud.schedule_university.fetcher.service.GroupFetcherService;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.techstud.schedule_university.fetcher.util.FetcherHttpUtils.createResponseHandler;

@Service("SSEU_GROUP_FETCHER")
@Slf4j
public class SseuGroupDataFetchService implements GroupFetcherService {

    @Override
    public List<GroupData> fetchGroupsData() {
        List<GroupData> groupDataList = new ArrayList<>();
        String baseUrl = "https://lms3.sseu.ru/api/v1/schedule-board/groups";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(baseUrl);
            httpGet.setHeader("Accept", "application/json");

            HttpClientResponseHandler<List<SseuApiGroupDataResponse>> responseHandler =
                    createResponseHandler(new TypeReference<>() {
                    });

            List<SseuApiGroupDataResponse> apiResponses = httpClient.execute(httpGet, responseHandler);

            groupDataList = mapApiResponseToGroupList(apiResponses);
        } catch (Exception e) {
            log.error("Error fetching group data from SSEU", e);
        }
        return groupDataList;
    }

    private List<GroupData> mapApiResponseToGroupList(List<SseuApiGroupDataResponse> sseuApiGroupDataResponseList) {
        List<GroupData> groupDataList = new ArrayList<>();

        sseuApiGroupDataResponseList.forEach(sseuApiGroupDataResponse -> {
            GroupData groupData = new GroupData(sseuApiGroupDataResponse.name(), sseuApiGroupDataResponse.id().toString());
            groupDataList.add(groupData);
        });

        return groupDataList;
    }
}

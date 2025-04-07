package com.techstud.schedule_university.fetcher.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstud.schedule_university.fetcher.dto.GroupData;
import com.techstud.schedule_university.fetcher.dto.api.response.TltsuApiGroupDataResponse;
import com.techstud.schedule_university.fetcher.service.GroupFetcherService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.StreamSupport;

import static com.techstud.schedule_university.fetcher.util.FetcherHttpUtils.unchecked;
import static org.apache.hc.client5.http.impl.classic.HttpClients.createDefault;

@Service("TLTSU_GROUP_FETCHER")
@Slf4j
public class TltsuGroupDataFetchService implements GroupFetcherService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @SneakyThrows
    public List<GroupData> fetchGroupsData() {
        String institutesUrl = "https://its.tltsu.ru/api/institutes";
        List<GroupData> groupDataList;
        JsonNode jsonNode = objectMapper.readTree(returnHttpResponse(institutesUrl).toString());
        List<TltsuApiGroupDataResponse> courseInfo = StreamSupport.stream(jsonNode.spliterator(), false)
                .map(node -> node.get("id").asInt())
                .map(unchecked(this::returnCourseInfo))
                .flatMap(Collection::stream)
                .toList();
        groupDataList = courseInfo.parallelStream()
                .map(unchecked(group ->
                        returnGroupInfo(
                                group.courseName(),
                                group.universityId()
                        )
                ))
                .flatMap(Collection::stream)
                .toList();
        return groupDataList;
    }

    private List<GroupData> returnGroupInfo(String courseName, int groupId) throws JsonProcessingException {
        List<GroupData> groupDataList = new ArrayList<>();
        String link = "https://its.tltsu.ru/api/groups/course/"
                + URLEncoder.encode(courseName, StandardCharsets.UTF_8)
                + "/institute/"
                + URLEncoder.encode(String.valueOf(groupId), StandardCharsets.UTF_8);
        JsonNode jsonNode = objectMapper.readTree(returnHttpResponse(link).toString());
        jsonNode.forEach(node -> {
            GroupData groupData = new GroupData(
                    node.get("name").asText(),
                    node.get("id").asText()
            );
            groupDataList.add(groupData);
        });
        return groupDataList;
    }

    private List<TltsuApiGroupDataResponse> returnCourseInfo(int idUnversity) throws JsonProcessingException {
        List<TltsuApiGroupDataResponse> courseInfo = new ArrayList<>();
        String link = "https://its.tltsu.ru/api/courses/institute/" + idUnversity;
        JsonNode jsonNode = objectMapper.readTree(returnHttpResponse(link).toString());
        List<String> array = StreamSupport
                .stream(jsonNode.spliterator(), false)
                .map(JsonNode::asText)
                .toList();
        array.forEach(info ->
                courseInfo.add(new TltsuApiGroupDataResponse(idUnversity, info))
        );
        return courseInfo;
    }

    private StringBuilder returnHttpResponse(String link) {
        StringBuilder responseBody = new StringBuilder();
        try (CloseableHttpClient httpClient = createDefault()) {
            HttpGet httpGet = new HttpGet(link);
            HttpClientResponseHandler<String> responseHandler = response -> {
                if (response.getCode() != 200) {
                    log.error("Error fetching group data from TLTSU{}", response.getCode());
                    throw new IOException("Unexpected response code " + response.getCode());
                }
                log.info("Successful connection to API TLTSU");
                return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            };
            return responseBody.append(httpClient.execute(httpGet, responseHandler));
        } catch (Exception e) {
            log.error("Error fetching group data from TLTSU", e);
            throw new RuntimeException(e);
        }
    }
}

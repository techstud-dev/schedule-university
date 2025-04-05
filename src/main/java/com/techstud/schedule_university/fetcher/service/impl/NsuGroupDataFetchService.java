package com.techstud.schedule_university.fetcher.service.impl;

import com.techstud.schedule_university.fetcher.dto.GroupData;
import com.techstud.schedule_university.fetcher.service.GroupFetcherService;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.techstud.schedule_university.fetcher.util.FetcherHttpUtils.createResponseHandler;

@Service("NSU_GROUP_FETCHER")
@Slf4j
public class NsuGroupDataFetchService implements GroupFetcherService {

    @Override
    public List<GroupData> fetchGroupsData() {
        List<GroupData> groupDataList = new ArrayList<>();
        Set<String> seenGroupIds = new HashSet<>();
        String baseUrl = "https://table.nsu.ru/faculties";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(baseUrl);

            String responseBody = httpClient.execute(httpGet, createResponseHandler(String.class, true));
            Document document = Jsoup.parse(responseBody);

            Elements faculties = document.select("a.faculty");
            for (Element faculty : faculties) {
                String facultyLink = faculty.attr("href");
                if (!facultyLink.isEmpty()) {
                    parseFacultyGroups(httpClient, facultyLink, groupDataList, seenGroupIds);
                }
            }

        } catch (Exception e) {
            log.error("Error processing pattern {}", e.getMessage());
        }

        return groupDataList.stream()
                .sorted(Comparator.comparing(GroupData::universityGroupId))
                .collect(Collectors.toList());
    }

    private void parseFacultyGroups(CloseableHttpClient httpClient, String facultyLink,
                                    List<GroupData> groupDataList, Set<String> seenGroupIds) {
        String facultyUrl = "https://table.nsu.ru" + facultyLink;

        try {
            HttpGet httpGet = new HttpGet(facultyUrl);

            String responseBody = httpClient.execute(httpGet, createResponseHandler(String.class, true));
            Document document = Jsoup.parse(responseBody);

            Elements groups = document.select("a.group");
            for (Element group : groups) {
                String groupCode = group.text().trim();
                String groupHref = group.attr("href").trim();

                if (!groupCode.isEmpty() && groupHref.startsWith("/group/")) {
                    String universityGroupId = groupHref.replace("/group/", "");

                    if (seenGroupIds.add(universityGroupId)) {
                        groupDataList.add(new GroupData(groupCode, universityGroupId));
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error fetching group data from NSU", e);
        }

    }
}

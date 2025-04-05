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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.techstud.schedule_university.fetcher.util.FetcherHttpUtils.createResponseHandler;

@Service("MIIT_GROUP_FETCHER")
@Slf4j
public class MiitGroupDataFetchService implements GroupFetcherService {

    @Override
    public List<GroupData> fetchGroupsData() {
        List<GroupData> groupDataList = new ArrayList<>();
        String baseUrl = "https://www.miit.ru/timetable";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpGet httpGet = new HttpGet(baseUrl);

            String responseBody = httpClient.execute(httpGet, createResponseHandler(String.class, true));
            Document document = Jsoup.parse(responseBody);

            Elements groupElements = document.select("div.dropdown-menu .dropdown-item");
            for (Element groupElement : groupElements) {
                String href = groupElement.attr("href").trim();
                String groupCode = groupElement.text().trim();

                if (href.isEmpty() || href.equals("/") || href.contains(".pdf") || groupCode.isEmpty()) {
                    log.warn("href or group code is empty: {}, {}", href, groupCode);
                    continue;
                }

                String universityGroupId = href.startsWith("/timetable/") ? href.replace("/timetable/", "") : href;

                if (!isNumeric(universityGroupId)) {
                    log.warn("skipping non-numeric university group id: {}", universityGroupId);
                    continue;
                }

                groupDataList.add(new GroupData(groupCode, universityGroupId));
            }

        } catch (Exception e) {
            log.error("Error fetching group data from MIIT", e);
        }

        return groupDataList.stream()
                .sorted(Comparator.comparing(GroupData::universityGroupId))
                .collect(Collectors.toList());
    }

    private boolean isNumeric(String str) {
        return str.matches("\\d+");
    }
}

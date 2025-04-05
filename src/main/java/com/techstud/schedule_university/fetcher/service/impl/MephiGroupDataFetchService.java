package com.techstud.schedule_university.fetcher.service.impl;

import com.techstud.schedule_university.fetcher.dto.GroupData;
import com.techstud.schedule_university.fetcher.service.GroupFetcherService;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
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

@Service("MEPHI_GROUP_FETCHER")
@Slf4j
public class MephiGroupDataFetchService implements GroupFetcherService {

    @Override
    public List<GroupData> fetchGroupsData() {
        List<GroupData> groupDataList = new ArrayList<>();
        String baseUrl = "https://home.mephi.ru";
        int[] levels = {0, 1, 2, 3, 4}; // Бакалавр, специалитет, магистратура и т.д.

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            for (int level : levels) {
                String levelUrl = baseUrl + "/study_groups?level=" + level + "&organization_id=1&term_id=19";

                HttpGet httpGet = getHttpGet(levelUrl);

                String responseBody = httpClient.execute(httpGet, createResponseHandler(String.class, true));

                Document document = Jsoup.parse(responseBody);

                Elements groups = document.select("a.list-group-item");

                for (Element groupElement : groups) {
                    String groupCode = groupElement.text();

                    String href = groupElement.attr("href");
                    String universityGroupId = href.split("/")[2];

                    groupDataList.add(new GroupData(groupCode, universityGroupId));
                }

                if (groups.isEmpty()) {
                    log.warn("No groups found for level {}", level);
                }

            }

        } catch (Exception e) {
            log.error("Error fetching group data from MEPHI", e);
        }

        return groupDataList.stream()
                .sorted(Comparator.comparing(GroupData::universityGroupId))
                .collect(Collectors.toList());
    }

    private static HttpGet getHttpGet(String url) {
        HttpGet httpGet = new HttpGet(url);

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Accept", "application/json, text/javascript, */*; q=0.01"));
        headers.add(new BasicHeader("Accept-Encoding", "gzip, deflate, br, zstd"));
        headers.add(new BasicHeader("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7"));
        headers.add(new BasicHeader("Connection", "keep-alive"));
        headers.add(new BasicHeader("Content-Type", "application/json; charset=UTF-8"));
        headers.add(new BasicHeader("Host", "home.mephi.ru"));
        headers.add(new BasicHeader("Origin", "https://home.mephi.ru"));
        headers.add(new BasicHeader("Sec-CH-UA", "\"Google Chrome\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\""));
        headers.add(new BasicHeader("Sec-CH-UA-Mobile", "?0"));
        headers.add(new BasicHeader("Sec-CH-UA-Platform", "\"Windows\""));
        headers.add(new BasicHeader("Sec-Fetch-Dest", "empty"));
        headers.add(new BasicHeader("Sec-Fetch-Mode", "cors"));
        headers.add(new BasicHeader("Sec-Fetch-Site", "same-origin"));
        headers.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"));
        headers.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));

        httpGet.setHeaders(headers.toArray(new Header[0]));
        return httpGet;
    }
}

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

@Service("PGUPS_GROUP_FETCHER")
@Slf4j
public class PgupsGroupDataFetchService implements GroupFetcherService {

    @Override
    public List<GroupData> fetchGroupsData() {
        List<GroupData> groupDataList = new ArrayList<>();
        String baseUrl = "https://rasp.pgups.ru/schedule/group";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(baseUrl);

            String responseBody = httpClient.execute(httpGet, createResponseHandler(String.class, true));
            Document document = Jsoup.parse(responseBody);

            Elements elements = document.select("a.btn.btn-sm.btn");
            for (Element element : elements) {
                String groupId = element.text();
                String href = element.attr("href");

                String universityGroupId = href.replace("https://rasp.pgups.ru/schedule/group/", "");

                groupDataList.add(new GroupData(groupId, universityGroupId));
            }

            groupDataList = groupDataList.stream()
                    .filter(this::isValidGroup)
                    .sorted(Comparator.comparing(GroupData::universityGroupId))
                    .collect(Collectors.toList());


        } catch (Exception e) {
            log.error("Error processing data from PGUPS {}", e.getMessage());
        }

        return groupDataList.stream()
                .filter(this::isValidGroup)
                .sorted(Comparator.comparing(GroupData::universityGroupId))
                .collect(Collectors.toList());
    }

    private boolean isValidGroup(GroupData group) {
        if (!group.universityGroupId().matches("\\d+")) {
            log.warn("Filtered out non-numeric group: {}", group);
            return false;
        }

        return true;
    }
}

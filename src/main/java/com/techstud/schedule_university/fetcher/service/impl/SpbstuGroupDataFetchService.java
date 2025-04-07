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

@Service("SPBSTU_GROUP_FETCHER")
@Slf4j
public class SpbstuGroupDataFetchService implements GroupFetcherService {

    @Override
    public List<GroupData> fetchGroupsData() {
        List<GroupData> groupDataList = new ArrayList<>();
        String baseUrl = "https://ruz.spbstu.ru";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(baseUrl);

            String responseBody = httpClient.execute(httpGet, createResponseHandler(String.class, true));
            Document document = Jsoup.parse(responseBody);

            Elements faculties = document.select("li.faculty-list__item > a.faculty-list__link");
            for (Element faculty : faculties) {
                String facultyLink = faculty.attr("href");
                parseFacultyGroups(httpClient, facultyLink, groupDataList);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return groupDataList.stream()
                .sorted(Comparator.comparing(GroupData::universityGroupId))
                .collect(Collectors.toList());
    }

    private void parseFacultyGroups(CloseableHttpClient httpClient, String facultyLink, List<GroupData> groupDataList) {
        String facultyUrl = "https://ruz.spbstu.ru" + facultyLink;

        try {
            HttpGet httpGet = new HttpGet(facultyUrl);

            String responseBody = httpClient.execute(httpGet, createResponseHandler(String.class, true));
            Document document = Jsoup.parse(responseBody);

            Elements elements = document.select("li.groups-list__item > a.groups-list__link");
            for (Element element : elements) {
                String href = element.attr("href");
                String groupId = element.text();
                String universityGroupId = href.replace(facultyLink + "/", "");

                groupDataList.add(new GroupData(groupId, universityGroupId));
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}

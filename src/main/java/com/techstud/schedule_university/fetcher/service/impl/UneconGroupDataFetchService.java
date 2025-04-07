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

@Service("UNECON_GROUP_FETCHER")
@Slf4j
public class UneconGroupDataFetchService implements GroupFetcherService {

    @Override
    public List<GroupData> fetchGroupsData() {
        List<GroupData> groupDataList = new ArrayList<>();
        String baseUrl = "https://rasp.unecon.ru/raspisanie.php";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(baseUrl);

            httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
            httpGet.addHeader("Referer", "https://rasp.unecon.ru/raspisanie.php");
            httpGet.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

            String responseBody = httpClient.execute(httpGet, createResponseHandler(String.class, true));
            Document document = Jsoup.parse(responseBody);

            Elements facultyElements = document.select(".fakultets a");
            for (Element facultyElement : facultyElements) {
                String facultyLink = facultyElement.attr("href");

                parseFacultyGroups(httpClient, facultyLink, groupDataList);
            }

        } catch (Exception e) {
            log.error("Error while fetching groups: {}", e.getMessage());
        }

        return groupDataList.stream()
                .sorted(Comparator.comparing(GroupData::universityGroupId))
                .collect(Collectors.toList());
    }

    private void parseFacultyGroups(CloseableHttpClient httpClient, String facultyLink, List<GroupData> groupDataList) {
        String facultyUrl = "https://rasp.unecon.ru" + (facultyLink.startsWith("/") ? facultyLink : "/" + facultyLink);

        try {
            HttpGet httpGet = new HttpGet(facultyUrl);

            httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
            httpGet.addHeader("Referer", "https://rasp.unecon.ru/");
            httpGet.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

            String responseBody = httpClient.execute(httpGet, createResponseHandler(String.class, true));
            Document document = Jsoup.parse(responseBody);

            Elements courseLinks = document.select("div.kurses a");
            for (Element courseLink : courseLinks) {
                String courseHref = courseLink.attr("href");

                String courseUrl = "https://rasp.unecon.ru/" + courseHref;

                parseCourseGroups(httpClient, courseUrl, groupDataList);
            }

        } catch (Exception e) {
            log.error("Error while parsing faculty groups: {}", e.getMessage());
        }
    }

    private void parseCourseGroups(CloseableHttpClient httpClient, String courseUrl, List<GroupData> groupDataList) {
        try {
            HttpGet httpGet = new HttpGet(courseUrl);

            httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
            httpGet.addHeader("Referer", "https://rasp.unecon.ru/");
            httpGet.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

            String responseBody = httpClient.execute(httpGet, createResponseHandler(String.class, true));
            Document document = Jsoup.parse(responseBody);

            Elements groupLinks = document.select("div.grps a");
            for (Element groupLink : groupLinks) {
                String href = groupLink.attr("href");
                String groupCode = groupLink.text();
                String universityGroupId = href.replace("raspisanie_grp.php?g=", "");

                groupDataList.add(new GroupData(groupCode, universityGroupId));
                log.info("Parsed group: {} (ID: {})", groupCode, universityGroupId);
            }

        } catch (Exception e) {
            log.error("Error while parsing course groups: {}", e.getMessage());
        }
    }
}

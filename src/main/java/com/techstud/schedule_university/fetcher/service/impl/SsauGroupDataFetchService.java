package com.techstud.schedule_university.fetcher.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.techstud.schedule_university.fetcher.dto.GroupData;
import com.techstud.schedule_university.fetcher.dto.api.response.SsauApiGroupDataResponse;
import com.techstud.schedule_university.fetcher.service.GroupFetcherService;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.techstud.schedule_university.fetcher.util.FetcherHttpUtils.createResponseHandler;

@Service("SSAU_GROUP_FETCHER")
@Slf4j
public class SsauGroupDataFetchService implements GroupFetcherService {

    @Override
    public List<GroupData> fetchGroupsData() {
        List<GroupData> groupDataList = new ArrayList<>();
        String baseUrl = "https://ssau.ru";
        String csrfToken;
        String fullCookies;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpGet httpGet = new HttpGet(baseUrl);
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

            HttpClientResponseHandler<String> csrfResponseHandler =
                    createResponseHandler(String.class, true);

            String responseBody = httpClient.execute(httpGet, csrfResponseHandler);
            Document document = Jsoup.parse(responseBody);
            Element csrfTag = document.selectFirst("meta[name=csrf-token]");

            if (csrfTag != null) {
                csrfToken = csrfTag.attr("content");
                log.info("CSRF token: {}", csrfToken);
            } else {
                log.error("CSRF token not found");
                return groupDataList;
            }

            fullCookies = Arrays.stream(httpClient.execute(httpGet).getHeaders("Set-Cookie"))
                    .map(header -> header.getValue().split(";")[0])
                    .collect(Collectors.joining("; "));

            Header[] commonHeaders = {
                    new BasicHeader("Accept", "application/json"),
                    new BasicHeader("Accept-Encoding", "gzip, deflate, br, zstd"),
                    new BasicHeader("Accept-Language", "ru,en;q=0.9"),
                    new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"),
                    new BasicHeader("Cookie", fullCookies),
                    new BasicHeader("Origin", baseUrl),
                    new BasicHeader("Referer", baseUrl + "/rasp"),
                    new BasicHeader("Sec-Ch-Ua", "\"Chromium\";v=\"128\", \"Not;A=Brand\";v=\"24\", \"YaBrowser\";v=\"24.10\", \"Yowser\";v=\"2.5"),
                    new BasicHeader("Sec-Ch-Ua-Mobile", "?0"),
                    new BasicHeader("Sec-Fetch-Dest", "empty"),
                    new BasicHeader("Sec-Fetch-Mode", "cors"),
                    new BasicHeader("Sec-Fetch-Site", "same-origin"),
                    new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 YaBrowser/24.10.0.0 Safari/537.36"),
                    new BasicHeader("X-Csrf-Token", csrfToken),
                    new BasicHeader("X-Requested-With", "XMLHttpRequest")
            };

            List<String> patterns = IntStream.rangeClosed(1, 9)
                    .boxed()
                    .flatMap(i -> IntStream.rangeClosed(1, 9).mapToObj(j -> String.format("%d%d", i, j)))
                    .toList();

            for (String pattern : patterns) {
                HttpPost httpPost = new HttpPost(baseUrl + "/rasp/search");
                httpPost.setHeaders(commonHeaders);

                List<NameValuePair> params = Collections.singletonList(new BasicNameValuePair("text", pattern));
                httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

                HttpClientResponseHandler<List<SsauApiGroupDataResponse>> postResponseHandler =
                        createResponseHandler(new TypeReference<>() {
                        });

                try {
                    List<SsauApiGroupDataResponse> apiResponses = httpClient.execute(httpPost, postResponseHandler);

                    for (SsauApiGroupDataResponse apiResponse : apiResponses) {
                        groupDataList.add(new GroupData(apiResponse.text(), apiResponse.id().toString()));
                    }
                } catch (Exception e) {
                    log.error("Error processing pattern {}: {}", pattern, e.getMessage());
                }
            }


        } catch (Exception e) {
            log.error("Error fetching group data", e);
        }

        return groupDataList;
    }

}

package com.techstud.schedule_university.fetcher.service.impl;

import com.techstud.schedule_university.fetcher.dto.GroupData;
import com.techstud.schedule_university.fetcher.dto.api.response.BmstuApiGroupDataResponse;
import com.techstud.schedule_university.fetcher.service.GroupFetcherService;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.message.BasicHeader;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.techstud.schedule_university.fetcher.util.FetcherHttpUtils.createResponseHandler;

@Service("BMSTU_GROUP_FETCHER")
@Slf4j
public class BmstuGroupDataFetchService implements GroupFetcherService {

    @Override
    public List<GroupData> fetchGroupsData() {
        List<GroupData> groupDataList = new ArrayList<>();
        String baseUrl = "https://lks.bmstu.ru/lks-back/api/v1/structure";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = getHttpGet(baseUrl);

            HttpClientResponseHandler<BmstuApiGroupDataResponse> responseHandler =
                    createResponseHandler(BmstuApiGroupDataResponse.class, false);

            BmstuApiGroupDataResponse apiResponse = httpClient.execute(httpGet, responseHandler);

            groupDataList = mapApiResponseToGroupDataList(apiResponse);
        } catch (Exception e) {
            log.error("Error fetching group data", e);
        }
        return groupDataList;
    }

    private static HttpGet getHttpGet(String baseUrl) {
        HttpGet httpGet = new HttpGet(baseUrl);

        Header[] commonHeaders = {
                new BasicHeader("Accept", "*/*"),
                new BasicHeader("Accept-Encoding", "gzip, deflate, br, zstd"),
                new BasicHeader("Accept-Language", "ru,en;q=0.9"),
                new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"),
                new BasicHeader("Referer", "https://lks.bmstu.ru/schedule/"),
                new BasicHeader("Sec-Ch-Ua", "\"Chromium\";v=\"128\", \"Not;A=Brand\";v=\"24\", \"YaBrowser\";v=\"24.10\", \"Yowser\";v=\"2.5"),
                new BasicHeader("Sec-Ch-Ua-Mobile", "?0"),
                new BasicHeader("Sec-Ch-Ua-Platform", "Windows"),
                new BasicHeader("Sec-Fetch-Dest", "empty"),
                new BasicHeader("Sec-Fetch-Mode", "cors"),
                new BasicHeader("Sec-Fetch-Site", "same-origin"),
                new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 YaBrowser/24.10.0.0 Safari/537.36")
        };

        httpGet.setHeaders(commonHeaders);
        return httpGet;
    }

    private List<GroupData> mapApiResponseToGroupDataList(BmstuApiGroupDataResponse apiResponseData) {
        List<GroupData> groupDataList = new ArrayList<>();

        if (apiResponseData != null && apiResponseData.getData() != null) {
            BmstuApiGroupDataResponse.BmstuApiResponseData responseData = apiResponseData.getData();

            processLevel1(responseData.getChildren(), groupDataList);
        }

        return groupDataList;
    }

    private void processLevel1(List<BmstuApiGroupDataResponse.BmstuApiResponseChildrenLevel1> level1Children, List<GroupData> groupDataList) {
        if (level1Children != null) {
            for (BmstuApiGroupDataResponse.BmstuApiResponseChildrenLevel1 child : level1Children) {
                processLevel2(child.getChildren(), groupDataList);
            }
        }
    }

    private void processLevel2(List<BmstuApiGroupDataResponse.BmstuApiResponseChildrenLevel2> level2Children, List<GroupData> groupDataList) {
        if (level2Children != null) {
            for (BmstuApiGroupDataResponse.BmstuApiResponseChildrenLevel2 child : level2Children) {
                processLevel3(child.getChildren(), groupDataList);
            }
        }
    }

    private void processLevel3(List<BmstuApiGroupDataResponse.BmstuApiResponseChildrenLevel3> level3Children, List<GroupData> groupDataList) {
        if (level3Children != null) {
            for (BmstuApiGroupDataResponse.BmstuApiResponseChildrenLevel3 child : level3Children) {
                processLevel4(child.getChildren(), groupDataList);
            }
        }
    }

    private void processLevel4(List<BmstuApiGroupDataResponse.BmstuApiResponseChildrenLevel4> level4Children, List<GroupData> groupDataList) {
        if (level4Children != null) {
            for (BmstuApiGroupDataResponse.BmstuApiResponseChildrenLevel4 child : level4Children) {
                processLevel5(child.getChildren(), groupDataList);
            }
        }
    }

    private void processLevel5(List<BmstuApiGroupDataResponse.BmstuApiResponseChildrenLevel5> level5Children, List<GroupData> groupDataList) {
        if (level5Children != null) {
            for (BmstuApiGroupDataResponse.BmstuApiResponseChildrenLevel5 child : level5Children) {
                groupDataList.add(new GroupData(child.getAbbr(), child.getUuid()));
            }
        }
    }
}

package com.techstud.schedule_university.fetcher.service;

import com.techstud.schedule_university.fetcher.dto.GroupData;

import java.util.List;

public interface GroupFetcherService {

    List<GroupData> fetchGroupsData();

}

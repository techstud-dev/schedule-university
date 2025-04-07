package com.techstud.schedule_university.fetcher.dto.api.response;

import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
public final class BmstuApiGroupDataResponse {

    private BmstuApiResponseData data;
    private Date date;

    @Getter
    public abstract static class BmstuTemplate {
        private String abbr;
        private String uuid;
        private Long course;
        private String name;
        private String nodeType;
        private Long semester;
        private String parentUuid;
    }

    @Getter
    public static class BmstuApiResponseData extends BmstuTemplate {
        private List<BmstuApiResponseChildrenLevel1> children;
    }

    @Getter
    public static class BmstuApiResponseChildrenLevel1 extends BmstuTemplate {
        private List<BmstuApiResponseChildrenLevel2> children;
    }
    @Getter
    public static class BmstuApiResponseChildrenLevel2 extends BmstuTemplate {
        private List<BmstuApiResponseChildrenLevel3> children;
    }

    @Getter
    public static class BmstuApiResponseChildrenLevel3 extends BmstuTemplate {
        private List<BmstuApiResponseChildrenLevel4> children;
    }

    @Getter
    public static class BmstuApiResponseChildrenLevel4 extends BmstuTemplate {
        private List<BmstuApiResponseChildrenLevel5> children;
    }

    @Getter
    public static class BmstuApiResponseChildrenLevel5 extends BmstuTemplate  {

    }
}

package io.littlehorse.model;

import io.littlehorse.sdk.common.proto.WfRunIdList;
import java.util.Base64;
import java.util.List;

public class WfRunIdListModel {
    private final List<String> wfRunIdList;
    private final String bookmark;

    public WfRunIdListModel(List<String> wfRunIdList, String bookmark) {
        this.wfRunIdList = wfRunIdList;
        this.bookmark = bookmark;
    }

    public static WfRunIdListModel fromProto(WfRunIdList wfRunIdList) {
        List<String> wfRunIdArr =
                wfRunIdList.getResultsList().stream().map(item -> item.getId()).toList();
        String bookmark =
                Base64.getEncoder().encodeToString(wfRunIdList.getBookmark().toByteArray());

        return new WfRunIdListModel(wfRunIdArr, bookmark);
    }

    public List<String> getWfRunIdList() {
        return wfRunIdList;
    }

    public String getBookmark() {
        return bookmark;
    }
}

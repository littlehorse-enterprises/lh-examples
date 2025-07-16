package io.littlehorse.model;

import java.util.List;

public class SearchIdentityVerificationStatusResponse {
    public List<IdentityVerificationStatus> results;
    public String bookmark;

    public SearchIdentityVerificationStatusResponse(List<IdentityVerificationStatus> results, SearchBookmark bookmark) {
        this.results = results;
        this.bookmark = bookmark.toString();
    }
}

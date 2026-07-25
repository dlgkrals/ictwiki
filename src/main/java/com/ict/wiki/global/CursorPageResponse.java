package com.ict.wiki.global;

import lombok.Getter;

import java.util.List;

@Getter
public class CursorPageResponse<T> {

    private final List<T> content;
    private final Long nextCursor;
    private final boolean hasNext;

    public CursorPageResponse(List<T> content, Long nextCursor, boolean hasNext) {
        this.content = content;
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
    }

    public static <T> CursorPageResponse<T> empty() {
        return new CursorPageResponse<>(List.of(), null, false);
    }
}

package com.ict.wiki.inquiry.events;

import com.ict.wiki.inquiry.domain.Inquiry;
import lombok.Getter;

/**
 * 민원 완료 이벤트
 * - InquiryService.complete() 에서 발행
 * - RagEventHandler 에서 구독 → 임베딩 저장
 */
@Getter
public class InquiryCompletedEvent {

    private final Long inquiryId;
    private final Inquiry inquiry;

    public InquiryCompletedEvent(Inquiry inquiry) {
        this.inquiryId = inquiry.getId();
        this.inquiry = inquiry;
    }
}
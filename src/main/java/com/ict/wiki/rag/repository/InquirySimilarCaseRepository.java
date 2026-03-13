package com.ict.wiki.rag.repository;

import com.ict.wiki.inquiry.domain.Inquiry;
import com.ict.wiki.rag.domain.InquirySimilarCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InquirySimilarCaseRepository extends JpaRepository<InquirySimilarCase, Long> {

    Optional<InquirySimilarCase> findByInquiry(Inquiry inquiry);

    boolean existsByInquiry(Inquiry inquiry);
}
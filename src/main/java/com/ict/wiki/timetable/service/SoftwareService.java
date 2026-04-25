package com.ict.wiki.timetable.service;

import com.ict.wiki.exception.code.TimetableErrorCode;
import com.ict.wiki.exception.custom.TimetableException;
import com.ict.wiki.timetable.domain.Software;
import com.ict.wiki.timetable.repository.SoftwareRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 소프트웨어 서비스
 * - 소프트웨어 마스터 CRUD
 * - alias 기반 검색 (한/영, 띄어쓰기, 연도 표기 차이 흡수)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SoftwareService {

    private final SoftwareRepository softwareRepository;
    private final ObjectMapper objectMapper;

    // ========== 조회 ==========

    /**
     * 전체 소프트웨어 목록 (기본SW 우선, 이름 순)
     */
    public List<Software> getAllSoftwares() {
        return softwareRepository.findAllOrdered();
    }

    @Transactional
    public Software updateVersionSensitive(Long id, boolean versionSensitive) {
        Software sw = findById(id);
        sw.updateVersionSensitive(versionSensitive);
        return sw;
    }

    /**
     * 단건 조회
     */
    public Software findById(Long id) {
        return softwareRepository.findById(id)
                .orElseThrow(() -> TimetableException.of(TimetableErrorCode.SOFTWARE_NOT_FOUND));
    }

    /**
     * 소프트웨어 검색 (alias 기반)
     * - 입력값 normalize: 소문자 변환 + 공백 제거 + 연도 패턴 제거
     * - 예: "스케치 업 2025" → "스케치업" 으로 normalize 후 alias 매칭
     */
    public List<Software> search(String keyword) {
        String normalized = normalize(keyword);
        return softwareRepository.searchByNameOrAlias(normalized);
    }

    /**
     * ID 목록으로 일괄 조회
     */
    public List<Software> findAllByIds(List<Long> ids) {
        return softwareRepository.findAllById(ids);
    }

    // ========== 등록/수정/삭제 ==========

    /**
     * 소프트웨어 등록
     */
    @Transactional
    public Software createSoftware(String name, List<String> aliases, boolean isDefault) {
        Optional<Software> existing = softwareRepository.findByName(name);
        if (existing.isPresent()) {
            log.info("이미 존재하는 소프트웨어 재사용 - name: {}", name);
            return existing.get();
        }
        String aliasJson = toJson(aliases);
        Software software = Software.of(name, aliasJson, isDefault);
        log.info("소프트웨어 등록 - name: {}, isDefault: {}", name, isDefault);
        return softwareRepository.save(software);
    }

    /**
     * alias 목록 수정
     */
    @Transactional
    public Software updateAliases(Long id, List<String> aliases) {
        Software software = findById(id);
        software.updateAliases(toJson(aliases));
        log.info("소프트웨어 alias 수정 - id: {}, name: {}", id, software.getName());
        return software;
    }

    /**
     * 소프트웨어명 수정
     */
    @Transactional
    public Software updateName(Long id, String name) {
        Software software = findById(id);
        software.updateName(name);
        return software;
    }

    /**
     * 기본 소프트웨어 여부 변경
     */
    @Transactional
    public Software updateIsDefault(Long id, boolean isDefault) {
        Software software = findById(id);
        software.updateIsDefault(isDefault);
        return software;
    }

    /**
     * 소프트웨어 삭제
     */
    @Transactional
    public void deleteSoftware(Long id) {
        Software software = findById(id);
        softwareRepository.delete(software);
        log.info("소프트웨어 삭제 - id: {}, name: {}", id, software.getName());
    }

    // ========== 내부 유틸 ==========

    /**
     * 검색어 normalize
     * 1. 소문자 변환
     * 2. 연도 패턴 제거 (4자리 숫자)
     * 3. 공백 제거
     */
    public String normalize(String input) {
        if (input == null) return "";
        return input.toLowerCase()
                .replaceAll("\\b\\d{4}\\b", "")  // 연도 제거 (2024, 2025 등)
                .replaceAll("\\s+", "")            // 공백 제거
                .trim();
    }

    private String toJson(List<String> aliases) {
        try {
            return objectMapper.writeValueAsString(aliases != null ? aliases : new ArrayList<>());
        } catch (Exception e) {
            log.error("alias JSON 변환 실패", e);
            return "[]";
        }
    }

    public List<String> fromJson(String aliasJson) {
        try {
            if (aliasJson == null || aliasJson.isBlank()) return new ArrayList<>();
            return objectMapper.readValue(aliasJson, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("alias JSON 파싱 실패", e);
            return new ArrayList<>();
        }
    }
}
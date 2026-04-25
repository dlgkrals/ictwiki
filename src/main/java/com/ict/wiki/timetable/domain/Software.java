package com.ict.wiki.timetable.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 소프트웨어 마스터
 * - 설치 가능한 소프트웨어 목록 관리
 * - alias 기반 검색: 한/영, 띄어쓰기, 연도 표기 차이 흡수
 * - 예: "스케치업", "스케치 업", "SketchUp", "sketchup 2025" → 동일 소프트웨어
 */
@Entity
@Table(name = "softwares")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Software {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 소프트웨어 공식 명칭
     * - 예: "SketchUp", "Adobe Photoshop", "Microsoft Excel"
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * 검색용 별칭 목록 (JSON 배열로 저장)
     * - 예: ["스케치업", "스케치 업", "sketchup", "SKETCH UP"]
     * - 보강 신청 시 소프트웨어명 검색에 사용
     */
    @Column(columnDefinition = "JSON")
    private String aliases;

    /**
     * 기본 소프트웨어 여부
     * - true: 전 강의실 공통 설치 대상 (Excel, 한글 등)
     * - false: 특정 강의실에만 설치되는 특수 소프트웨어
     * - 구형 PC의 경우 is_default=true여도 미설치일 수 있으므로
     *   실제 설치 여부는 ClassroomSoftware 기준으로 판단
     */
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    /**
     * 버전 민감 여부
     * - true: 버전별로 다른 소프트웨어 (VMware 2016 ≠ VMware 2017)
     * - false: 버전 달라도 같은 소프트웨어 (기본값)
     */
    @Column(nullable = false)
    private boolean versionSensitive = false;

    // ========== 생성 메서드 ==========

    public static Software of(String name, String aliases, boolean isDefault) {
        Software software = new Software();
        software.name = name;
        software.aliases = aliases;
        software.isDefault = isDefault;
        return software;
    }

    // ========== 비즈니스 메서드 ==========

    /**
     * alias 목록 수정
     */
    public void updateAliases(String aliases) {
        this.aliases = aliases;
    }

    /**
     * 기본 소프트웨어 여부 변경
     */
    public void updateIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * 소프트웨어명 수정
     */
    public void updateName(String name) {
        this.name = name;
    }

    /**
     * 버전 민감도 수정
     */
    public void updateVersionSensitive(boolean versionSensitive) {
        this.versionSensitive = versionSensitive;
    }
}
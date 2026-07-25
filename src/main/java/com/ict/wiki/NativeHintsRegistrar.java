package com.ict.wiki;

import com.ict.wiki.admin.dto.response.UserManagementResponse;
import com.ict.wiki.config.FloatVectorType;
import com.ict.wiki.login.dto.request.LoginRequest;
import com.ict.wiki.login.dto.response.LoginResponse;
import com.ict.wiki.login.dto.response.UserResponse;
import com.ict.wiki.login.dto.response.UserSummaryResponse;
import com.ict.wiki.report.dto.response.DailyReportResponse;
import com.ict.wiki.timetable.dto.request.ScheduleExcelImportRequest;
import com.ict.wiki.timetable.dto.request.ScheduleImportRequest;
import com.ict.wiki.timetable.dto.response.ScheduleExportResponse;
import com.ict.wiki.util.AesEncryptionUtil;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class NativeHintsRegistrar implements RuntimeHintsRegistrar {

    // ✅ Native 힌트 등록이 필요한 클래스들 - 여기만 관리
    private static final Class<?>[] REFLECTION_CLASSES = {
            // 요청/응답 DTO
            LoginRequest.class,
            LoginResponse.class,
            UserResponse.class,
            UserSummaryResponse.class,
            UserManagementResponse.class,

            // 업무일지
            DailyReportResponse.class,
            DailyReportResponse.InquiryRowData.class,
            DailyReportResponse.LocationLine.class,

            // 시간표 내보내기
            ScheduleExportResponse.class,
            ScheduleExportResponse.FloorData.class,
            ScheduleExportResponse.DayData.class,
            ScheduleExportResponse.PeriodRow.class,
            ScheduleExportResponse.NightGroup.class,
            ScheduleExportResponse.CellData.class,

            // JPA Converter
            AesEncryptionUtil.class,

            // Hibernate UserType (pgvector)
            FloatVectorType.class,

            // 시간표 업로드 Request
            ScheduleImportRequest.class,
            ScheduleExcelImportRequest.class,
    };

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        for (Class<?> clazz : REFLECTION_CLASSES) {
            hints.reflection().registerType(clazz, hint -> hint.withMembers(
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.DECLARED_FIELDS
            ));
        }
    }
}
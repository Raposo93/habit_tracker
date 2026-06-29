package com.raposo.habittracker.web.report;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.raposo.habittracker.application.GetHabitReportBetweenDatesUseCase;
import com.raposo.habittracker.application.GetHabitReportLastWeekUseCase;
import com.raposo.habittracker.application.report.HabitReport;
import com.raposo.habittracker.domain.DateRange;

@RestController
@RequestMapping("/api/reports")
class ReportController {

    private final GetHabitReportLastWeekUseCase getHabitReportLastWeekUseCase;
    private final GetHabitReportBetweenDatesUseCase getHabitReportBetweenDatesUseCase;
    private final ReportResponseMapper mapper;

    ReportController(
            GetHabitReportLastWeekUseCase getHabitReportLastWeekUseCase,
            GetHabitReportBetweenDatesUseCase getHabitReportBetweenDatesUseCase,
            ReportResponseMapper reportResponseMapper) {
        this.getHabitReportLastWeekUseCase = getHabitReportLastWeekUseCase;
        this.getHabitReportBetweenDatesUseCase = getHabitReportBetweenDatesUseCase;
        this.mapper = reportResponseMapper;
    }

    @GetMapping("/last-week")
    ReportResponse lastWeek() {
        HabitReport report = getHabitReportLastWeekUseCase.execute();

        return mapper.toResponse(report);
    }

    @GetMapping
    ReportResponse betweenDates(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        DateRange dateRange = new DateRange(startDate, endDate);
        HabitReport report = getHabitReportBetweenDatesUseCase.execute(dateRange);

        return mapper.toResponse(report);
    }
}

package com.raposo.habittracker.web.report;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import com.raposo.habittracker.application.GetHabitReportBetweenDatesUseCase;
import com.raposo.habittracker.application.GetHabitReportLastWeekUseCase;
import com.raposo.habittracker.application.report.HabitReport;
import com.raposo.habittracker.application.report.ReportContext;
import com.raposo.habittracker.domain.DateRange;
import com.raposo.habittracker.web.report.ReportResponse.HabitSummaryResponse;
import com.raposo.habittracker.web.report.ReportResponse.ReportContextResponse;
import com.raposo.habittracker.web.report.ReportResponse.ReportRangeResponse;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GetHabitReportLastWeekUseCase getHabitReportLastWeekUseCase;

	@MockitoBean
	private GetHabitReportBetweenDatesUseCase getHabitReportBetweenDatesUseCase;

	@MockitoBean
	private ReportResponseMapper reportResponseMapper;

	@Test
	void givenLastWeekReportWhenGetLastWeekThenReturnReportJson() throws Exception {
		HabitReport report = report();
		ReportResponse response = responseWithNoBaseline();

		given(getHabitReportLastWeekUseCase.execute()).willReturn(report);
		given(reportResponseMapper.toResponse(report)).willReturn(response);

		mockMvc.perform(get("/api/reports/last-week"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.summary[0].trend").value("NO_BASELINE"))
				.andExpect(jsonPath("$.summary[0].previousPeriodScore").isEmpty())
				.andExpect(jsonPath("$.summary[0].delta").isEmpty());
	}

	@Test
	void givenStartAndEndDateWhenGetReportThenReturnReportJson() throws Exception {
		String startDate = "2026-05-25";
		String endDate = "2026-05-31";
		DateRange currentRange = DateRange.of(
				LocalDate.parse(startDate),
				LocalDate.parse(endDate));

		HabitReport report = report();
		ReportResponse response = responseWithNoBaseline();

		given(getHabitReportBetweenDatesUseCase.execute(currentRange)).willReturn(report);
		given(reportResponseMapper.toResponse(report)).willReturn(response);

		mockMvc.perform(get("/api/reports")
				.param("startDate", startDate)
				.param("endDate", endDate))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currentRange.start").value(startDate))
				.andExpect(jsonPath("$.currentRange.end").value(endDate))
				.andExpect(jsonPath("$.summary[0].trend").value("NO_BASELINE"));

	}

	private ReportResponse responseWithNoBaseline() {
		return new ReportResponse(
				new ReportContextResponse("scoreScale"),
				new ReportRangeResponse(
						LocalDate.of(2026, 5, 25),
						LocalDate.of(2026, 5, 31)),
				new ReportRangeResponse(
						LocalDate.of(2026, 5, 18),
						LocalDate.of(2026, 5, 24)),
				List.of(summaryWithNoBaseline()),
				List.of());
	}

	private HabitSummaryResponse summaryWithNoBaseline() {
		return new HabitSummaryResponse(
				"habit",
				null,
				0.0,
				null,
				"NO_BASELINE",
				0,
				0,
				0,
				0);
	}

	private HabitReport report() {
		DateRange currentRange = DateRange.of(
				LocalDate.of(2026, 5, 25),
				LocalDate.of(2026, 5, 31));

		return new HabitReport(
				new ReportContext("", "", ""),
				currentRange,
				currentRange.previousEquivalent(),
				List.of(),
				List.of());
	}
}

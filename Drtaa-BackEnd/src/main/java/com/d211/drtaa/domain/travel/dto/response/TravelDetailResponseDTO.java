package com.d211.drtaa.domain.travel.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class TravelDetailResponseDTO {
    @Schema(description = "여행 고유 번호", example = "1")
    private Long travelId;
    @Schema(description = "여행 이름", example = "서울 여행")
    private String travelName;
    @Schema(description = "여행 시작 일정", example = "2024/01/01")
    private LocalDate travelStartDate;
    @Schema(description = "여행 종료 일정", example = "2024/01/02")
    private LocalDate travelEndDate;

    private List<DatesDetailResponseDTO> datesDetail = new ArrayList<>();
}

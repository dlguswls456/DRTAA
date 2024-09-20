package com.d211.drtaa.domain.travel.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class PlacesDetailResponseDTO {
    @Schema(description = "일정 장소 고유번호", example = "1")
    private long travelDatesId;
    @Schema(description = "일정 장소 이름", example = "디지털미디어시티역")
    private String datePlacesName;
    @Schema(description = "일정 장소 위도", example = "0.0")
    private double datePlacesLat;
    @Schema(description = "일정 장소 경도", example = "0.0")
    private double datePlacesLon;
}
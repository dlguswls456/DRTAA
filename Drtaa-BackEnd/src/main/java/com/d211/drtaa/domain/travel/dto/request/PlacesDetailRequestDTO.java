package com.d211.drtaa.domain.travel.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Getter
public class PlacesDetailRequestDTO {
    @Schema(description = "여행 일정 고유번호", example = "1")
    private Long travelDatesId;
    @Schema(description = "일정 장소 고유번호", example = "1")
    private long datePlacesId;
    @Schema(description = "여행 일정 장소 순서", example = "1")
    private int datePlacesOrder;
    @Schema(description = "일정 장소 이름", example = "디지털미디어시티역")
    private String datePlacesName;
    @Schema(description = "일정 장소 카테고리", example = "지하철역")
    private String datePlacesCategory;
    @Schema(description = "일정 장소 주소", example = "서울특별시 마포구 월드컵북로 366")
    private String datePlacesAddress;
    @Schema(description = "일정 장소 위도", example = "0.0")
    private double datePlacesLat;
    @Schema(description = "일정 장소 경도", example = "0.0")
    private double datePlacesLon;
    @Schema(description = "일정 장소 방문 여부", example = "false")
    private boolean datePlacesIsVisited;
    @Schema(description = "일정 장소 만료 여부", example = "false")
    private boolean datePlacesIsExpired;

    public boolean getDatePlacesIsVisited() {
        return this.datePlacesIsVisited;
    }
    public boolean getDatePlacesIsExpired() {
        return this.datePlacesIsExpired;
    }
}

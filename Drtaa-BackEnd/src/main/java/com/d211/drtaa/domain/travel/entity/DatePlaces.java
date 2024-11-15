package com.d211.drtaa.domain.travel.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "date_places")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DatePlaces {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "date_places_id", nullable = false)
    @Schema(description = "일정 장소 고유번호", example = "1")
    private long datePlacesId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_id", nullable = false)
    @Schema(description = "여행 고유번호", example = "1")
    private Travel travel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_dates_id", nullable = false)
    @Schema(description = "여행 일정 고유번호", example = "1")
    private TravelDates travelDates;

    @Column(name = "date_places_order", nullable = false)
    @Schema(description = "여행 일정 장소 순서", example = "1")
    private int datePlacesOrder;

    @Column(name = "date_places_name", nullable = false)
    @Schema(description = "일정 장소 이름", example = "디지털미디어시티역")
    private String datePlacesName;

    @Column(name = "date_places_category", nullable = false)
    @Schema(description = "일정 장소 카테고리", example = "지하철역")
    private String datePlacesCategory;

    @Column(name = "date_places_address", nullable = false)
    @Schema(description = "일정 장소 주소", example = "서울특별시 마포구 월드컵북로 366")
    private String datePlacesAddress;

    @Column(name = "date_places_lat", nullable = false)
    @Schema(description = "일정 장소 위도", example = "0.0")
    private double datePlacesLat;

    @Column(name = "date_places_lon", nullable = false)
    @Schema(description = "일정 장소 경도", example = "0.0")
    private double datePlacesLon;

    @Column(name = "date_places_is_visited", nullable = false)
    @ColumnDefault("0")
    @Schema(description = "일정 장소 방문 여부", example = "false")
    private boolean datePlacesIsVisited;

    @Column(name = "date_places_is_expired", nullable = false)
    @ColumnDefault("0")
    @Schema(description = "일정 장소 만료 여부", example = "false")
    private boolean datePlacesIsExpired;

    public boolean getDatePlacesIsVisited() {
        return this.datePlacesIsVisited;
    }

    public boolean getDatePlacesIsExpired() {
        return this.datePlacesIsExpired;
    }
}

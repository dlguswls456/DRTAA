package com.d211.drtaa.domain.rent.service;

import com.d211.drtaa.domain.rent.dto.request.RentStatusRequestDTO;
import com.d211.drtaa.domain.rent.dto.request.RentCreateRequestDTO;
import com.d211.drtaa.domain.rent.dto.request.RentEditRequestDTO;
import com.d211.drtaa.domain.rent.dto.request.RentTimeRequestDTO;
import com.d211.drtaa.domain.rent.dto.response.RentDetailResponseDTO;
import com.d211.drtaa.domain.rent.dto.response.RentResponseDTO;
import com.d211.drtaa.domain.rent.entity.Rent;
import com.d211.drtaa.domain.rent.entity.RentStatus;
import com.d211.drtaa.domain.rent.entity.car.RentCar;
import com.d211.drtaa.domain.rent.entity.car.RentCarSchedule;
import com.d211.drtaa.domain.rent.entity.car.RentDrivingStatus;
import com.d211.drtaa.domain.rent.repository.RentRepository;
import com.d211.drtaa.domain.rent.repository.car.RentCarRepository;
import com.d211.drtaa.domain.rent.repository.car.RentCarScheduleRepository;
import com.d211.drtaa.domain.rent.service.history.RentHistoryService;
import com.d211.drtaa.domain.travel.entity.Travel;
import com.d211.drtaa.domain.travel.entity.TravelDates;
import com.d211.drtaa.domain.travel.repository.TravelDatesRepository;
import com.d211.drtaa.domain.travel.repository.TravelRepository;
import com.d211.drtaa.domain.user.entity.User;
import com.d211.drtaa.domain.user.repository.UserRepository;
import com.d211.drtaa.global.exception.rent.RentCarNotFoundException;
import com.d211.drtaa.global.exception.rent.RentCarScheduleNotFoundException;
import com.d211.drtaa.global.exception.rent.RentNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class RentServiceImpl implements RentService{

    private final RentHistoryService rentHistoryService;

    private final UserRepository userRepository;
    private final RentRepository rentRepository;
    private final RentCarRepository rentCarRepository;
    private final RentCarScheduleRepository rentCarScheduleRepository;
    private final TravelRepository travelRepository;
    private final TravelDatesRepository travelDatesRepository;

    @Override
    public List<RentResponseDTO> getAllRent(String userProviderId) {
        // 사용자 찾기
        User user = userRepository.findByUserProviderId(userProviderId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 userProviderId의 맞는 회원을 찾을 수 없습니다."));

        // 사용자의 렌트 찾기
        List<Rent> rents = rentRepository.findByUser(user);

        List<RentResponseDTO> response = new ArrayList<>();
        for(Rent rent: rents) {
            RentResponseDTO dto = RentResponseDTO.builder()
                    .rentId(rent.getRentId())
                    .rentStatus(rent.getRentStatus())
                    .rentHeadCount(rent.getRentHeadCount())
                    .rentTime(rent.getRentTime())
                    .rentStartTime(rent.getRentStartTime())
                    .build();

            response.add(dto);
        }

        return response;
    }

    @Override
    public RentDetailResponseDTO getDetailRent(long rentId) {
        Rent rent = rentRepository.findByRentId(rentId)
                .orElseThrow(() -> new RentNotFoundException("해당 rentId의 맞는 렌트를 찾을 수 없습니다."));

        RentCar rentCar = rent.getRentCar();

        RentDetailResponseDTO response = RentDetailResponseDTO.builder()
                .rentStatus(rent.getRentStatus())
                .rentHeadCount(rent.getRentHeadCount())
                .rentPrice(rent.getRentPrice())
                .rentTime(rent.getRentTime())
                .rentStartTime(rent.getRentStartTime())
                .rentEndTime(rent.getRentEndTime())
                .rentDptLat(rent.getRentDptLat())
                .rentDptLon(rent.getRentDptLon())
                .rentCreatedAt(rent.getRentCreatedAt())
                // rent-car
                .rentCarId(rentCar.getRentCarId())
                .rentCarNumber(rentCar.getRentCarNumber())
                .rentCarManufacturer(rentCar.getRentCarManufacturer())
                .rentCarModel(rentCar.getRentCarModel())
                .build();

        return response;
    }

    @Override
    @Transactional
    public RentDetailResponseDTO createRent(String userProviderId, RentCreateRequestDTO rentCreateRequestDTO) {
        // 사용자 찾기
        User user = userRepository.findByUserProviderId(userProviderId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 userProviderId의 맞는 회원을 찾을 수 없습니다."));

        // 시작, 종료 시간
        LocalDateTime startDateTime = rentCreateRequestDTO.getRentStartTime();
        LocalDateTime endDateTime = rentCreateRequestDTO.getRentEndTime();
        LocalDate startDate = startDateTime.toLocalDate();
        LocalDate endDate = endDateTime.toLocalDate();

        // ** 결제 **
        List<RentCar> availableCars = rentCarRepository.findAll().stream()
                .filter(car -> {
                    List<RentCarSchedule> schedules = rentCarScheduleRepository.findByRentCar(car);

                    if (schedules.isEmpty()) {
                        return true;
                    }

                    boolean isAvailable = schedules.stream().allMatch(schedule -> {
                        if (!schedule.isRentCarScheduleIsDone()) {
                            return !(startDateTime.toLocalDate().isBefore(schedule.getRentCarScheduleEndDate()) && endDateTime.toLocalDate().isAfter(schedule.getRentCarScheduleStartDate()));
                        }
                        return true;
                    });

                    return isAvailable;
                })
                .collect(Collectors.toList());

        if (availableCars.isEmpty())
            throw new RuntimeException("해당 기간에 사용 가능한 차량이 없습니다.");

        RentCar availableCar = availableCars.get(0);

        Travel travel = Travel.builder()
                .travelName(startDate + " 여행")
                .travelStartDate(startDate)
                .travelEndDate(endDate)
                .build();

        travelRepository.save(travel);

        while (!startDate.isAfter(endDate)) {
            TravelDates travelDates = TravelDates.builder()
                    .travel(travel)
                    .travelDatesDate(startDate)
                    .build();

            travelDatesRepository.save(travelDates);

            startDate = startDate.plusDays(1);
        }

        Rent rent = Rent.builder()
                .user(user)
                .rentCar(availableCar)
                .travel(travel)
                .rentStatus(RentStatus.reserved)
                .rentHeadCount(rentCreateRequestDTO.getRentHeadCount())
                .rentPrice(rentCreateRequestDTO.getRentPrice())
                .rentTime(rentCreateRequestDTO.getRentTime())
                .rentStartTime(startDateTime)
                .rentEndTime(endDateTime)
                .rentDptLat(rentCreateRequestDTO.getRentDptLat())
                .rentDptLon(rentCreateRequestDTO.getRentDptLon())
                .build();

        rentRepository.save(rent);

        availableCar.setRentCarDrivingStatus(RentDrivingStatus.parked);
        rentCarRepository.save(availableCar);

        RentCarSchedule rentCarSchedule = RentCarSchedule.builder()
                .rentCar(availableCar)
                .rentCarScheduleStartDate(startDateTime.toLocalDate())
                .rentCarScheduleEndDate(endDateTime.toLocalDate())
                .build();

        rentCarScheduleRepository.save(rentCarSchedule);

        RentDetailResponseDTO response = RentDetailResponseDTO.builder()
                .rentStatus(rent.getRentStatus())
                .rentHeadCount(rent.getRentHeadCount())
                .rentPrice(rent.getRentPrice())
                .rentTime(rent.getRentTime())
                .rentStartTime(rent.getRentStartTime())
                .rentEndTime(rent.getRentEndTime())
                .rentDptLat(rent.getRentDptLat())
                .rentDptLon(rent.getRentDptLon())
                .rentCreatedAt(rent.getRentCreatedAt())
                .rentCarId(rent.getRentCar().getRentCarId())
                .rentCarNumber(rent.getRentCar().getRentCarNumber())
                .rentCarManufacturer(rent.getRentCar().getRentCarManufacturer())
                .rentCarModel(rent.getRentCar().getRentCarModel())
                .build();

        return response;
    }

    @Override
    @Transactional
    public void updateRent(RentEditRequestDTO rentEditRequestDTO) {
        // 렌트 찾기
        Rent rent = rentRepository.findByRentId(rentEditRequestDTO.getRentId())
                .orElseThrow(() -> new RentNotFoundException("해당 rentId의 맞는 렌트를 찾을 수 없습니다."));

        // 상태 변경
        rent.setRentHeadCount(rentEditRequestDTO.getRentHeadCount() != null ? rentEditRequestDTO.getRentHeadCount() : rent.getRentHeadCount());
        rent.setRentDptLat(rentEditRequestDTO.getRentDptLat() != null ? rentEditRequestDTO.getRentDptLat() : rent.getRentDptLat());
        rent.setRentDptLon(rentEditRequestDTO.getRentDptLon() != null ? rentEditRequestDTO.getRentDptLon() : rent.getRentDptLon());

        // 변경 상태 저장
        rentRepository.save(rent);
    }

    @Override
    @Transactional
    public void rentStatusInProgress(long rentId) {
        // 렌트 찾기
        Rent rent = rentRepository.findByRentId(rentId)
                .orElseThrow(() -> new RentNotFoundException("해당 rentId의 맞는 렌트를 찾을 수 없습니다."));

        // 상태 변경
        rent.setRentStatus(RentStatus.in_progress);

        // 변경 상태 저장
        rentRepository.save(rent);
    }

    @Override
    @Transactional
    public void rentStatusCompleted(RentStatusRequestDTO requestDTO) {
        // 렌트 찾기
        Rent rent = rentRepository.findByRentId(requestDTO.getRentId())
                .orElseThrow(() -> new RentNotFoundException("해당 rentId의 맞는 렌트를 찾을 수 없습니다."));

        // 렌트 차량 탐색
        RentCar car = rentCarRepository.findByRentCarId(rent.getRentCar().getRentCarId())
                .orElseThrow(() -> new RentCarNotFoundException("해당 rentCarId의 맞는 차량을 찾을 수 없습니다."));

        // 렌트 차량 일정 탐색
        RentCarSchedule carSchedule = rentCarScheduleRepository.findByRentCarScheduleId(requestDTO.getRentCarScheduleId())
                .orElseThrow(() -> new RentCarScheduleNotFoundException("해당 렌트에 맞는 차량 일정을 찾을 수 없습니다."));

        // 상태 변경
        rent.setRentStatus(RentStatus.completed); // 완료
        carSchedule.setRentCarScheduleIsDone(true); // 완료
        
        // 렌트 기록 생성
        rentHistoryService.createHistory(rent.getUser().getUserProviderId(), rent.getRentId());

        // 변경 상태 저장
        rentRepository.save(rent);
        rentCarScheduleRepository.save(carSchedule);
    }

    @Override
    @Transactional
    public void rentStatusCanceld(RentStatusRequestDTO requestDTO) {
        // 렌트 찾기
        Rent rent = rentRepository.findByRentId(requestDTO.getRentId())
                .orElseThrow(() -> new RentNotFoundException("해당 rentId의 맞는 렌트를 찾을 수 없습니다."));

        // 렌트 차량 탐색
        RentCar car = rentCarRepository.findByRentCarId(rent.getRentCar().getRentCarId())
                .orElseThrow(() -> new RentCarNotFoundException("해당 rentCarId의 맞는 차량을 찾을 수 없습니다."));

        // 렌트 차량 일정 탐색
        RentCarSchedule carSchedule = rentCarScheduleRepository.findByRentCarScheduleId(requestDTO.getRentCarScheduleId())
                .orElseThrow(() -> new RentCarScheduleNotFoundException("해당 렌트에 맞는 차량 일정을 찾을 수 없습니다."));

        // 상태 변경
        rent.setRentStatus(RentStatus.canceled); // 취소
        carSchedule.setRentCarScheduleIsDone(true); // 완료

        // 변경 상태 저장
        rentRepository.save(rent);
        rentCarScheduleRepository.save(carSchedule);
    }

    @Override
    @Transactional
    public void updateRentTime(RentTimeRequestDTO rentTimeRequestDTO) {
        // 렌트 찾기
        Rent rent = rentRepository.findByRentId(rentTimeRequestDTO.getRentId())
                .orElseThrow(() -> new RentNotFoundException("해당 rentId의 맞는 렌트를 찾을 수 없습니다."));

        // 상태 변경
        rent.setRentTime(rentTimeRequestDTO.getRentTime());

        // 변경 상태 저장
        rentRepository.save(rent);
    }
}
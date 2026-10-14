package com.reservation.repository;
import com.reservation.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface TrainRepository extends JpaRepository<Train, Long> {
 Optional<Train> findByTrainNumber(Integer trainNumber);
 List<Train> findBySourceStationIgnoreCaseAndDestinationStationIgnoreCase(String sourceStation, String destinationStation);
}

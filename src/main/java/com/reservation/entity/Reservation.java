
package com.reservation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(
    name = "reservations",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_reservations_pnr",
        columnNames = "pnr"
    )
)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pnr", nullable = false, unique = true, length = 10)
    private String pnr;

    @NotBlank(message = "Passenger name is required.")
    @Size(max = 100)
    @Column(name = "passenger_name", nullable = false, length = 100)
    private String passengerName;

    @NotNull(message = "Passenger age is required.")
    @Min(1)
    @Max(120)
    @Column(name = "age", nullable = false)
    private Integer age;

    @NotNull(message = "Please select a train.")
    @Column(name = "train_number", nullable = false)
    private Integer trainNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @Column(name = "train_name", nullable = false, length = 120)
    private String trainName;

    @NotBlank(message = "Please select a class.")
    @Column(name = "class_type", nullable = false, length = 10)
    private String classType;

    @NotNull(message = "Journey date is required.")
    @FutureOrPresent(message = "Journey date cannot be in the past.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "journey_date", nullable = false)
    private LocalDate journeyDate;

    @Column(name = "source_station", nullable = false, length = 100)
    private String sourceStation;

    @Column(name = "destination_station", nullable = false, length = 100)
    private String destinationStation;

    @Column(name = "seat_number", length = 20)
    private String seatNumber;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "CONFIRMED";

    // A reservation may belong to a registered user.
    // Nullable to support guest bookings if needed.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Reservation() {
    }

    public Long getId() { return id; }

    public String getPnr() { return pnr; }
    public void setPnr(String pnr) { this.pnr = pnr; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Integer getTrainNumber() { return trainNumber; }
    public void setTrainNumber(Integer trainNumber) {
        this.trainNumber = trainNumber;
    }

    public Train getTrain() { return train; }
    public void setTrain(Train train) { this.train = train; }

    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getClassType() { return classType; }
    public void setClassType(String classType) {
        this.classType = classType;
    }

    public LocalDate getJourneyDate() { return journeyDate; }
    public void setJourneyDate(LocalDate journeyDate) {
        this.journeyDate = journeyDate;
    }

    public String getSourceStation() { return sourceStation; }
    public void setSourceStation(String sourceStation) {
        this.sourceStation = sourceStation;
    }

    public String getDestinationStation() { return destinationStation; }
    public void setDestinationStation(String destinationStation) {
        this.destinationStation = destinationStation;
    }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}

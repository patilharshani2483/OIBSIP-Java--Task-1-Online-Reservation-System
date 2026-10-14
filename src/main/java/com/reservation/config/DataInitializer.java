package com.reservation.config;

import com.reservation.entity.Train;
import com.reservation.entity.User;
import com.reservation.repository.TrainRepository;
import com.reservation.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(
            TrainRepository trainRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // Create default admin user if not present
            if (!userRepository.findByUsername("admin").isPresent()) {
                User user = new User();
                user.setUsername("admin");
                user.setPassword(passwordEncoder.encode("Admin@123"));
                user.setRole("ADMIN");

                userRepository.save(user);
            }

            // Add trains if train numbers do not exist
            addTrainIfMissing(trainRepository, 12951,
                    "Mumbai Rajdhani Express",
                    "Mumbai Central", "New Delhi");

            addTrainIfMissing(trainRepository, 12925,
                    "Paschim Express",
                    "Mumbai Central", "Amritsar");

            addTrainIfMissing(trainRepository, 19033,
                    "Gujarat Queen",
                    "Valsad", "Ahmedabad");

            addTrainIfMissing(trainRepository, 22953,
                    "Gujarat Express",
                    "Mumbai Central", "Ahmedabad");
        };
    }

    private void addTrainIfMissing(
            TrainRepository repository,
            int number,
            String name,
            String source,
            String destination) {

        // Check by train number, not by primary key ID
        if (!repository.findByTrainNumber(number).isPresent()) {

            Train train = new Train();

            train.setTrainNumber(number);
            train.setTrainName(name);

            // Set both station and route fields
            train.setSource(source);
            train.setDestination(destination);

            train.setSourceStation(source);
            train.setDestinationStation(destination);

            repository.save(train);
        }
    }
}

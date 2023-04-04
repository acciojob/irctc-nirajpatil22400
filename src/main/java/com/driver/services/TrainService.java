package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        Train train = new Train();
        //converting all station into route in string
        StringBuilder route = new StringBuilder();
        for(Station station: trainEntryDto.getStationRoute()){
            route.append(station.toString());
            route.append(",");
        }
        String finalRoute = route.toString().substring(0,route.length());
        //now setting train attributes
        train.setRoute(finalRoute);
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        train = trainRepository.save(train);

        return train.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        return 0;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
        Train train = trainRepository.findById(trainId).get();

        //finding train if passing through given route or not

        boolean isTrainPassed=false;
        String route = train.getRoute();
        String [] routes=route.split(",");
        // now checking for available route
        for(String currRoute : routes){
            if(currRoute.equals(station.toString())){
                isTrainPassed=true;
                break;
            }
        }

        if(!isTrainPassed)
            throw new Exception("Train is not passing from this station");

        //now finding no of people who bording train from given station
        // finding passengers who book tickets from given station
        // and increase count;
        List<Ticket> ticketList= train.getBookedTickets();
        int countOfPassengers = 0;
        for(Ticket ticketBooked : ticketList){
            String startingStation = ticketBooked.getFromStation().toString();
            if(startingStation.equals(station.toString()))
                countOfPassengers++;
        }


        return countOfPassengers;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId) throws Exception{

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        Train train = trainRepository.findById(trainId).get();
        // from train get list of all tickets
        // from tickets get list of passengers
        // from passengers finding oldest one
        List<Ticket> ticketList = train.getBookedTickets();

        // finding list of passengers from tickets
        int maxAge = 0;
        for(Ticket ticket : ticketList){
            List<Passenger> passengerList = ticket.getPassengersList();
            for(Passenger passenger : passengerList){
                int passengerAge=passenger.getAge();
                maxAge=Math.max(maxAge,passengerAge);
            }
        }
        if(maxAge == 0) throw new Exception("no person travelling");
        return maxAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.
        List<Integer> trainListBetweenGivenTime = new ArrayList<>();

        List<Train> trainList=trainRepository.findAll();
        for(Train train : trainList){
            //finding route of perticular train
            String[] route=train.getRoute().split(",");

            int noOfStations =route.length-1 ;

            for(String currStation : route ){
                if(currStation.equals(station.toString())){
                    LocalTime trainDepartureTime = train.getDepartureTime();
                    //assuming train destination time
                    // LocalTime trainDestinationTime = LocalTime.parse("23:59:59");
                    LocalTime trainDestinationTime=trainDepartureTime.plusHours(noOfStations);
                    // comparing start time and end time
                    if(startTime.isAfter(trainDepartureTime) && endTime.isBefore(trainDestinationTime)){
                        trainListBetweenGivenTime.add(train.getTrainId());
                    }
                }
            }

        }
        return trainListBetweenGivenTime;
    }

}

package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
        //And the end return the ticketId that has come from db

        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
        int noOfBookedTickets = train.getBookedTickets().size();
        int totalSeatsAvaible = train.getNoOfSeats();
        int noOfAvailableSeats= totalSeatsAvaible-noOfBookedTickets;
        if(bookTicketEntryDto.getNoOfSeats() > noOfAvailableSeats)
            throw new Exception("Less tickets are available");

        //now checking for requested stations
        // finding all routes of train
        String route = train.getRoute();
        String[] routes = route.split(",");
        //finding requested station present in route or not
        //if present also note their distance
        // to calculate fair
        boolean fromStationPresent = false;
        boolean toStationPresent = false;
        int noOfStartingStation = -1 ;
        int noOFEndStation = -1 ;
        String startingStation = bookTicketEntryDto.getFromStation().toString();
        String endStation =bookTicketEntryDto.getToStation().toString();
        // now checking for presence of station
        for(int i=0 ; i< routes.length ; i++){
            String currStation = routes[i];
            if(currStation.equals(startingStation)){
                fromStationPresent=true;
                noOfStartingStation=i;
            }
            if(currStation.equals(endStation)){
                toStationPresent=true;
                noOFEndStation=i;
            }
        }
        // validating presence of starting and ending station
        // also check they are present in correct order or not
        if(!fromStationPresent || !toStationPresent || noOfStartingStation > noOFEndStation ){
            throw new Exception("Invalid stations");
        }
        //if train passes through correct and available location
        //calculating fair of train
        int totalFair = (noOFEndStation - noOfStartingStation) * 300 ;




        //Setting all attributes of train
        Ticket ticket = new Ticket();
        //setting tickets attributes
        List<Passenger> passengerList= new ArrayList<>();
        passengerList.add(passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get());
        for(int passengerId : bookTicketEntryDto.getPassengerIds()){
            passengerList.add(passengerRepository.findById(passengerId).get());
        }
        ticket.setPassengersList(passengerList);
        ticket.setTrain(train);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTotalFare(totalFair);

        ticket = ticketRepository.save(ticket);
        int ticketId=ticket.getTicketId();
        //setting train attributes
        train.getBookedTickets().add(ticketRepository.findById(ticketId).get());
        trainRepository.save(train);
        //setting passenger attributes
        // because of ManytoMany mapping
        Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        passenger.getBookedTickets().add(ticketRepository.findById(ticketId).get());
        passengerRepository.save(passenger);
        return ticketId;
    }
}
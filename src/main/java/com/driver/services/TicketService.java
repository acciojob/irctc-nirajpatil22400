package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;

    @Autowired
    BookTicketEntryDto bookTicketEntryDto;


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
        int avilable = train.getNoOfSeats() - train.getBookedTickets().size();

        Ticket ticket = null;
        if (avilable >= bookTicketEntryDto.getNoOfSeats()){
            if(bookTicketEntryDto.getFromStation().toString().indexOf(train.getRoute()) > -1){
                ticket.setTrain(train);
                ticket.setFromStation(bookTicketEntryDto.getFromStation());
                ticket.setToStation(bookTicketEntryDto.getToStation());
                List<Passenger> passengers = new ArrayList<>();
                for (int ids : bookTicketEntryDto.getPassengerIds()){
                    Passenger passenger = passengerRepository.findById(ids).get();
                    passengers.add(passenger);
                }
                ticket.setPassengersList(passengers);

                int distance = FindDidtance(bookTicketEntryDto.getFromStation(), bookTicketEntryDto.getToStation());
                int fare = (distance-1)*300;

                ticket.setTotalFare(fare);

                Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
                ticketRepository.save(ticket);
                List<Ticket> tickets = passenger.getBookedTickets();
                tickets.add(ticket);
                passenger.setBookedTickets(tickets);
                trainRepository.save(train);
            }
            else{
                throw new Exception("Invalid stations");
            }
        }
        else{
            throw new Exception("Less tickets are available");
        }
        return ticket == null ? null : ticket.getTicketId();
    }

    public int FindDidtance(Station fromStation, Station toStation){
        EnumSet<Station> set1 = EnumSet.allOf(Station.class);
        int distance = 0;
        int i = 0;
        for (Station station : set1){
            if(station == fromStation || station == toStation){
                distance += i;
            }
            i++;
        }

        return distance;
    }
}
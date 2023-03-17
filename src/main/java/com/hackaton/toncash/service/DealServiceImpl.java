package com.hackaton.toncash.service;

import com.hackaton.toncash.dto.*;
import com.hackaton.toncash.exception.DealNotFoundException;
import com.hackaton.toncash.exception.OrderNotFoundException;
import com.hackaton.toncash.exception.UserNotFoundException;
import com.hackaton.toncash.model.*;
import com.hackaton.toncash.repo.OrderRepo;
import com.hackaton.toncash.repo.PersonRepo;
import com.hackaton.toncash.tgbot.TonBotService;
import com.hackaton.toncash.tgbot.TonCashBot;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.ws.rs.BadRequestException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.hackaton.toncash.service.CommonMethods.mapToPersonDTO;

@Service
@AllArgsConstructor
public class DealServiceImpl implements DealService {
    private final OrderRepo orderRepository;
    private final PersonService personService;
    private final PersonRepo personRepository;
    private final ModelMapper modelMapper;
    private final MongoTemplate mongoTemplate;
    private final TonCashBot bot;


    @Override
    public PersonDealDTO createDeal(DealDTO dealDTO, Long clientId) {
        Order order = orderRepository.findById(dealDTO.getOrderId()).orElseThrow(() -> new OrderNotFoundException(dealDTO.getOrderId()));
        Deal deal = madeDeal(order, dealDTO, clientId);
        Person clientPerson = personRepository.findById(clientId).orElseThrow(() -> new UserNotFoundException(clientId));
        Person orderOwnerPerson = personRepository.findById(order.getOwnerId()).orElseThrow(() -> new UserNotFoundException(order.getOwnerId()));

        clientPerson.getCurrentDeals().add(deal);
        orderOwnerPerson.getCurrentDeals().add(deal);
        order.getDeals().add(deal);

        dealRequest(orderOwnerPerson, clientPerson, order, deal);

        orderRepository.save(order);
        personRepository.save(clientPerson);
        personRepository.save(orderOwnerPerson);
        PersonDTO personDTO = personService.getPerson(clientPerson.getId());
        return new PersonDealDTO(personDTO, modelMapper.map(deal, DealDTO.class));
    }


    private Deal madeDeal(Order order, DealDTO dealDTO, Long clientId) {
        Deal deal = Deal.builder()
                .id(new ObjectId().toString())
                .localDateTime(LocalDateTime.now())
                .dealStatus(DealStatus.CURRENT)
                .addressContract(dealDTO.getAddressContract())
                .addressBuyer(dealDTO.getAddressBuyer())
                .contractDeployed(dealDTO.getContractDeployed())
                .amount(dealDTO.getAmount())
                .orderId(dealDTO.getOrderId())
                .build();
        if (order.getOrderType().equals(OrderType.BUY)) {
            deal.setBuyerId(order.getOwnerId());
            deal.setSellerId(clientId);

        } else {
            deal.setSellerId(order.getOwnerId());
            deal.setBuyerId(clientId);
        }
        if (dealDTO.getAmount() > order.getAmount()) {
            throw new BadRequestException("Your amount greater than amount of order");
        }
        return deal;
    }

    private void dealRequest(Person orderOwnerPerson, Person clientPerson, Order order, Deal deal) {
        String clientUsername = clientPerson.getUsername();
        String orderTypeForClient = "BUY";
        if (order.getOrderType().equals(OrderType.BUY)) {
            orderTypeForClient = "SELL";
        }

        String messageOrderOwner = "You have an offer from @" + clientUsername + " by your order " + order.getOrderType() + " " + order.getAmount() + "TON";
        String messageDealOwner = "You create deal and send an offer to owner @" + orderOwnerPerson.getUsername() + " of order with " + orderTypeForClient + " " + deal.getAmount() + "TON";

        TonBotService.sendOfferDealNotification(bot, Long.toString(orderOwnerPerson.getId()), messageOrderOwner, deal.getId(), clientPerson.getId());
        TonBotService.sendNotification(bot, Long.toString(clientPerson.getId()), messageDealOwner);
    }

    @Override
    public PersonDealDTO acceptDeal(String dealId) {
        Order order = orderRepository.findByDealsId(dealId);
        Deal deal = findDealInList(dealId, order.getDeals());
        Person dealOwner = getDealOwner(dealId);
        Person orderOwner = personRepository.findById(order.getOwnerId()).orElseThrow(() -> new UserNotFoundException(order.getOwnerId()));

        manageDeal(order, deal, orderOwner, dealOwner, true);

        return new PersonDealDTO(mapToPersonDTO(dealOwner), modelMapper.map(deal, DealDTO.class));

    }

    @Override
    public void denyDeal(String dealId) {
        Order order = orderRepository.findByDealsId(dealId);
        Person dealOwner = getDealOwner(dealId);

        Deal deal = findDealInList(dealId, order.getDeals());
        Person orderOwner = personRepository.findById(order.getOwnerId()).orElseThrow(() -> new UserNotFoundException(order.getOwnerId()));

        manageDeal(order, deal, orderOwner, dealOwner, false);
    }

    @Override
    public void deleteDeal(String dealId) {
        Person dealOwnerPerson = getDealOwner(dealId);
        Deal deal = findDealInList(dealId, dealOwnerPerson.getCurrentDeals());
        Order order = orderRepository.findById(deal.getOrderId()).orElseThrow(() -> new OrderNotFoundException(deal.getOrderId()));
        Person orderOwner = personRepository.findById(order.getOwnerId()).orElseThrow(() -> new UserNotFoundException(order.getOwnerId()));
        String orderTypeForClient = "BUY";
        if (order.getOrderType().equals(OrderType.BUY)) {
            orderTypeForClient = "SELL";
        }
        long dealOwner = getDealOwner(deal, order);
        String messageDealOwner = "You delete the deal with " + orderTypeForClient + " " + deal.getAmount() + "TON";
        String messageOrderOwner = "";

        if (deal.getDealStatus().equals(DealStatus.DENIED) || deal.getDealStatus().equals(DealStatus.CANCEL)) {
            dealOwnerPerson.getCurrentDeals().remove(deal);
        }
        if (deal.getDealStatus().equals(DealStatus.CURRENT)) {
            dealOwnerPerson.getCurrentDeals().remove(deal);
            order.getDeals().remove(deal);
            messageOrderOwner = "User deny his deal with " + orderTypeForClient + " " + deal.getAmount() + "TON";
        }
        if (deal.getDealStatus().equals(DealStatus.FINISH) || deal.getDealStatus().equals(DealStatus.PENDING)) {
            throw new BadRequestException("You can't delete this Deal");
        }

        TonBotService.sendNotification(bot, Long.toString(dealOwnerPerson.getId()), messageDealOwner);
        if (!messageOrderOwner.isEmpty()) {
            TonBotService.sendNotification(bot, Long.toString(order.getOwnerId()), messageOrderOwner);
        }

    }

    @Override
    public PersonDealDTO updateDeal(String dealId, DealDTO dealDTO) {
        Person dealOwnerPerson = getDealOwner(dealId);
        Deal ownerDeal = findDealInList(dealId, dealOwnerPerson.getCurrentDeals());
        dealOwnerPerson.getCurrentDeals().remove(ownerDeal);

        Order order = orderRepository.findById(ownerDeal.getOrderId()).orElseThrow(() -> new OrderNotFoundException(ownerDeal.getOrderId()));
        Person orderOwnerPerson = personRepository.findById(order.getOwnerId()).orElseThrow(() -> new UserNotFoundException(order.getOwnerId()));

        Deal orderDeal = findDealInList(dealId, order.getDeals());
        order.getDeals().remove(orderDeal);


        Deal updateDeal = CommonMethods.updateEntity(ownerDeal, dealDTO);

        dealOwnerPerson.getCurrentDeals().add(updateDeal);
        order.getDeals().add(updateDeal);
        String messageDealOwner = "You changed the deal with @" + orderOwnerPerson.getUsername() + " by the order " + order.getOrderType() + " " + order.getAmount() + "TON";
        String messageOrderOwner = "The deal with @" + dealOwnerPerson.getUsername() + " by the order " + order.getOrderType() + " " + order.getAmount() + "TON was changed by client";

        if (dealDTO.getDealStatus().equals(DealStatus.CANCEL)) {
            order.getDeals().remove(orderDeal);
            messageDealOwner = "The deal with @" + orderOwnerPerson.getUsername() + " by the order " + order.getOrderType() + " " + order.getAmount() + "TON was canceled";
            messageOrderOwner = "The deal with @" + dealOwnerPerson.getUsername() + " by the order " + order.getOrderType() + " " + order.getAmount() + "TON was canceled";
        }
        if (dealDTO.getDealStatus().equals(DealStatus.FINISH)) {
            order.getDeals().add(updateDeal);
            dealOwnerPerson.getCommunity().add(orderOwnerPerson);
            orderOwnerPerson.getCommunity().add(dealOwnerPerson);
            messageDealOwner = "The deal with @" + orderOwnerPerson.getUsername() + " by the order " + order.getOrderType() + " " + order.getAmount() + "TON was finished";
            messageOrderOwner = "The deal with @" + dealOwnerPerson.getUsername() + " by the order " + order.getOrderType() + " " + order.getAmount() + "TON was finished";


        }

        TonBotService.sendNotification(bot, Long.toString(dealOwnerPerson.getId()), messageDealOwner);
        TonBotService.sendNotification(bot, Long.toString(orderOwnerPerson.getId()), messageOrderOwner);

        orderRepository.save(order);
        personRepository.save(dealOwnerPerson);
        personRepository.save(orderOwnerPerson);
        return new PersonDealDTO(mapToPersonDTO(dealOwnerPerson), modelMapper.map(ownerDeal, DealDTO.class));
    }

    private void manageDeal(Order order, Deal orderDeal, Person orderOwner, Person dealOwner, boolean dealAction) {
        long ownerId = order.getOwnerId();
        String clientUsername = dealOwner.getUsername();
        Deal clientDeal = findDealInList(orderDeal.getId(), dealOwner.getCurrentDeals());
        String orderTypeForClient = "BUY";
        if (order.getOrderType().equals(OrderType.BUY)) {
            orderTypeForClient = "SELL";
        }

        if (dealAction) {
            int amount = order.getAmount();
            order.setAmount(amount - orderDeal.getAmount());
            orderDeal.setDealStatus(DealStatus.PENDING);
            clientDeal.setDealStatus(DealStatus.PENDING);
            orderOwner.getCurrentDeals().add(clientDeal);
            String messageOwner = "You accept the offer from the client @" + clientUsername + " by the order " + order.getOrderType() + " " + order.getAmount() + "TON";
            String messageClient = "Your offer has been confirmed @" + orderOwner.getUsername() + " by the deal " + orderTypeForClient + " with " + orderDeal.getAmount() + "TON";
            TonBotService.sendNotification(bot, Long.toString(dealOwner.getId()), messageClient);
            TonBotService.sendNotification(bot, Long.toString(ownerId), messageOwner);
        } else {
            order.getDeals().remove(orderDeal);
            orderDeal.setDealStatus(DealStatus.DENIED);
            clientDeal.setDealStatus(DealStatus.DENIED);
            orderOwner.getCurrentDeals().remove(clientDeal);

            String messageOwner = "You deny the offer from the client @" + clientUsername + " by the order " + order.getOrderType() + " " + order.getAmount() + "TON";
            String messageClient = "Your offer has been denied by @" + orderOwner.getUsername() + " the deal " + orderTypeForClient + " with " + orderDeal.getAmount() + "TON";
            TonBotService.sendNotification(bot, Long.toString(dealOwner.getId()), messageClient);
            TonBotService.sendNotification(bot, Long.toString(ownerId), messageOwner);

        }

        orderRepository.save(order);
        personRepository.save(dealOwner);
    }

    @Override
    public PersonDealDTO getOrderDeal(String dealId) {
        Person person = getDealOwner(dealId);
        Deal deal = findDealInList(dealId, person.getCurrentDeals());
        return new PersonDealDTO(mapToPersonDTO(person), modelMapper.map(deal, DealDTO.class));
    }

    @Override
    public Iterable<PersonDealDTO> getOrderDeals(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        List<Deal> deals = order.getDeals();

        Set<Long> personsIds = deals.stream()
                .map(deal ->
                        getDealOwner(deal, order)).collect(Collectors.toSet());

        Map<Long, Person> persons = StreamSupport.stream(personRepository.findAllById(personsIds).spliterator(), false)
                .collect(Collectors.toMap(Person::getId, Function.identity()));

        return deals.stream()
                .map(deal -> new PersonDealDTO(
                        mapToPersonDTO(persons.get(getDealOwner(deal, order))),
                        modelMapper.map(deal, DealDTO.class)))
                .collect(Collectors.toList());
    }

    private Person getDealOwner(String dealId) {
        List<Person> personByCurrentDealsId = personRepository.findPersonByCurrentDealsId(dealId);
        if (personByCurrentDealsId.size() == 0) {
            throw new DealNotFoundException(dealId);
        }
        Deal deal = findDealInList(dealId, personByCurrentDealsId.get(0).getCurrentDeals());
        String orderId = deal.getOrderId();
        if (personByCurrentDealsId.get(0).getCurrentOrders().contains(orderId)) {
            return personByCurrentDealsId.get(1);
        }
        return personByCurrentDealsId.get(0);
    }

    private PersonOrderDTO mapOrderDTOtoPersonOrderDTO(OrderDTO orderDTO) {
        PersonDTO personDTO = personService.getPerson(orderDTO.getOwnerId());
        return new PersonOrderDTO(personDTO, orderDTO);
    }

    private Deal findDealInList(String dealId, List<Deal> deals) {
        Deal fakeDeal = new Deal();
        fakeDeal.setId(dealId);
        int index = deals.indexOf(fakeDeal);
        if (index < 0) {
            throw new BadRequestException("Deal with id - " + dealId + " doesn't exist");
        }
        return deals.get(index);
    }

    private PersonDealDTO mapDealDTOtoPersonDealDTO(Deal deal, Order order) {
        long dealOwner = getDealOwner(deal, order);
        PersonDTO personDTO = personService.getPerson(dealOwner);
        return new PersonDealDTO(personDTO, modelMapper.map(deal, DealDTO.class));
    }

    private long getDealOwner(Deal deal, Order order) {
        long ownerId = order.getOwnerId();
        if (ownerId != deal.getBuyerId()) {
            ownerId = deal.getSellerId();
        }
        return ownerId;
    }

}

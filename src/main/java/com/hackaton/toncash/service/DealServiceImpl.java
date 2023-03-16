package com.hackaton.toncash.service;

import com.hackaton.toncash.dto.*;
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
        clientPerson.getCurrentDeals().add(deal);
        dealRequest(order, clientPerson, deal);

        order.getDeals().add(deal);
        orderRepository.save(order);
        personRepository.save(clientPerson);
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
                .contractDeployed(dealDTO.isContractDeployed())
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

    private void dealRequest(Order order, Person clientPerson, Deal deal) {
        String clientUsername = clientPerson.getUsername();
        String orderTypeForClient = "BUY";
        if (order.getOrderType().equals(OrderType.BUY)) {
            orderTypeForClient = "SELL";
        }

        String messageOrderOwner = "You have an offer from @" + clientUsername + " by your order " + order.getOrderType() + " " + order.getAmount() + "TON";
        String messageDealOwner = "You create deal and send an offer to owner of order with " + orderTypeForClient + " " + deal.getAmount() + "TON";

        TonBotService.sendOfferDealNotification(bot, Long.toString(order.getOwnerId()), messageOrderOwner, deal.getId(), clientPerson.getId());
        TonBotService.sendNotification(bot, Long.toString(clientPerson.getId()), messageDealOwner);
    }

    @Override
    public PersonDealDTO acceptDeal(String dealId) {
        Order order = orderRepository.findByDealsId(dealId);
        Person client = personRepository.findPersonByCurrentDealsId(dealId);
        Deal deal = findDealInList(dealId, order.getDeals());

        manageDeal(order, deal, client, true);

        return new PersonDealDTO(mapToPersonDTO(client), modelMapper.map(deal, DealDTO.class));

    }

    @Override
    public void denyDeal(String dealId) {
        Order order = orderRepository.findByDealsId(dealId);
        Person client = personRepository.findPersonByCurrentDealsId(dealId);

        Deal deal = findDealInList(dealId, order.getDeals());

        manageDeal(order, deal, client, false);
    }

    @Override
    public void deleteDeal(String dealId) {
        Person dealOwnerPerson = personRepository.findPersonByCurrentDealsId(dealId);
        Deal deal = findDealInList(dealId, dealOwnerPerson.getCurrentDeals());
        Order order = orderRepository.findById(deal.getOrderId()).orElseThrow(() -> new OrderNotFoundException(deal.getOrderId()));

        long dealOwner = getDealOwner(deal, order);
        if (deal.getDealStatus().equals(DealStatus.DENIED)) {
            dealOwnerPerson.getCurrentDeals().remove(deal);
        }
        if (deal.getDealStatus().equals(DealStatus.CURRENT)) {
            dealOwnerPerson.getCurrentDeals().remove(deal);
            order.getDeals().remove(deal);
        }
        if (deal.getDealStatus().equals(DealStatus.FINISH) || deal.getDealStatus().equals(DealStatus.PENDING)) {
            throw new BadRequestException("You can't delete this Deal");
        }

    }

    private void manageDeal(Order order, Deal orderDeal, Person client, boolean dealAction) {
        long ownerId = order.getOwnerId();
        String clientUsername = client.getUsername();
        Deal clientDeal = findDealInList(orderDeal.getId(), client.getCurrentDeals());

        String orderTypeForClient = "BUY";
        if (order.getOrderType().equals(OrderType.BUY)) {
            orderTypeForClient = "SELL";
        }

        if (dealAction) {
            float amount = order.getAmount();
            order.setAmount(amount - orderDeal.getAmount());
            orderDeal.setDealStatus(DealStatus.PENDING);
            clientDeal.setDealStatus(DealStatus.PENDING);
            String messageOwner = "You accept the offer from the client @" + clientUsername + " by the order " + order.getOrderType() + " " + order.getAmount() + "TON";
            String messageClient = "Your offer has been confirmed @" + clientUsername + " by the deal " + orderTypeForClient + " with " + orderDeal.getAmount() + "TON";
            TonBotService.sendNotification(bot, Long.toString(client.getId()), messageClient);
            TonBotService.sendNotification(bot, Long.toString(ownerId), messageOwner);
        } else {
            order.getDeals().remove(orderDeal);
            orderDeal.setDealStatus(DealStatus.DENIED);
            clientDeal.setDealStatus(DealStatus.DENIED);

            String messageOwner = "You deny the offer from the client @" + clientUsername + " by the order " + order.getOrderType() + " " + order.getAmount() + "TON";
            String messageClient = "Your offer has been denied by the deal " + orderTypeForClient + " with " + orderDeal.getAmount() + "TON";
            TonBotService.sendNotification(bot, Long.toString(client.getId()), messageClient);
            TonBotService.sendNotification(bot, Long.toString(ownerId), messageOwner);

        }

        orderRepository.save(order);
        personRepository.save(client);
    }
//    public void acceptDeal(long clientId, String dealId, Long ownerOrderId, Long chatId, Integer messageId) {
//        System.out.println("accept deal");
//        Order order = orderRepository.findByDealsId(dealId);
//        Deal deal = findDealInOrder(dealId, order);
//        float amount = order.getAmount();
////        order.setAmount(amount-deal.getAmount());
//        deal.setDealStatus(DealStatus.PENDING);
////
//        String clientUsername = personService.getPerson(clientId).getUsername();
//        String messageOwner = "You accept a client @" + clientUsername + " by the order " + order.getOrderType() + " " + order.getAmount() + "TON";
//        String orderTypeForClient = "BUY";
//        if (order.getOrderType().equals(OrderType.BUY)) {
//            orderTypeForClient = "SELL";
//        }
//        String messageClient = "Your offer has been confirmed @" + clientUsername + " by the deal " + orderTypeForClient + " with " + deal.getAmount() + "TON";
//        TonBotService.sendNotification(bot, Long.toString(clientId), messageClient);
//        TonBotService.sendEditMassage(bot, chatId, messageId, messageOwner, order.getId(), dealId, clientId);
//    }


    @Override
    public PersonDealDTO getOrderDeal(String dealId) {
        Person person = personRepository.findPersonByCurrentDealsId(dealId);
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

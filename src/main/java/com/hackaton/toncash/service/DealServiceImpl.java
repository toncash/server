package com.hackaton.toncash.service;

import com.hackaton.toncash.dto.*;
import com.hackaton.toncash.exception.OrderNotFoundException;
import com.hackaton.toncash.exception.UserNotFoundException;
import com.hackaton.toncash.model.*;
import com.hackaton.toncash.repo.DealRepository;
import com.hackaton.toncash.repo.OrderRepository;
import com.hackaton.toncash.repo.PersonRepository;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.hackaton.toncash.service.CommonMethods.mapToPersonDTO;

@Service
@AllArgsConstructor
public class DealServiceImpl implements DealService {

    private final DealRepository dealRepository;
    private final OrderRepository orderRepository;
    private final PersonRepository personRepository;
    private final ModelMapper modelMapper;
    private final MongoTemplate mongoTemplate;
//    private final TonCashBot bot;


    @Override
    public PersonDealDTO createDeal(DealDTO dealDTO, Long clientId) {
        Order order = orderRepository.findById(dealDTO.getOrderId()).orElseThrow(() -> new OrderNotFoundException(dealDTO.getOrderId()));
        Deal deal = madeDeal(order, dealDTO, clientId);
        Person dealOwnerPerson = personRepository.findById(clientId).orElseThrow(() -> new UserNotFoundException(clientId));

        //FIXME maybe add deal to Order after accept
        dealOwnerPerson.getCurrentDeals().add(deal.getId());
        order.getDeals().add(deal.getId());
        orderRepository.save(order);
        personRepository.save(dealOwnerPerson);

        dealRequest(dealOwnerPerson, order, deal);

        return new PersonDealDTO(mapToPersonDTO(dealOwnerPerson), modelMapper.map(deal, DealDTO.class));


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
            throw new IllegalArgumentException("Your amount greater than amount of order");
        }
//        return deal;
        return dealRepository.save(deal);
    }

    private void dealRequest(Person dealOwnerPerson, Order order, Deal deal) {
        Person orderOwnerPerson = personRepository.findById(order.getOwnerId()).orElseThrow(() -> new UserNotFoundException(order.getOwnerId()));

        String orderTypeForClient = "BUY";
        if (order.getOrderType().equals(OrderType.BUY)) {
            orderTypeForClient = "SELL";
        }

        String messageOrderOwner = "You have an offer from @" + dealOwnerPerson.getUsername() + " by your order " + order.getOrderType() + " " + order.getAmount() + "TON";
        String messageDealOwner = "You create deal and send an offer to @" + orderOwnerPerson.getUsername() + " with " + orderTypeForClient + " " + deal.getAmount() + "TON";

//        TonBotService.sendOfferDealNotification(bot, Long.toString(dealOwnerPerson.getTelegramId()), messageDealOwner, dealId, clientId);
//        TonBotService.sendOfferDealNotification(bot, Long.toString(orderOwnerPerson.getTelegramId()), messageOrderOwner, dealId, clientId);
    }

    @Override
    public PersonDealDTO acceptDeal(String dealId) {
        Deal deal = dealRepository.findById(dealId).orElseThrow(() -> new OrderNotFoundException(dealId));
        Order order = orderRepository.findById(deal.getOrderId()).orElseThrow(() -> new OrderNotFoundException(deal.getOrderId()));
        manageDeal(order, deal, true);
        PersonDTO ownerDeal = getPersonDTOByOrder(order, deal);
        return new PersonDealDTO(ownerDeal, modelMapper.map(deal, DealDTO.class));

    }

    @Override
    public void denyDeal(String dealId) {
        Deal deal = dealRepository.findById(dealId).orElseThrow(() -> new OrderNotFoundException(dealId));
        Order order = orderRepository.findById(deal.getOrderId()).orElseThrow(() -> new OrderNotFoundException(deal.getOrderId()));

        manageDeal(order, deal, false);
    }

    @Override
    public void deleteDeal(String dealId) {
        Deal deal = dealRepository.findById(dealId).orElseThrow(() -> new OrderNotFoundException(dealId));
        Order order = orderRepository.findById(deal.getOrderId()).orElseThrow(() -> new OrderNotFoundException(deal.getOrderId()));
        long dealOwner = getDealOwner(deal, order);
        Person dealOwnerPerson = personRepository.findById(dealOwner).orElseThrow(() -> new UserNotFoundException(dealOwner));
        if (deal.getDealStatus().equals(DealStatus.DENIED)) {
            dealOwnerPerson.getCurrentDeals().remove(deal.getId());
        }
        if (deal.getDealStatus().equals(DealStatus.CURRENT)) {
            dealOwnerPerson.getCurrentDeals().remove(deal.getId());
            order.getDeals().remove(deal.getId());
        }
        if (deal.getDealStatus().equals(DealStatus.FINISH) || deal.getDealStatus().equals(DealStatus.PENDING)) {
            throw new BadRequestException("You can't delete this Deal");
        }
        dealRepository.delete(deal);
    }

    private void manageDeal(Order order, Deal deal, boolean dealAction) {
        long ownerId = order.getOwnerId();
        long clientId;
        if (ownerId == deal.getBuyerId()) {
            clientId = deal.getSellerId();
        } else {
            clientId = deal.getBuyerId();
        }
        Person client = personRepository.findById(clientId).orElseThrow(() -> new UserNotFoundException(clientId));
        Person owner = personRepository.findById(ownerId).orElseThrow(() -> new UserNotFoundException(clientId));

        long ownerTelegramId = owner.getTelegramId();
        String clientUsername = client.getUsername();

        String orderTypeForClient = "BUY";
        if (order.getOrderType().equals(OrderType.BUY)) {
            orderTypeForClient = "SELL";
        }

        if (dealAction) {
            float amount = order.getAmount();
            order.setAmount(amount - deal.getAmount());
            deal.setDealStatus(DealStatus.PENDING);
            owner.getCurrentDeals().add(deal.getId());
//            clientDeal.setDealStatus(DealStatus.PENDING);
            String messageOwner = "You accept the client @" + clientUsername + " by the order " + order.getOrderType() + " " + order.getAmount() + "TON";
            String messageClient = "Your offer has been confirmed @" + clientUsername + " by the deal " + orderTypeForClient + " with " + deal.getAmount() + "TON";
//            TonBotService.sendNotification(bot, Long.toString(client.getTelegramId()), messageClient);
//            TonBotService.sendNotification(bot, Long.toString(ownerTelegramId), messageOwner);
        } else {
            order.getDeals().remove(deal.getId());
            deal.setDealStatus(DealStatus.DENIED);
//            clientDeal.setDealStatus(DealStatus.DENIED);

            String messageOwner = "You deny the client @" + clientUsername + " by the order " + order.getOrderType() + " " + order.getAmount() + "TON";
            String messageClient = "Your offer has been denied by the deal " + orderTypeForClient + " with " + deal.getAmount() + "TON";
//            TonBotService.sendNotification(bot, Long.toString(client.getTelegramId()), messageClient);
//            TonBotService.sendNotification(bot, Long.toString(ownerTelegramId), messageOwner);

        }
        dealRepository.save(deal);
        orderRepository.save(order);
        personRepository.save(owner);
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
    public PersonDealDTO getDeal(String dealId) {
        Deal deal = dealRepository.findById(dealId).orElseThrow(() -> new OrderNotFoundException(dealId));
//        Order order = orderRepository.findById(deal.getOrderId()).orElseThrow(() -> new OrderNotFoundException(deal.getOrderId()));
        Person person = personRepository.findByCurrentDealsContains(dealId).orElseThrow(()-> new NotFoundException("deal not found"));
        return new PersonDealDTO( mapToPersonDTO(person), modelMapper.map(deal, DealDTO.class));
    }


    @Override
    public Iterable<PersonDealDTO> getOrderDeals(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        List<Deal> deals = StreamSupport.stream(dealRepository.findAllById(order.getDeals()).spliterator(), false).collect(Collectors.toList());

        Set<Long> personsIds = deals.stream().map(deal -> getDealOwner(deal, order)).collect(Collectors.toSet());

        Map<Long, Person> persons = StreamSupport.stream(personRepository.findAllById(personsIds).spliterator(), false)
                .collect(Collectors.toMap(Person::getId, Function.identity()));

        return deals.stream()
                .map(deal -> new PersonDealDTO(
                        mapToPersonDTO(persons.get(getDealOwner(deal, order))),
                        modelMapper.map(deal, DealDTO.class)))
                .collect(Collectors.toList());
    }


    private PersonDTO getPersonDTOByOrder(Order order, Deal deal) {
        long dealOwner = getDealOwner(deal, order);
        Person person = personRepository.findById(dealOwner).orElseThrow(() -> new UserNotFoundException(order.getOwnerId()));
        return mapToPersonDTO(person);
    }

    private long getDealOwner(Deal deal, Order order) {
        long ownerId = order.getOwnerId();
        if (ownerId != deal.getBuyerId()) {
            ownerId = deal.getSellerId();
        }
        return ownerId;
    }

}

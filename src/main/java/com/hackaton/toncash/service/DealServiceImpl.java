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

import javax.ws.rs.BadRequestException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DealServiceImpl implements DealService {

    private final DealRepository dealRepository;
    private final OrderRepository orderRepository;


//    private final PersonService personService;
    private final PersonRepository personRepository;
    private final ModelMapper modelMapper;
    private final MongoTemplate mongoTemplate;
//    private final TonCashBot bot;


    @Override
    public PersonDealDTO createDeal(DealDTO dealDTO, Long clientId) {
        Order order = orderRepository.findById(dealDTO.getOrderId()).orElseThrow(() -> new OrderNotFoundException(dealDTO.getOrderId()));
        Deal deal = madeDeal(order, dealDTO, clientId);
        Person person = personRepository.findById(clientId).orElseThrow(() -> new UserNotFoundException(clientId));
        person.getCurrentDeals().add(deal.getId());
        dealRequest(order, clientId, deal.getId());

        order.getDeals().add(deal.getId());
        orderRepository.save(order);
        personRepository.save(person);

        Iterable<Deal> deals = dealRepository.findAllById(person.getCurrentDeals());
        PersonDTO personDTO = CommonMethods.mapToPersonDTO(person, deals,modelMapper);
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
            throw new IllegalArgumentException("Your amount greater than amount of order");
        }
//        return deal;
        return dealRepository.save(deal);
    }

    private void dealRequest(Order order, long clientId, String dealId) {
        Person clientPerson = personRepository.findById(clientId).orElseThrow(() -> new UserNotFoundException(clientId));

        String message = "You have an offer from @" + clientPerson.getUsername() + " by your order " + order.getOrderType() + " " + order.getAmount() + "TON";
        String clientUsername = clientPerson.getUsername();
//        TonBotService.sendOfferDealNotification(bot, Long.toString(order.getOwnerId()), message, dealId, clientId);
    }

    @Override
    public PersonDealDTO acceptDeal(String orderId, String dealId) {
//        Order order = orderRepository.findByDealsId(dealId);
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
//        Deal deal = findDealInList(dealId, order.getDeals());
        Deal deal = dealRepository.findById(dealId).orElseThrow(() -> new OrderNotFoundException(dealId));
        manageDeal(order, deal, true);

        return mapDealDTOtoPersonDealDTO(modelMapper.map(deal, DealDTO.class), order);

    }

    @Override
    public void denyDeal(String orderId, String dealId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        Deal deal = dealRepository.findById(dealId).orElseThrow(()-> new OrderNotFoundException(dealId));

        manageDeal(order, deal, false);
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
//        Deal clientDeal = findDealInList(deal.getId(), client.getCurrentDeals());

        String orderTypeForClient = "BUY";
        if (order.getOrderType().equals(OrderType.BUY)) {
            orderTypeForClient = "SELL";
        }

        if (dealAction) {
            float amount = order.getAmount();
            order.setAmount(amount - deal.getAmount());
            deal.setDealStatus(DealStatus.PENDING);

//            clientDeal.setDealStatus(DealStatus.PENDING);
            String messageOwner = "You accept the client @" + clientUsername + " by the order " + order.getOrderType() + " " + order.getAmount() + "TON";
            String messageClient = "Your offer has been confirmed @" + clientUsername + " by the deal " + orderTypeForClient + " with " + deal.getAmount() + "TON";
//            TonBotService.sendNotification(bot, Long.toString(client.getTelegramId()), messageClient);
//            TonBotService.sendNotification(bot, Long.toString(ownerTelegramId), messageOwner);
        } else {
            order.getDeals().remove(deal);
            deal.setDealStatus(DealStatus.DENIED);
//            clientDeal.setDealStatus(DealStatus.DENIED);

            String messageOwner = "You deny the client @" + clientUsername + " by the order " + order.getOrderType() + " " + order.getAmount() + "TON";
            String messageClient = "Your offer has been denied by the deal " + orderTypeForClient + " with " + deal.getAmount() + "TON";
//            TonBotService.sendNotification(bot, Long.toString(client.getTelegramId()), messageClient);
//            TonBotService.sendNotification(bot, Long.toString(ownerTelegramId), messageOwner);

        }
        dealRepository.save(deal);
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
    public PersonDealDTO getOrderDeal(String orderId, String dealId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        Deal deal = dealRepository.findById(dealId).orElseThrow(()-> new OrderNotFoundException(dealId));

        return mapDealDTOtoPersonDealDTO(modelMapper.map(deal, DealDTO.class), order);
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

    @Override
    public Iterable<PersonDealDTO> getOrderDeals(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        return order.getDeals().stream()
                .map(deal -> modelMapper.map(deal, DealDTO.class))
                .map(d -> mapDealDTOtoPersonDealDTO(d, order))
                .collect(Collectors.toList());
    }



    private PersonDealDTO mapDealDTOtoPersonDealDTO(DealDTO dealDTO, Order order) {
        long dealOwner = getDealOwner(dealDTO, order);
        Person person = personRepository.findById(dealOwner).orElseThrow(() -> new UserNotFoundException(dealOwner));

        Iterable<Deal> deals = dealRepository.findAllById(person.getCurrentDeals());
        PersonDTO personDTO = CommonMethods.mapToPersonDTO(person, deals,modelMapper);
        return new PersonDealDTO(personDTO, dealDTO);
    }

    private long getDealOwner(DealDTO dealDTO, Order order) {
        long ownerId = order.getOwnerId();
        if (ownerId != dealDTO.getBuyerId()) {
            ownerId = dealDTO.getSellerId();
        }
        return ownerId;
    }

}

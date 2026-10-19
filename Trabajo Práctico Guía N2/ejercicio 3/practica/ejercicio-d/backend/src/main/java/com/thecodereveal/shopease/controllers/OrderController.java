package com.thecodereveal.shopease.controllers;

import com.thecodereveal.shopease.auth.dto.OrderResponse;
import com.thecodereveal.shopease.dto.OrderDetails;
import com.thecodereveal.shopease.dto.OrderRequest;
import com.thecodereveal.shopease.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/order")
@CrossOrigin
public class OrderController {


    @Autowired
    OrderService orderService;



    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest, Principal principal) throws Exception {
        OrderResponse orderResponse = orderService.createOrder(orderRequest,principal);
            //return new ResponseEntity<>(order, HttpStatus.CREATED);

        return new ResponseEntity<>(orderResponse,HttpStatus.OK);
    }

    @PostMapping("/update-payment")
    public ResponseEntity<?> updatePaymentStatus(@RequestBody Map<String,String> request){
        Map<String,String> response = orderService.updateStatus(request.get("paymentIntent"),request.get("status"));
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancelOrder(@PathVariable UUID id,Principal principal){
        orderService.cancelOrder(id,principal);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<List<OrderDetails>> getOrderByUser(Principal principal) {
        List<OrderDetails> orders = orderService.getOrdersByUser(principal.getName());
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

}

package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.dto.PaymentDTO;
import com.tranhuy105.musicserviceapi.service.SubscriptionService;
import com.tranhuy105.musicserviceapi.utils.Util;
import com.tranhuy105.musicserviceapi.utils.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @PostMapping("/purchase")
    public ResponseEntity<PaymentDTO.VNPayResponse> purchaseSubscription(@RequestParam Short planId, HttpServletRequest request, Authentication authentication) {
        String bankCode = request.getParameter("bankCode");
        String ipAddress = VNPayUtil.getIpAddress(request);
        Long userId = Util.extractUserIdFromAuthentication(authentication);

        PaymentDTO.VNPayResponse paymentResponse = subscriptionService.purchaseSubscription(userId, planId, bankCode, ipAddress);
        return new ResponseEntity<>(paymentResponse, HttpStatus.OK);
    }

    @GetMapping("/vnpay-callback")
    public ResponseEntity<String> vnPayCallback(HttpServletRequest request) {
        String responseCode = request.getParameter("vnp_ResponseCode");
        String txnRef = request.getParameter("vnp_TxnRef");
        String orderInfo = request.getParameter("vnp_OrderInfo");

        subscriptionService.handleSubscriptionCallback(responseCode, txnRef, orderInfo);
        return ResponseEntity.ok("Payment callback processed.");
    }
}

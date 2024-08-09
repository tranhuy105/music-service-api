package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.dto.PaymentDTO;
import com.tranhuy105.musicserviceapi.exception.ObjectNotFoundException;
import com.tranhuy105.musicserviceapi.model.SubscriptionPlan;
import com.tranhuy105.musicserviceapi.repository.api.SubscriptionRepository;
import com.tranhuy105.musicserviceapi.utils.VNPayUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentService paymentService;

    public PaymentDTO.VNPayResponse purchaseSubscription(Long userId, Short planId, String bankCode, String ipAddress) {
        long amount = VNPayUtil.convertUSDtoVND(getSubscriptionPlanPrice(planId));
        String orderInfo = paymentService.getRandomPaymentId()+":"+planId.toString();
        String txnRef = userId + "-" + System.currentTimeMillis();

        return paymentService.createVnPayPayment(amount, orderInfo, txnRef, bankCode, ipAddress);
    }

    public void handleSubscriptionCallback(String responseCode, String txnRef, String orderInfo) {
        if (paymentService.verifyPayment(responseCode)) {
            String[] txnParts = txnRef.split("-");
            Long userId = Long.valueOf(txnParts[0]);
            Short planId = Short.valueOf(orderInfo.split(":")[1]);
            subscriptionRepository.createSubscriptionPlan(userId, planId);
        }
    }

    private BigDecimal getSubscriptionPlanPrice(Short planId) {
        SubscriptionPlan plan = subscriptionRepository.findPlanById(planId).orElseThrow(
                () -> new ObjectNotFoundException("plan", planId.toString())
        );
        return plan.getPrice();
    }
}

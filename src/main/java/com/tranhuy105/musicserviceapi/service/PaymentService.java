package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.config.payment.VNPAYConfig;
import com.tranhuy105.musicserviceapi.dto.PaymentDTO;
import com.tranhuy105.musicserviceapi.utils.VNPayUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final VNPAYConfig vnPayConfig;

    public PaymentDTO.VNPayResponse createVnPayPayment(long amount, String orderInfo, String txnRef, String bankCode, String ipAddress) {
        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount * 100));
        vnpParamsMap.put("vnp_OrderInfo", orderInfo);
        vnpParamsMap.put("vnp_TxnRef", txnRef);
        vnpParamsMap.put("vnp_IpAddr", ipAddress);
        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }

        // build query url
        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;

        return PaymentDTO.VNPayResponse.builder()
                .code("ok")
                .message("success")
                .paymentUrl(paymentUrl).build();
    }

    public String getRandomPaymentId() {
        return UUID.randomUUID().toString();
    }

    public boolean verifyPayment(String responseCode) {
        return "00".equals(responseCode);
    }
}

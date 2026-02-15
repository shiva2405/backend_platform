package com.example.payment.config;

import com.example.payment.model.PaymentMethod;
import com.example.payment.repository.PaymentMethodRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    
    @Override
    public void run(String... args) {
        logger.info("Initializing demo payment methods...");
        
        // Add demo cards for user ID 2 (demo user)
        if (paymentMethodRepository.findByUserId(2L).isEmpty()) {
            // VISA card
            PaymentMethod visa = new PaymentMethod();
            visa.setUserId(2L);
            visa.setType(PaymentMethod.PaymentType.CREDIT_CARD);
            visa.setCardHolderName("Demo User");
            visa.setCardNumber("**** **** **** 4242");
            visa.setCardBrand("VISA");
            visa.setExpiryMonth("12");
            visa.setExpiryYear("2026");
            visa.setDefault(true);
            paymentMethodRepository.save(visa);
            logger.info("Added demo VISA card for user 2");
            
            // Mastercard
            PaymentMethod mastercard = new PaymentMethod();
            mastercard.setUserId(2L);
            mastercard.setType(PaymentMethod.PaymentType.DEBIT_CARD);
            mastercard.setCardHolderName("Demo User");
            mastercard.setCardNumber("**** **** **** 5555");
            mastercard.setCardBrand("MASTERCARD");
            mastercard.setExpiryMonth("06");
            mastercard.setExpiryYear("2027");
            mastercard.setDefault(false);
            paymentMethodRepository.save(mastercard);
            logger.info("Added demo Mastercard for user 2");
            
            // COD option
            PaymentMethod cod = new PaymentMethod();
            cod.setUserId(2L);
            cod.setType(PaymentMethod.PaymentType.COD);
            cod.setPhoneNumber("+1-555-0100");
            cod.setDefault(false);
            paymentMethodRepository.save(cod);
            logger.info("Enabled COD for user 2");
        }
        
        // Add demo card for user ID 1 (admin)
        if (paymentMethodRepository.findByUserId(1L).isEmpty()) {
            PaymentMethod adminCard = new PaymentMethod();
            adminCard.setUserId(1L);
            adminCard.setType(PaymentMethod.PaymentType.CREDIT_CARD);
            adminCard.setCardHolderName("Admin User");
            adminCard.setCardNumber("**** **** **** 1111");
            adminCard.setCardBrand("VISA");
            adminCard.setExpiryMonth("03");
            adminCard.setExpiryYear("2028");
            adminCard.setDefault(true);
            paymentMethodRepository.save(adminCard);
            logger.info("Added demo card for admin user 1");
        }
        
        logger.info("Demo payment methods initialized successfully");
    }
}

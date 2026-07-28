//package com.cts.logichain360.seed;
//
//import com.cts.logichain360.dto.request.UserRegistrationRequest;
//import com.cts.logichain360.dto.request.WarehouseAssignmentRequest;
//import com.cts.logichain360.dto.response.UserRegistrationResponse;
//import com.cts.logichain360.entity.*;
//import com.cts.logichain360.enums.*;
//import com.cts.logichain360.repository.*;
//import com.cts.logichain360.service.UserService;
//import com.cts.logichain360.service.WarehouseManagerService;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class DataSeeder implements CommandLineRunner {
//
//    private final UserService userService;
//    private final WarehouseManagerService warehouseManagerService;
//
//    private final WarehouseRepository warehouseRepo;
//    private final VendorRepository vendorRepo;
//    private final ProductRepository productRepo;
//    private final ProductWarehouseRepository productWarehouseRepo;
//    private final CustomerRepository customerRepo;
//    private final DriverRepository driverRepo;
//    private final OrderRepository orderRepo;
//    private final WarehouseManagerRepository warehouseManagerRepo;
//    private final PaymentRepository paymentRepo;
//    private final InvoiceRepository invoiceRepo;
//    private final PODRepository podRepo;
//    private final ReturnRequestRepository returnRequestRepo;
//    private final NotificationRepository notificationRepo;
//
//    private static final double TAX_PERCENT = 18.0;
//    private static final double DELIVERY_PERCENT = 5.0;
//
//    @Override
//    @Transactional
//    public void run(String... args) {
//        if (vendorRepo.count() > 0) {
//            log.info("Seed data already present — skipping DataSeeder.");
//            return;
//        }
//        log.info("Seeding LogiChain360 demo data (v2 — full lifecycle coverage)...");
//
//        //1. Warehouses
//        Warehouse blr = warehouseRepo.save(Warehouse.builder()
//                .warehouseCode("WH-BLR-01").location("Bangalore").capacity(1000).build());
//        Warehouse del = warehouseRepo.save(Warehouse.builder()
//                .warehouseCode("WH-DEL-01").location("Delhi").capacity(800).build());
//
//        //2. Warehouse Managers
//        Long wm1ProfileId = registerAndGetProfileId(UserRegistrationRequest.builder()
//                .name("Ravi Kumar").phone("9876500001").password("password123")
//                .role(UserRole.WAREHOUSE_MANAGER)
//                .employeeCode("EMP-WM-001").designation("Warehouse Manager")
//                .build());
//        warehouseManagerService.assignWarehouse(wm1ProfileId,
//                WarehouseAssignmentRequest.builder().warehouseId(blr.getId()).build());
//
//        Long wm2ProfileId = registerAndGetProfileId(UserRegistrationRequest.builder()
//                .name("Anita Sharma").phone("9876500002").password("password123")
//                .role(UserRole.WAREHOUSE_MANAGER)
//                .employeeCode("EMP-WM-002").designation("Warehouse Manager")
//                .build());
//        warehouseManagerService.assignWarehouse(wm2ProfileId,
//                WarehouseAssignmentRequest.builder().warehouseId(del.getId()).build());
//
//        //3. Vendors
//        Long vendor1Id = registerAndGetProfileId(UserRegistrationRequest.builder()
//                .name("Suresh Nair").phone("9876500011").password("password123")
//                .role(UserRole.VENDOR)
//                .companyName("TechSupplies Pvt Ltd").gstNumber("29ABCDE1234F1Z5")
//                .email("vendor1@techsupplies.com").businessAddress("MG Road, Bangalore")
//                .contactPerson("Suresh Nair").paymentTerms("NET30")
//                .build());
//
//        Long vendor2Id = registerAndGetProfileId(UserRegistrationRequest.builder()
//                .name("Priya Desai").phone("9876500012").password("password123")
//                .role(UserRole.VENDOR)
//                .companyName("HomeEssentials Pvt Ltd").gstNumber("07XYZAB5678G1Z2")
//                .email("vendor2@homeessentials.com").businessAddress("CP, Delhi")
//                .contactPerson("Priya Desai").paymentTerms("NET15")
//                .build());
//
//        Vendor vendor1 = vendorRepo.getReferenceById(vendor1Id);
//        Vendor vendor2 = vendorRepo.getReferenceById(vendor2Id);
//
//        //4. Products
//        Product headphones = productRepo.save(Product.builder()
//                .productName("Sony WH-1000XM5 Headphones").productPrice(29990.00)
//                .productDescription("Wireless noise-cancelling over-ear headphones")
//                .vendor(vendor1).build());
//
//        Product mouse = productRepo.save(Product.builder()
//                .productName("Logitech MX Master 3S").productPrice(8995.00)
//                .productDescription("Wireless performance mouse")
//                .vendor(vendor1).build());
//
//        Product blender = productRepo.save(Product.builder()
//                .productName("Philips Mixer Grinder").productPrice(3499.00)
//                .productDescription("750W mixer grinder, 3 jars")
//                .vendor(vendor2).build());
//
//        Product cooktop = productRepo.save(Product.builder()
//                .productName("Prestige Induction Cooktop").productPrice(2499.00)
//                .productDescription("2000W induction cooktop with touch controls")
//                .vendor(vendor2).build());
//
//        //5. ProductWarehouse
//        ProductWarehouse pwHeadphones = productWarehouseRepo.save(ProductWarehouse.builder()
//                .product(headphones).warehouse(blr)
//                .stock(196).maxStock(500).rolPercent(40.0).build());
//
//        ProductWarehouse pwMouse = productWarehouseRepo.save(ProductWarehouse.builder()
//                .product(mouse).warehouse(blr)
//                .stock(147).maxStock(400).rolPercent(30.0).build());
//
//        ProductWarehouse pwBlender = productWarehouseRepo.save(ProductWarehouse.builder()
//                .product(blender).warehouse(del)
//                .stock(51).maxStock(300).rolPercent(40.0).build());
//
//        ProductWarehouse pwCooktop = productWarehouseRepo.save(ProductWarehouse.builder()
//                .product(cooktop).warehouse(del)
//                .stock(79).maxStock(200).rolPercent(25.0).build());
//
//        //6. Customers
//        Long cust1Id = registerAndGetProfileId(UserRegistrationRequest.builder()
//                .name("Arjun Mehta").phone("9876500021").password("password123")
//                .role(UserRole.CUSTOMER)
//                .companyName("Mehta Retail").gstNumber("27PQRSX9988H1Z9")
//                .email("arjun@mehtaretail.com")
//                .shippingAddress("12 MG Road, Bangalore 560001")
//                .billingAddress("12 MG Road, Bangalore 560001")
//                .creditLimit(100000.0).paymentTerms("NET30")
//                .build());
//
//        Long cust2Id = registerAndGetProfileId(UserRegistrationRequest.builder()
//                .name("Neha Kapoor").phone("9876500022").password("password123")
//                .role(UserRole.CUSTOMER)
//                .companyName("Kapoor Traders").gstNumber("07LMNOX4433J1Z7")
//                .email("neha@kapoortraders.com")
//                .shippingAddress("45 CP, Delhi 110001")
//                .billingAddress("45 CP, Delhi 110001")
//                .creditLimit(75000.0).paymentTerms("NET15")
//                .build());
//
//        Long cust3Id = registerAndGetProfileId(UserRegistrationRequest.builder()
//                .name("Rohan Verma").phone("9876500023").password("password123")
//                .role(UserRole.CUSTOMER)
//                .companyName("Verma Enterprises").gstNumber("09ABCDE1234F1Z6")
//                .email("rohan@vermaenterprises.com")
//                .shippingAddress("221B Sector 18, Delhi 110045")
//                .billingAddress("221B Sector 18, Delhi 110045")
//                .creditLimit(50000.0).paymentTerms("NET30")
//                .build());
//
//        Customer customer1 = customerRepo.getReferenceById(cust1Id);
//        Customer customer2 = customerRepo.getReferenceById(cust2Id);
//        Customer customer3 = customerRepo.getReferenceById(cust3Id);
//
//        // 7. Drivers
//        Long driver1Id = registerAndGetProfileId(UserRegistrationRequest.builder()
//                .name("Manoj Yadav").phone("9876500031").password("password123")
//                .role(UserRole.DRIVER)
//                .licenseNumber("DL-KA-2020-0001").licenseExpiry(LocalDate.now().plusYears(2))
//                .location("Bangalore")
//                .build());
//
//        Long driver2Id = registerAndGetProfileId(UserRegistrationRequest.builder()
//                .name("Rakesh Singh").phone("9876500032").password("password123")
//                .role(UserRole.DRIVER)
//                .licenseNumber("DL-DL-2021-0002").licenseExpiry(LocalDate.now().plusYears(3))
//                .location("Delhi")
//                .build());
//
//        Long driver3Id = registerAndGetProfileId(UserRegistrationRequest.builder()
//                .name("Vikram Rao").phone("9876500033").password("password123")
//                .role(UserRole.DRIVER)
//                .licenseNumber("DL-KA-2022-0003").licenseExpiry(LocalDate.now().plusYears(2))
//                .location("Bangalore")
//                .build());
//
//        Long driver4Id = registerAndGetProfileId(UserRegistrationRequest.builder()
//                .name("Sunil Reddy").phone("9876500034").password("password123")
//                .role(UserRole.DRIVER)
//                .licenseNumber("DL-DL-2022-0004").licenseExpiry(LocalDate.now().plusYears(3))
//                .location("Delhi")
//                .build());
//
//        Driver driver1 = driverRepo.getReferenceById(driver1Id); // Manoj
//        Driver driver2 = driverRepo.getReferenceById(driver2Id); // Rakesh
//        Driver driver3 = driverRepo.getReferenceById(driver3Id); // Vikram
//        Driver driver4 = driverRepo.getReferenceById(driver4Id); // Sunil
//
//        //8. Orders
//        LocalDateTime now = LocalDateTime.now();
//
//        // --- Order A: headphones x1, DELIVERED (fully paid + POD) — Arjun ---
//        double aTotal = headphones.getProductPrice() * 1;
//        Orders orderA = orderRepo.save(Orders.builder()
//                .customer(customer1).product(headphones).productWarehouse(pwHeadphones)
//                .driver(driver1) // Manoj delivered this one
//                .productNameSnapshot(headphones.getProductName())
//                .unitPriceSnapshot(headphones.getProductPrice())
//                .totalAmount(aTotal)
//                .amountPaid(36887.70) // advance 17694.10 + final 19193.60
//                .status(OrderStatus.DELIVERED)
//                .placedAt(now.minusDays(10))
//                .shippingAddress(customer1.getShippingAddress())
//                .quantity(1)
//                .build());
//        seedPayment(orderA, PaymentType.ADVANCE, 17694.10, now.minusDays(10).plusHours(1));
//        seedPayment(orderA, PaymentType.FINAL, 19193.60, now.minusDays(8));
//        seedInvoice(orderA, customer1, vendor1, headphones, 1, 29990.00, aTotal);
//        podRepo.save(POD.builder()
//                .order(orderA).photoFilename("seed-placeholder-pod-1.jpg")
//                .driverId(driver1.getId()).driverName("Manoj Yadav")
//                .uploadedAt(now.minusDays(8)).build());
//        driver1.setAvailable(true);
//        driverRepo.save(driver1);
//
//        double bTotal = headphones.getProductPrice() * 2;
//        Orders orderB = orderRepo.save(Orders.builder()
//                .customer(customer2).product(headphones).productWarehouse(pwHeadphones)
//                .driver(driver3)
//                .productNameSnapshot(headphones.getProductName())
//                .unitPriceSnapshot(headphones.getProductPrice())
//                .totalAmount(bTotal)
//                .amountPaid(35388.20)
//                .status(OrderStatus.IN_TRANSIT)
//                .placedAt(now.minusDays(3))
//                .shippingAddress(customer2.getShippingAddress())
//                .quantity(2)
//                .build());
//        seedPayment(orderB, PaymentType.ADVANCE, 35388.20, now.minusDays(3).plusHours(1));
//        seedInvoice(orderB, customer2, vendor1, headphones, 2, 29990.00, bTotal);
//        driver3.setAvailable(false);
//        driverRepo.save(driver3);
//
//        double cTotal = headphones.getProductPrice() * 1;
//        Orders orderC = orderRepo.save(Orders.builder()
//                .customer(customer3).product(headphones).productWarehouse(pwHeadphones)
//                .productNameSnapshot(headphones.getProductName())
//                .unitPriceSnapshot(headphones.getProductPrice())
//                .totalAmount(cTotal)
//                .amountPaid(17694.10)
//                .status(OrderStatus.CONFIRMED)
//                .placedAt(now.minusDays(1))
//                .shippingAddress(customer3.getShippingAddress())
//                .quantity(1)
//                .build());
//        seedPayment(orderC, PaymentType.ADVANCE, 17694.10, now.minusDays(1).plusHours(1));
//        seedInvoice(orderC, customer3, vendor1, headphones, 1, 29990.00, cTotal);
//        double dTotal = mouse.getProductPrice() * 3;
//        orderRepo.save(Orders.builder()
//                .customer(customer1).product(mouse).productWarehouse(pwMouse)
//                .productNameSnapshot(mouse.getProductName())
//                .unitPriceSnapshot(mouse.getProductPrice())
//                .totalAmount(dTotal)
//                .amountPaid(0.0)
//                .status(OrderStatus.PENDING)
//                .placedAt(now.minusHours(6))
//                .shippingAddress(customer1.getShippingAddress())
//                .quantity(3)
//                .build());
//
//        double eTotal = blender.getProductPrice() * 1;
//        Orders orderE = orderRepo.save(Orders.builder()
//                .customer(customer2).product(blender).productWarehouse(pwBlender)
//                .productNameSnapshot(blender.getProductName())
//                .unitPriceSnapshot(blender.getProductPrice())
//                .totalAmount(eTotal)
//                .amountPaid(0.0) // reset to 0 after the refund below
//                .status(OrderStatus.CANCELLED)
//                .placedAt(now.minusDays(5))
//                .shippingAddress(customer2.getShippingAddress())
//                .quantity(1)
//                .build());
//        seedPayment(orderE, PaymentType.ADVANCE, 2064.41, now.minusDays(5).plusHours(1));
//        seedPayment(orderE, PaymentType.REFUND, -2064.41, now.minusDays(4));
//        Invoice eInvoice = seedInvoice(orderE, customer2, vendor2, blender, 1, 3499.00, eTotal);
//        eInvoice.setStatus(InvoiceStatus.VOID);
//        eInvoice.setVoidedAt(now.minusDays(4));
//        invoiceRepo.save(eInvoice);
//
//        double fTotal = cooktop.getProductPrice() * 1;
//        Orders orderF = orderRepo.save(Orders.builder()
//                .customer(customer3).product(cooktop).productWarehouse(pwCooktop)
//                .driver(driver4) // Sunil delivered, then picked up the return too
//                .productNameSnapshot(cooktop.getProductName())
//                .unitPriceSnapshot(cooktop.getProductPrice())
//                .totalAmount(fTotal)
//                .amountPaid(124.95) // 3073.77 paid, then 2948.82 refunded,net = delivery fee only
//                .status(OrderStatus.RETURNED)
//                .placedAt(now.minusDays(15))
//                .shippingAddress(customer3.getShippingAddress())
//                .quantity(1)
//                .build());
//        seedPayment(orderF, PaymentType.ADVANCE, 1474.41, now.minusDays(15).plusHours(1));
//        seedPayment(orderF, PaymentType.FINAL, 1599.36, now.minusDays(13));
//        seedInvoice(orderF, customer3, vendor2, cooktop, 1, 2499.00, fTotal);
//        podRepo.save(POD.builder()
//                .order(orderF).photoFilename("seed-placeholder-pod-2.jpg")
//                .driverId(driver4.getId()).driverName("Sunil Reddy")
//                .uploadedAt(now.minusDays(13)).build());
//        seedPayment(orderF, PaymentType.REFUND, -2948.82, now.minusDays(2));
//        returnRequestRepo.save(ReturnRequest.builder()
//                .order(orderF)
//                .returnQuantity(1)
//                .reason(ReturnReason.DAMAGED)
//                .notes("Cooktop arrived with a cracked display panel.")
//                .status(ReturnStatus.RESTOCKED)
//                .requestedAt(now.minusDays(3))
//                .resolvedAt(now.minusDays(3).plusHours(2))
//                .resolvedByManagerId(wm2ProfileId)
//                .pickupDriverId(driver4.getId())
//                .restockedAt(now.minusDays(2))
//                .refundAmount(2948.82)
//                .handlingFeeAmount(0.0)
//                .build());
//        driver4.setAvailable(true);
//        driverRepo.save(driver4);
//
//        double gTotal = blender.getProductPrice() * 1;
//        Orders orderG = orderRepo.save(Orders.builder()
//                .customer(customer1).product(blender).productWarehouse(pwBlender)
//                .driver(driver2)
//                .productNameSnapshot(blender.getProductName())
//                .unitPriceSnapshot(blender.getProductPrice())
//                .totalAmount(gTotal)
//                .amountPaid(2064.41)
//                .status(OrderStatus.ASSIGNED)
//                .placedAt(now.minusHours(20))
//                .shippingAddress(customer1.getShippingAddress())
//                .quantity(1)
//                .build());
//        seedPayment(orderG, PaymentType.ADVANCE, 2064.41, now.minusHours(19));
//        seedInvoice(orderG, customer1, vendor2, blender, 1, 3499.00, gTotal);
//        driver2.setAvailable(false);
//        driverRepo.save(driver2);
//
//        // 9.Notifications
//        User wm2User = warehouseManagerRepo.findById(wm2ProfileId).orElseThrow().getUser();
//
//        notificationRepo.save(Notification.builder()
//                .recipient(wm2User)
//                .type(NotificationType.ROL_BREACH)
//                .message("Product 'Philips Mixer Grinder' at warehouse WH-DEL-01 is at 17.0% of capalocation (below 40.0% reorder threshold). Consider restocking.")
//                .createdAt(now.minusDays(5))
//                .read(false)
//                .relatedEntityId(pwBlender.getId())
//                .relatedEntityType("ProductWarehouse")
//                .build());
//
//        notificationRepo.save(Notification.builder()
//                .recipient(vendor2.getUser())
//                .type(NotificationType.ROL_BREACH)
//                .message("Product 'Philips Mixer Grinder' at warehouse WH-DEL-01 is at 17.0% of capalocation (below 40.0% reorder threshold). Consider restocking.")
//                .createdAt(now.minusDays(5))
//                .read(false)
//                .relatedEntityId(pwBlender.getId())
//                .relatedEntityType("ProductWarehouse")
//                .build());
//
//        notificationRepo.save(Notification.builder()
//                .recipient(driver3.getUser())
//                .type(NotificationType.ORDER_ASSIGNED)
//                .message("You've been assigned to deliver order #" + orderB.getId() + " to " + orderB.getShippingAddress() + ".")
//                .createdAt(now.minusDays(3))
//                .read(true)
//                .relatedEntityId(orderB.getId())
//                .relatedEntityType("Order")
//                .build());
//
//        notificationRepo.save(Notification.builder()
//                .recipient(customer3.getUser())
//                .type(NotificationType.ORDER_STATUS_CHANGED)
//                .message("Your return for order #" + orderF.getId() + " has been processed and restocked.")
//                .createdAt(now.minusDays(2))
//                .read(false)
//                .relatedEntityId(orderF.getId())
//                .relatedEntityType("Order")
//                .build());
//
//        log.info("DataSeeder v2 complete: 2 warehouses, 2 warehouse managers, 2 vendors, " +
//                "4 products, 4 stock entries, 3 customers, 4 drivers, 7 orders " +
//                "(1 PENDING, 1 CONFIRMED, 1 ASSIGNED, 1 IN_TRANSIT, 1 DELIVERED, 1 CANCELLED, 1 RETURNED), " +
//                "1 completed return (DAMAGED, no restock, full refund minus delivery fee), 4 notifications.");
//    }
//
//    //Helper fns
//
//    private Long registerAndGetProfileId(UserRegistrationRequest req) {
//        UserRegistrationResponse resp = userService.registerUser(req).getBody();
//        return resp.getRoleProfileId();
//    }
//
//    private void seedPayment(Orders order, PaymentType type, double amount, LocalDateTime paidAt) {
//        paymentRepo.save(Payment.builder()
//                .order(order)
//                .amount(amount)
//                .type(type)
//                .status(PaymentStatus.SUCCESS)
//                .paidAt(paidAt)
//                .build());
//    }
//
//    private Invoice seedInvoice(Orders order, Customer customer, Vendor vendor, Product product,
//                                int quantity, double unitPrice, double subtotal) {
//        double taxAmount = round2(subtotal * TAX_PERCENT / 100.0);
//        double deliveryFee = round2(subtotal * DELIVERY_PERCENT / 100.0);
//        double total = round2(subtotal + taxAmount + deliveryFee);
//
//        Invoice invoice = Invoice.builder()
//                .invoiceNumber(java.util.UUID.randomUUID().toString())
//                .order(order)
//                .customerId(customer.getId())
//                .customerName(customer.getUser().getName())
//                .customerCompany(customer.getCompanyName())
//                .vendorId(vendor.getId())
//                .vendorCompanyName(vendor.getCompanyName())
//                .productName(product.getProductName())
//                .quantity(quantity)
//                .unitPrice(unitPrice)
//                .subtotal(subtotal)
//                .taxPercent(TAX_PERCENT)
//                .taxAmount(taxAmount)
//                .deliveryFee(deliveryFee)
//                .totalAmount(total)
//                .shippingAddress(order.getShippingAddress())
//                .issuedAt(order.getPlacedAt().plusHours(1))
//                .status(InvoiceStatus.ACTIVE)
//                .build();
//
//        Invoice saved = invoiceRepo.saveAndFlush(invoice);
//
//        saved.setInvoiceNumber(String.format("INV-%d-%05d", saved.getIssuedAt().getYear(), saved.getId()));
//        return invoiceRepo.saveAndFlush(saved);
//    }
//
//    private double round2(double value) {
//        return Math.round(value * 100.0) / 100.0;
//    }
//}
package com.cts.logichain360.seed;

import com.cts.logichain360.dto.request.UserRegistrationRequest;
import com.cts.logichain360.dto.request.WarehouseAssignmentRequest;
import com.cts.logichain360.dto.response.UserRegistrationResponse;
import com.cts.logichain360.entity.*;
import com.cts.logichain360.enums.UserRole;
import com.cts.logichain360.enums.OrderStatus; 
import com.cts.logichain360.enums.PaymentType;
import com.cts.logichain360.enums.PaymentStatus;
import com.cts.logichain360.repository.*; 
import com.cts.logichain360.service.UserService;
import com.cts.logichain360.service.WarehouseManagerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

//order of seeding: WH, WHM, Vendors, Products, PWH, Customers, Drivers, Orders
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserService userService;
    private final WarehouseManagerService warehouseManagerService;

    private final WarehouseRepository warehouseRepo;
    private final VendorRepository vendorRepo;
    private final ProductRepository productRepo;
    private final ProductWarehouseRepository productWarehouseRepo;
    private final CustomerRepository customerRepo;
    private final DriverRepository driverRepo;
    private final OrderRepository ordersRepo;
    private final WarehouseManagerRepository warehouseManagerRepo;
    private final PaymentRepository paymentRepo;

    @Override
    @Transactional
    public void run(String... args) {
        if (vendorRepo.count() > 0) {
            log.info("Seed data already present — skipping DataSeeder.");
            return;
        }
        log.info("Seeding LogiChain360 demo data...");

        //1. Warehouses
        Warehouse blr = warehouseRepo.save(Warehouse.builder()
                .warehouseCode("WH-BLR-01").location("Bangalore").capacity(1000).build());
        Warehouse del = warehouseRepo.save(Warehouse.builder()
                .warehouseCode("WH-DEL-01").location("Delhi").capacity(800).build());

        //2.Warehouse Managers-  register n assign to a wh
        Long wm1ProfileId = registerAndGetProfileId(UserRegistrationRequest.builder()
                .name("Ravi Kumar").phone("9876500001").password("password123")
                .role(UserRole.WAREHOUSE_MANAGER)
                .employeeCode("EMP-WM-001").designation("Warehouse Manager")
                .build());
        warehouseManagerService.assignWarehouse(wm1ProfileId,
                WarehouseAssignmentRequest.builder().warehouseId(blr.getId()).build());

        Long wm2ProfileId = registerAndGetProfileId(UserRegistrationRequest.builder()
                .name("Anita Sharma").phone("9876500002").password("password123")
                .role(UserRole.WAREHOUSE_MANAGER)
                .employeeCode("EMP-WM-002").designation("Warehouse Manager")
                .build());
        warehouseManagerService.assignWarehouse(wm2ProfileId,
                WarehouseAssignmentRequest.builder().warehouseId(del.getId()).build());

        //3.Vendors 
        Long vendor1Id = registerAndGetProfileId(UserRegistrationRequest.builder()
                .name("Suresh Nair").phone("9876500011").password("password123")
                .role(UserRole.VENDOR)
                .companyName("TechSupplies Pvt Ltd").gstNumber("29ABCDE1234F1Z5")
                .companyName("TechSuppliesDrivers Pvt Ltd").gstNumber("29ABCDE1234F1Z5")
                .email("vendor1@techsupplies.com").businessAddress("MG Road, Bangalore")
                .contactPerson("Suresh Nair").paymentTerms("NET30")
                .build());

        Long vendor2Id = registerAndGetProfileId(UserRegistrationRequest.builder()
                .name("Priya Desai").phone("9876500012").password("password123")
                .role(UserRole.VENDOR)
                .companyName("HomeEssentials Pvt Ltd").gstNumber("07XYZAB5678G1Z2")
                .email("vendor2@homeessentials.com").businessAddress("CP, Delhi")
                .contactPerson("Priya Desai").paymentTerms("NET15")
                .build());

        Vendor vendor1 = vendorRepo.getReferenceById(vendor1Id);
        Vendor vendor2 = vendorRepo.getReferenceById(vendor2Id);

        //4.Products
        Product headphones = productRepo.save(Product.builder()
                .productName("Sony WH-1000XM5 Headphones").productPrice(29990.00)
                .productDescription("Wireless noise-cancelling over-ear headphones")
                .vendor(vendor1).build());

        Product mouse = productRepo.save(Product.builder()
                .productName("Logitech MX Master 3S").productPrice(8995.00)
                .productDescription("Wireless performance mouse")
                .vendor(vendor1).build());

        Product blender = productRepo.save(Product.builder()
                .productName("Philips Mixer Grinder").productPrice(3499.00)
                .productDescription("750W mixer grinder, 3 jars")
                .vendor(vendor2).build());

        //5. ProductWarehouse:stock launch
        // Healthy stock levels
        ProductWarehouse pwHeadphones = productWarehouseRepo.save(ProductWarehouse.builder()
                .product(headphones).warehouse(blr)
                .stock(200).maxStock(500).rolPercent(40.0).build());

        ProductWarehouse pwMouse = productWarehouseRepo.save(ProductWarehouse.builder()
                .product(mouse).warehouse(blr)
                .stock(150).maxStock(400).rolPercent(30.0).build());

        // Deliberately BELOW ROL to test the reorder-notification trigger
        ProductWarehouse pwBlender = productWarehouseRepo.save(ProductWarehouse.builder()
                .product(blender).warehouse(del)
                .stock(50).maxStock(300).rolPercent(40.0).build());

        //6.Customers
        Long cust1Id = registerAndGetProfileId(UserRegistrationRequest.builder()
                .name("Arjun Mehta").phone("9876500021").password("password123")
                .role(UserRole.CUSTOMER)
                .companyName("Mehta Retail").gstNumber("27PQRSX9988H1Z9")
                .email("arjun@mehtaretail.com")
                .shippingAddress("12 MG Road, Bangalore 560001")
                .billingAddress("12 MG Road, Bangalore 560001")
                .creditLimit(100000.0).paymentTerms("NET30")
                .build());

        Long cust2Id = registerAndGetProfileId(UserRegistrationRequest.builder()
                .name("Neha Kapoor").phone("9876500022").password("password123")
                .role(UserRole.CUSTOMER)
                .companyName("Kapoor Traders").gstNumber("07LMNOX4433J1Z7")
                .email("neha@kapoortraders.com")
                .shippingAddress("45 CP, Delhi 110001")
                .billingAddress("45 CP, Delhi 110001")
                .creditLimit(75000.0).paymentTerms("NET15")
                .build());

        Customer customer1 = customerRepo.getReferenceById(cust1Id);
        Customer customer2 = customerRepo.getReferenceById(cust2Id);

        //7.Drivers
        Long driver1Id = registerAndGetProfileId(UserRegistrationRequest.builder()
                .name("Manoj Yadav").phone("9876500031").password("password123")
                .role(UserRole.DRIVER)
                .licenseNumber("DL-KA-2020-0001").licenseExpiry(LocalDate.now().plusYears(2))
                .build());

        registerAndGetProfileId(UserRegistrationRequest.builder()
                .name("Rakesh Singh").phone("9876500032").password("password123")
                .role(UserRole.DRIVER)
                .licenseNumber("DL-DL-2021-0002").licenseExpiry(LocalDate.now().plusYears(3))
                .build());

        Driver driver1 = driverRepo.getReferenceById(driver1Id);

        //8.Orders
        // Order 1: fully progressed, assigned to driver1, in transit.
        double order1Total = headphones.getProductPrice() * 2;
        double order1Advance = Math.round(order1Total * 0.5 * 100.0) / 100.0;

        Orders order1 = ordersRepo.save(Orders.builder()
                .customer(customer1).product(headphones).productWarehouse(pwHeadphones)
                .driver(driver1)
                .productNameSnapshot(headphones.getProductName())
                .unitPriceSnapshot(headphones.getProductPrice())
                .totalAmount(order1Total)
                .amountPaid(order1Advance)
                .status(OrderStatus.IN_TRANSIT)
                .placedAt(LocalDateTime.now().minusDays(1))
                .shippingAddress(customer1.getShippingAddress())
                .quantity(2)
                .build());

        paymentRepo.save(Payment.builder()
                .order(order1)
                .amount(order1Advance)
                .type(PaymentType.ADVANCE)
                .status(PaymentStatus.SUCCESS)
                .paidAt(LocalDateTime.now().minusDays(1).plusHours(1))
                .build());

        // Order 2: just placed, pending confirmation
        ordersRepo.save(Orders.builder()
                .customer(customer2).product(blender).productWarehouse(pwBlender)
                .productNameSnapshot(blender.getProductName())
                .unitPriceSnapshot(blender.getProductPrice())
                .totalAmount(blender.getProductPrice() * 1)
                .status(OrderStatus.PENDING)
                .placedAt(LocalDateTime.now())
                .shippingAddress(customer2.getShippingAddress())
                .quantity(1)
                .build());

        log.info("DataSeeder complete: 2 warehouses, 2 warehouse managers, 2 vendors, " +
                  "3 products, 3 stock entries, 2 customers, 2 drivers, 2 orders.");
    }

    //Registers a user + role profile, returns the role-profile id (e.g. vendor id, not user id)
    private Long registerAndGetProfileId(UserRegistrationRequest req) {
        UserRegistrationResponse resp = userService.registerUser(req).getBody();
        return resp.getRoleProfileId();
    }
}
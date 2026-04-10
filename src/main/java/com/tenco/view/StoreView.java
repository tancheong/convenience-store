package com.tenco.view;

import com.tenco.dto.Product;
import com.tenco.dto.Sales;
import com.tenco.service.StoreService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class StoreView {
    private final StoreService service = new StoreService();
    private final Scanner scanner = new Scanner(System.in);

    private Integer currentAdminId = null;
    private String currentAdminName = null;

    // 프로그램 메인 루프
    public void start() {
        while (true) {
            try {
                printMenu();
                int choice = readInt("선택: ");

                switch (choice) {
                    case 1:
                        listProduct();
                        break;
                    case 2:
                        findByBarcode();
                        break;
                    case 3:
                        processSale();
                        break;
                    case 4:
                        findTodaySales();
                        break;
                    case 5:
                        login();
                        break;
                    case 6:
                        addProduct();
                        break;
                    case 7:
                        updateProduct();
                        break;
                    case 8:
                        deleteProduct();
                        break;
                    case 9:
                        findLowStock();
                        break;
                    case 10:
                        exit();
                        return;
                    default:
                        System.out.println("1~10 사이의 숫자를 입력하세요.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("입력 오류: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("DB 오류: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("오류: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== 편의점 무인 재고 관리 시스템 ===");
        if (currentAdminId == null) {
            System.out.println("[ 로그아웃 상태 ]");
        } else {
            System.out.println("[ 로그인: " + currentAdminName + " ]");
        }
        System.out.println("──────────────────────");
        System.out.println("1. 상품 목록 조회");
        System.out.println("2. 바코드로 상품 검색");
        System.out.println("3. 판매 처리");
        System.out.println("4. 오늘 매출 조회");
        System.out.println("5. 관리자 로그인");
        System.out.println("6. 상품 등록");
        System.out.println("7. 상품 수정");
        System.out.println("8. 상품 소프트 삭제");
        System.out.println("9. 재고 부족 알림");
        System.out.println("10. 종료");
    }

    // 1. 상품 목록 조회
    private void listProduct() throws SQLException {
        List<Product> products = service.getProductList();
        System.out.println("\n=== 상품 목록 ===");

        if (products.isEmpty()) {
            System.out.println("등록된 상품이 없습니다.");
            return;
        }

        System.out.println("─────────────────────────────────────────────────────");

        for (Product p : products) {
            String status = "";

            if (service.isLowStock(p)) {
                status += " [재고부족]";
            }
            if (service.isNearExpiry(p)) {
                status += " [유통기한임박]";
            }

            System.out.println("상품명: " + p.getName()
                    + " | 바코드: " + p.getBarcode()
                    + " | 재고: " + p.getStock()
                    + " | 가격: " + p.getPrice()
                    + status);
        }
    }

    // 2. 바코드로 상품 검색
    private void findByBarcode() throws SQLException {
        System.out.print("바코드: ");
        String barcode = scanner.nextLine().trim();

        if (barcode.isEmpty()) {
            System.out.println("바코드를 입력해주세요.");
            return;
        }

        Product product = service.findByBarcode(barcode);

        System.out.println("\n=== 검색 결과 ===");

        if (product == null) {
            System.out.println("상품을 찾을 수 없습니다.");
            return;
        }

        String status = "";
        if (service.isLowStock(product)) {
            status += " [재고부족]";
        }
        if (service.isNearExpiry(product)) {
            status += " [유통기한임박]";
        }

        System.out.println("상품명: " + product.getName()
                + " | 바코드: " + product.getBarcode()
                + " | 재고: " + product.getStock()
                + " | 가격: " + product.getPrice()
                + status);
    }

    // 3. 판매 처리
    private void processSale() throws SQLException {
        System.out.print("바코드: ");
        String barcode = scanner.nextLine().trim();

        if (barcode.isEmpty()) {
            System.out.println("바코드를 입력해주세요.");
            return;
        }

        int quantity = readInt("수량: ");

        if (quantity <= 0) {
            System.out.println("수량은 1개 이상이어야 합니다.");
            return;
        }

        String result = service.processSale(barcode, quantity);
        System.out.println(result);
    }

    // 4. 오늘 매출 조회
    private void findTodaySales() throws SQLException {
        List<Sales> salesList = service.getTodaySales();

        System.out.println("\n=== 오늘 매출 조회 ===");

        if (salesList.isEmpty()) {
            System.out.println("오늘 매출 내역이 없습니다.");
            return;
        }

        for (Sales s : salesList) {
            System.out.println("판매ID: " + s.getId()
                    + " | 상품ID: " + s.getProductId()
                    + " | 수량: " + s.getQuantity()
                    + " | 총금액: " + s.getTotalSales()
                    + " | 판매일시: " + s.getSoldAt());
        }
    }

    // 5. 관리자 로그인
    private void login() throws SQLException {
        System.out.print("관리자 ID: ");
        String adminId = scanner.nextLine().trim();

        System.out.print("비밀번호: ");
        String password = scanner.nextLine().trim();

        boolean result = service.login(adminId, password);

        if (result) {
            currentAdminName = service.getCurrentAdminName();
            currentAdminId = 1; // 뷰 표시용 임시값
            System.out.println(currentAdminName + " 관리자님, 환영합니다!");
        } else {
            System.out.println("로그인 실패");
        }
    }

    // 6. 상품 등록
    private void addProduct() throws SQLException {
        System.out.println("\n=== 상품 등록 ===");

        System.out.print("바코드: ");
        String barcode = scanner.nextLine().trim();

        System.out.print("상품명: ");
        String name = scanner.nextLine().trim();

        System.out.print("카테고리: ");
        String category = scanner.nextLine().trim();

        BigDecimal price = BigDecimal.valueOf(readInt("가격: "));
        BigDecimal cost = BigDecimal.valueOf(readInt("원가: "));
        int stock = readInt("재고: ");
        int minStock = readInt("최소재고: ");

        Product product = new Product();
        product.setBarcode(barcode);
        product.setName(name);
        product.setCategory(category);
        product.setPrice(price);
        product.setCost(cost);
        product.setStock(stock);
        product.setMinStock(minStock);

        boolean result = service.addProduct(product);

        if (result) {
            System.out.println("상품 등록 성공");
        } else {
            System.out.println("상품 등록 실패");
        }
    }

    // 7. 상품 수정
    private void updateProduct() throws SQLException {
        System.out.println("\n=== 상품 수정 ===");

        System.out.print("수정할 상품 바코드: ");
        String barcode = scanner.nextLine().trim();

        Product product = service.findByBarcode(barcode);

        if (product == null) {
            System.out.println("해당 상품을 찾을 수 없습니다.");
            return;
        }

        System.out.print("새 상품명(" + product.getName() + "): ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) {
            product.setName(name);
        }

        System.out.print("새 카테고리(" + product.getCategory() + "): ");
        String category = scanner.nextLine().trim();
        if (!category.isEmpty()) {
            product.setCategory(category);
        }

        String input;

        System.out.print("새 가격(" + product.getPrice() + "): ");
        input = scanner.nextLine().trim();
        if (!input.isEmpty()) {
            product.setPrice(new BigDecimal(input));
        }

        System.out.print("새 원가(" + product.getCost() + "): ");
        input = scanner.nextLine().trim();
        if (!input.isEmpty()) {
            product.setCost(new BigDecimal(input));
        }

        System.out.print("새 재고(" + product.getStock() + "): ");
        input = scanner.nextLine().trim();
        if (!input.isEmpty()) {
            product.setStock(Integer.parseInt(input));
        }

        System.out.print("새 최소재고(" + product.getMinStock() + "): ");
        input = scanner.nextLine().trim();
        if (!input.isEmpty()) {
            product.setMinStock(Integer.parseInt(input));
        }

        boolean result = service.updateProduct(product);

        if (result) {
            System.out.println("상품 수정 성공");
        } else {
            System.out.println("상품 수정 실패");
        }
    }

    // 8. 상품 소프트 삭제
    private void deleteProduct() throws SQLException {
        System.out.println("\n=== 상품 소프트 삭제 ===");
        int productId = readInt("삭제할 상품 ID: ");

        boolean result = service.deleteProduct(productId);

        if (result) {
            System.out.println("상품 삭제 성공");
        } else {
            System.out.println("상품 삭제 실패");
        }
    }

    // 9. 재고 부족 알림
    private void findLowStock() throws SQLException {
        List<Product> lowStockList = service.getLowStockProducts();

        System.out.println("\n=== 재고 부족 상품 목록 ===");

        if (lowStockList.isEmpty()) {
            System.out.println("재고 부족 상품이 없습니다.");
            return;
        }

        for (Product p : lowStockList) {
            System.out.println("상품명: " + p.getName()
                    + " | 바코드: " + p.getBarcode()
                    + " | 재고: " + p.getStock()
                    + " | 최소재고: " + p.getMinStock());
        }
    }

    // 10. 종료
    private void exit() {
        System.out.println("프로그램을 종료합니다.");
        scanner.close();
    }

    private int readInt(String msg) {
        while (true) {
            try {
                System.out.print(msg);
                int value = Integer.parseInt(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("숫자만 입력하세요.");
            }
        }
    }
}
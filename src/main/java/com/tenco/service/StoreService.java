package com.tenco.service;

import com.tenco.dao.AdminDAO;
import com.tenco.dao.ProductDAO;
import com.tenco.dao.SalesDAO;
import com.tenco.dto.Admin;
import com.tenco.dto.Product;
import com.tenco.dto.Sales;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.IllformedLocaleException;
import java.util.List;

/**
 * 1. 서비스는 비즈니스 로직(업무 규칙)을 담당하는 중간 관리자이다.
 * - 서비스는 뷰에서는 받은 요청을 검증하고, 필요한 DAO를 호출하여 결과를 돌려주는 것이 목적
 * - 재고보다 많이 팔 수 없다는 규칙은 서비스에서 검증한다.
 *
 */
public class StoreService {
    private final ProductDAO productDAO = new ProductDAO();
    private final AdminDAO adminDAO = new AdminDAO();
    private final SalesDAO salesDAO = new SalesDAO();

    private Integer currentAdminId = null; // 로그인 중인 관리자 ID
    private String currentAdminName = null; // 로그인 중인 관리자 이름

    // 로그인 (인증 후 상태 저장)
    public boolean login(String adminId, String password) throws SQLException {
        if (adminId == null || adminId.trim().isEmpty()) {
            throw new SQLException("관리자 ID를 입력하세요.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new SQLException("관리자 password를 입력하세요.");
        }
        Admin admin = adminDAO.login(adminId, password);

        if (admin == null) {
            return false;
        }

        currentAdminId = admin.getId();
        currentAdminName = admin.getName();

        return true;
    }

    // 로그아웃
    public void logout() {
        currentAdminId = null;
        currentAdminName = null;
    }

    // 로그인 상태 확인
    public boolean isLoggedIn() {
        return currentAdminId != null;
    }

    // 상품 목록
    public List<Product> getProductList() throws SQLException {
        return productDAO.findAll();
    }

    // 판매 처리 (결과 메세지 반환)

    // 서비스에서는 단순 작업도 있음 (단순 DAO 위임)
    // 즉, 단순 조회는 DAO 메서드를 바로 호출하여 반환한다.
    // 별도 검증이 필요 없는 경우들

    // 판매 처리는 서비스에 역할을 가장 잘 보여준다.
    // 검증 -> 실행 -> 결과 반환 최소 3단계를 만들어야 하나의 서비스가 된다.
    // 1. 검증: 상품이 실제 존재하는지 확인 (SELECT) -> productDAO.findByBarcode()에 위임
    //           - 상품 확인, 재고 확인
    // 2. 실행: SalesDAO.processSale() --> 내부 트랜잭션 처리 완료
    // 3. 결과 반환: 결과에 따른 메세지 가공해서 뷰로 전달
    public String processSale(String barcode, int quantity) throws SQLException {
        // 바코드 존재 여부
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new SQLException("바코드가 존재하지 않습니다.");
        }

        if (quantity <= 0) {
            throw new SQLException("수량은 1개 이상이어야 합니다.");
        }

        // 1단계: 상품 존재 여부 확인
        // 뽑은 Product 객체로 2번 확인 가능
        List<Product> productList = getProductList();
        Product foundProduct = null;

        for (Product product : productList) {
            if (product.getBarcode().equals(barcode)) {
                foundProduct = product;
                break;
            }
        }
        if (foundProduct == null) {
            throw new SQLException("해당 상품이 존재하지 않습니다.");
        }


        // 2단계: 재고 충분 여부 확인 (비즈니스)
        if (foundProduct.getStock() < quantity) {
            throw new SQLException("재고가 부족합니다.");
        }

        // 3단계: DAO 판매 실행 위임
        // 트랜잭션 여부에 따라서 성공 실패 처리
        boolean result = salesDAO.processSale(foundProduct, quantity);

        // 4단계: 성공 메세지 생성해서 리턴
        if (result) {
            return "판매 성공";
        } else {
            return "판매 실패";
        }
    }

    // 재고 부족 판단
    public boolean isLowStock(Product product) {
        return product.getStock() <= product.getMinStock();
    }

    // 유통기한 임박 판단
    public boolean isNearExpiry(Product product) {
        if (product.getExpireDate() == null) return false;
        return !product.getExpireDate().isAfter(LocalDate.now().plusDays(3));
    }

    // 현재 관리자 이름 반환
    public String getCurrentAdminName() {
        if (currentAdminName == null) {
            return "로그인이 되지 않았습니다.";
        }
        return currentAdminName;
    }

    // 바코드로 상품 1건 조회
    public Product findByBarcode(String barcode) throws SQLException {
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new IllegalArgumentException("바코드는 필수 입력 항목입니다.");
        }
        return productDAO.findByBarcode(barcode);
    }

    // 상품 등록 (DAO 위임)
    public boolean addProduct(Product product) throws SQLException {
        // 유효성 검사
        if (product.getBarcode() == null || product.getBarcode().trim().isEmpty()) {
            throw new SQLException("바코드는 필수 입력 항목입니다.");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new SQLException("상품명은 필수 입력 항목입니다.");
        }
        if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
            throw new SQLException("카테고리는 필수 입력 항목입니다.");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SQLException("가격은 0보다 커야 합니다.");
        }
        if (product.getCost() == null || product.getCost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SQLException("원가는 0보다 커야 합니다.");
        }
        return productDAO.insert(product);
    }

    // 상품 수정 (DAO 위임)
    public boolean updateProduct(Product product) throws SQLException {

        return productDAO.update(product);
    }

    // 상품 소프트 삭제 (DAO 위임)
    public boolean deleteProduct(int productId) throws SQLException {
        return productDAO.delete(productId);
    }

    // 재고 부족 상품 목록
    public List<Product> getLowStockProducts() throws SQLException {
        return productDAO.findLowStock();
    }

    // 오늘 매출 조회 (DAO 위임)
    public List<Sales> getTodaySales() throws SQLException {
        return salesDAO.findTodaySales();
    }
}

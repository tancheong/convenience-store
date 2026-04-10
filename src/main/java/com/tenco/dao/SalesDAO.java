package com.tenco.dao;

import com.tenco.dto.Product;
import com.tenco.dto.Sales;
import com.tenco.util.DBConnectionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SalesDAO {
    // 판매처리
    public boolean processSale(Product product, int quantity) throws SQLException {
        Connection conn = null;

        if (quantity <= 0) {
            throw new SQLException("수량은 1개 이상이어야 합니다.");
        }

        try {
            conn = DBConnectionManager.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            BigDecimal currentPrice = null;

            // 현재 상품 가격 조회
            String selectSql = """
                    SELECT price FROM product WHERE id = ?
                    """;
            try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                pstmt.setInt(1, product.getId());

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        currentPrice = rs.getBigDecimal("price");
                    } else {
                        throw new SQLException("상품이 존재하지 않습니다.");
                    }
                }
            }

            // 재고 차감
            String decreaseSql = """
                    UPDATE product
                    SET stock = stock - ?
                    WHERE id = ?
                    AND stock >= ?
                    """;
            try (PreparedStatement decreasePstmt = conn.prepareStatement(decreaseSql)) {
                decreasePstmt.setInt(1, quantity);
                decreasePstmt.setInt(2, product.getId());
                decreasePstmt.setInt(3, quantity);

                int stockResult = decreasePstmt.executeUpdate();
                if (stockResult == 0) {
                    throw new SQLException("재고 부족 또는 상품이 존재하지 않습니다.");
                }
            }

            // 판매 기록 저장
            String salesSql = """
                    INSERT INTO sales(product_id, quantity, unit_price, sold_at)
                    VALUES(?, ?, ?, ?)
                    """;
            try (PreparedStatement salesPstmt = conn.prepareStatement(salesSql)) {
                salesPstmt.setInt(1, product.getId());
                salesPstmt.setInt(2, quantity);
                salesPstmt.setBigDecimal(3, currentPrice);
                salesPstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

                int salesResult = salesPstmt.executeUpdate();
                if (salesResult == 0) {
                    throw new SQLException("판매 기록 저장 실패");
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("오류 발생: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // 오늘 매출 집계
    public List<Sales> findTodaySales() throws SQLException {
        List<Sales> salesList = new ArrayList<>();

        String totalSalesSql = """
                SELECT
                    p.name AS product_name,
                    SUM(s.quantity * s.unit_price) AS total_sales
                FROM sales s
                JOIN product p ON s.product_id = p.id
                WHERE s.sold_at >= CURDATE()
                  AND s.sold_at < CURDATE() + INTERVAL 1 DAY
                GROUP BY p.id, p.name
                """;
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement totalSalesPstmt = conn.prepareStatement(totalSalesSql);
             ResultSet rs = totalSalesPstmt.executeQuery()) {

            while (rs.next()) {
                salesList.add(Sales.builder()
                        .productName(rs.getString("product_name"))
                        .totalSales(rs.getBigDecimal("total_sales"))
                        .build());
            }
        }
        return salesList;
    }
}

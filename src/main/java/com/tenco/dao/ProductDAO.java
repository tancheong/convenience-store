package com.tenco.dao;

import com.tenco.dto.Product;
import com.tenco.util.DBConnectionManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private Product mapToProduct(ResultSet rs) throws SQLException {
        return Product.builder()
                .id(rs.getInt("id"))
                .barcode(rs.getString("barcode"))
                .name(rs.getString("name"))
                .category(rs.getString("category"))
                .price(rs.getBigDecimal("price"))
                .cost(rs.getBigDecimal("cost"))
                .stock(rs.getInt("stock"))
                .minStock(rs.getInt("min_stock"))
                .expireDate(rs.getDate("expire_date") != null
                        ? rs.getDate("expire_date").toLocalDate()
                        : null)
                .isActive(rs.getBoolean("is_active"))
                .build();
    }

    // 상품 전체 목록 조회
    public List<Product> findAll() throws SQLException {
        List<Product> productList = new ArrayList<>();
        String sql = """
                SELECT * FROM product ORDER BY id
                """;
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                productList.add(mapToProduct(rs));
            }
            return productList;
        }
    }

    // 바코드로 상품 조회
    public Product findByBarcode(String barcode) throws SQLException {
        String sql = """
                SELECT * FROM product WHERE barcode = ?
                """;
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, barcode);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToProduct(rs);
                }
            }
        }
        return null;
    }

    // 상품 등록
    public boolean insert(Product product) throws SQLException {
        String sql = """
                INSERT INTO product(barcode, name, category, price, cost, stock, expire_date)
                VALUES(?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getBarcode());
            pstmt.setString(2, product.getName());
            pstmt.setString(3, product.getCategory());
            pstmt.setBigDecimal(4, product.getPrice());
            pstmt.setBigDecimal(5, product.getCost());
            pstmt.setInt(6, product.getStock());
            pstmt.setDate(7, Date.valueOf(product.getExpireDate()));

            int rows = pstmt.executeUpdate();

            return rows > 0;
        }
    }

    // 상품 수정
    public boolean update(Product product) throws SQLException {
        String sql = """
                UPDATE product
                SET price = ?,
                    cost = ?,
                    stock = ?,
                    min_stock = ?,
                    expire_date = ?
                WHERE id = ?
                """;
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, product.getPrice());
            pstmt.setBigDecimal(2, product.getCost());
            pstmt.setInt(3, product.getStock());
            pstmt.setInt(4, product.getMinStock());
            pstmt.setDate(5, Date.valueOf(product.getExpireDate()));
            pstmt.setString(6, product.getName());

            int rows = pstmt.executeUpdate();

            return rows > 0;
        }
    }

    // 소프트 삭제
    public boolean delete(int productId) throws SQLException {
        String sql = """
                UPDATE product SET is_active = false WHERE id = ?
                """;
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            int rows = pstmt.executeUpdate();

            return rows > 0;
        }
    }

    // 재고 부족 상품 조회
    public List<Product> findLowStock() throws SQLException {
        List<Product> productList = new ArrayList<>();
        String sql = """
                SELECT * FROM product WHERE stock < min_stock
                """;
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                productList.add(mapToProduct(rs));
            }
        }
        return productList;
    }
}

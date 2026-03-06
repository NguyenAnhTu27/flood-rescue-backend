-- ============================================
-- CÁC CÂU LỆNH KIỂM TRA TỒN KHO
-- ============================================

-- 1. XEM TẤT CẢ TỒN KHO THEO TỪNG MẶT HÀNG VÀ NGUỒN
SELECT 
    ic.id AS item_id,
    ic.code AS item_code,
    ic.name AS item_name,
    sb.source_type,
    sb.qty AS stock_qty,
    ic.unit,
    sb.updated_at
FROM stock_balances sb
INNER JOIN item_categories ic ON sb.item_category_id = ic.id
ORDER BY ic.name, sb.source_type;

-- 2. TỔNG TỒN KHO CỦA TẤT CẢ MẶT HÀNG (GỘP THEO MẶT HÀNG)
SELECT 
    ic.id AS item_id,
    ic.code AS item_code,
    ic.name AS item_name,
    SUM(sb.qty) AS total_stock,
    sb.unit,
    GROUP_CONCAT(DISTINCT sb.source_type) AS source_types
FROM stock_balances sb
INNER JOIN item_categories ic ON sb.item_category_id = ic.id
GROUP BY ic.id, ic.code, ic.name, sb.unit
ORDER BY ic.name;

-- 3. TỒN KHO CHI TIẾT THEO TỪNG NGUỒN (DONATION vs PURCHASE)
SELECT 
    ic.id AS item_id,
    ic.code AS item_code,
    ic.name AS item_name,
    sb.source_type,
    sb.qty AS stock_qty,
    ic.unit
FROM stock_balances sb
INNER JOIN item_categories ic ON sb.item_category_id = ic.id
ORDER BY ic.name, sb.source_type;

-- 4. TỒN KHO THEO NGUỒN (TỔNG HỢP)
SELECT 
    sb.source_type,
    COUNT(DISTINCT sb.item_category_id) AS number_of_items,
    SUM(sb.qty) AS total_qty
FROM stock_balances sb
GROUP BY sb.source_type;

-- 5. MẶT HÀNG KHÔNG CÓ TỒN KHO (CHỈ HIỂN THỊ MẶT HÀNG ĐÃ CÓ TRONG DB)
SELECT 
    ic.id,
    ic.code,
    ic.name,
    'KHÔNG CÓ TỒN KHO' AS status
FROM item_categories ic
LEFT JOIN stock_balances sb ON ic.id = sb.item_category_id
WHERE sb.id IS NULL
ORDER BY ic.name;

-- 6. MẶT HÀNG CÓ TỒN KHO = 0
SELECT 
    ic.id AS item_id,
    ic.code AS item_code,
    ic.name AS item_name,
    sb.source_type,
    sb.qty AS stock_qty,
    ic.unit
FROM stock_balances sb
INNER JOIN item_categories ic ON sb.item_category_id = ic.id
WHERE sb.qty = 0 OR sb.qty IS NULL
ORDER BY ic.name;

-- 7. TOP 10 MẶT HÀNG CÓ TỒN KHO NHIỀU NHẤT
SELECT 
    ic.id AS item_id,
    ic.code AS item_code,
    ic.name AS item_name,
    SUM(sb.qty) AS total_stock,
    ic.unit
FROM stock_balances sb
INNER JOIN item_categories ic ON sb.item_category_id = ic.id
GROUP BY ic.id, ic.code, ic.name, ic.unit
ORDER BY total_stock DESC
LIMIT 10;

-- 8. TỒN KHO THEO NGUỒN DONATION
SELECT 
    ic.id AS item_id,
    ic.code AS item_code,
    ic.name AS item_name,
    sb.qty AS stock_qty,
    ic.unit
FROM stock_balances sb
INNER JOIN item_categories ic ON sb.item_category_id = ic.id
WHERE sb.source_type = 'DONATION'
ORDER BY ic.name;

-- 9. TỒN KHO THEO NGUỒN PURCHASE
SELECT 
    ic.id AS item_id,
    ic.code AS item_code,
    ic.name AS item_name,
    sb.qty AS stock_qty,
    ic.unit
FROM stock_balances sb
INNER JOIN item_categories ic ON sb.item_category_id = ic.id
WHERE sb.source_type = 'PURCHASE'
ORDER BY ic.name;

-- 10. TỔNG HỢP ĐẦY ĐỦ: TẤT CẢ MẶT HÀNG + TỒN KHO (KỂ CẢ MẶT HÀNG KHÔNG CÓ TỒN KHO)
SELECT 
    ic.id AS item_id,
    ic.code AS item_code,
    ic.name AS item_name,
    COALESCE(SUM(sb.qty), 0) AS total_stock,
    ic.unit,
    CASE 
        WHEN SUM(sb.qty) IS NULL THEN 'KHÔNG CÓ TỒN KHO'
        WHEN SUM(sb.qty) = 0 THEN 'HẾT HÀNG'
        ELSE 'CÒN HÀNG'
    END AS status
FROM item_categories ic
LEFT JOIN stock_balances sb ON ic.id = sb.item_category_id
GROUP BY ic.id, ic.code, ic.name, ic.unit
ORDER BY ic.name;

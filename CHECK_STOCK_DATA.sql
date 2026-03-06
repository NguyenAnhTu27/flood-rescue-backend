-- ============================================
-- KIỂM TRA DỮ LIỆU TỒN KHO
-- ============================================

-- 1. KIỂM TRA XEM CÓ DỮ LIỆU TRONG stock_balances KHÔNG
SELECT COUNT(*) AS total_stock_records FROM stock_balances;

-- 2. KIỂM TRA XEM CÓ DỮ LIỆU TRONG item_categories KHÔNG
SELECT COUNT(*) AS total_items FROM item_categories;

-- 3. XEM TẤT CẢ MẶT HÀNG (KỂ CẢ KHÔNG CÓ TỒN KHO) - DÙNG LEFT JOIN
SELECT 
    ic.id AS item_id,
    ic.code AS item_code,
    ic.name AS item_name,
    COALESCE(SUM(sb.qty), 0) AS total_stock,
    ic.unit,
    CASE 
        WHEN SUM(sb.qty) IS NULL OR SUM(sb.qty) = 0 THEN 'KHÔNG CÓ TỒN KHO'
        ELSE 'CÒN HÀNG'
    END AS status
FROM item_categories ic
LEFT JOIN stock_balances sb ON ic.id = sb.item_category_id
GROUP BY ic.id, ic.code, ic.name, ic.unit
ORDER BY ic.name;

-- 4. CHI TIẾT TỒN KHO THEO TỪNG NGUỒN (NẾU CÓ)
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

-- 5. XEM TẤT CẢ MẶT HÀNG TRONG DB
SELECT 
    id,
    code,
    name,
    unit,
    is_active
FROM item_categories
ORDER BY name;

-- 6. XEM TẤT CẢ PHIẾU NHẬP ĐÃ ĐƯỢC DUYỆT (APPROVED)
SELECT 
    id,
    code,
    status,
    source_type,
    created_at
FROM inventory_receipts
WHERE status = 'APPROVED'
ORDER BY created_at DESC;

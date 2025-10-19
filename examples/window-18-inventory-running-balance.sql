-- Inventory running balance with stock movements
SELECT
    movement_date,
    warehouse_id,
    product_id,
    movement_type,
    quantity,
    CASE
        WHEN movement_type IN ('purchase', 'return', 'adjustment_in') THEN quantity
        WHEN movement_type IN ('sale', 'damage', 'adjustment_out') THEN -quantity
        ELSE 0
    END AS quantity_change,
    -- Running inventory balance
    SUM(
        CASE
            WHEN movement_type IN ('purchase', 'return', 'adjustment_in') THEN quantity
            WHEN movement_type IN ('sale', 'damage', 'adjustment_out') THEN -quantity
            ELSE 0
        END
    ) OVER (
        PARTITION BY warehouse_id, product_id
        ORDER BY movement_date, movement_id
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    ) AS current_stock_level,
    -- Stock statistics
    AVG(quantity) OVER (
        PARTITION BY warehouse_id, product_id
        ORDER BY movement_date
        ROWS BETWEEN 29 PRECEDING AND CURRENT ROW
    ) AS avg_movement_30days,
    MIN(
        SUM(
            CASE
                WHEN movement_type IN ('purchase', 'return', 'adjustment_in') THEN quantity
                WHEN movement_type IN ('sale', 'damage', 'adjustment_out') THEN -quantity
                ELSE 0
            END
        ) OVER (
            PARTITION BY warehouse_id, product_id
            ORDER BY movement_date, movement_id
            ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
        )
    ) OVER (
        PARTITION BY warehouse_id, product_id
        ORDER BY movement_date
        ROWS BETWEEN 89 PRECEDING AND CURRENT ROW
    ) AS min_stock_90days,
    MAX(
        SUM(
            CASE
                WHEN movement_type IN ('purchase', 'return', 'adjustment_in') THEN quantity
                WHEN movement_type IN ('sale', 'damage', 'adjustment_out') THEN -quantity
                ELSE 0
            END
        ) OVER (
            PARTITION BY warehouse_id, product_id
            ORDER BY movement_date, movement_id
            ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
        )
    ) OVER (
        PARTITION BY warehouse_id, product_id
        ORDER BY movement_date
        ROWS BETWEEN 89 PRECEDING AND CURRENT ROW
    ) AS max_stock_90days,
    -- Days since last restock
    EXTRACT(DAY FROM movement_date - LAG(movement_date) FILTER (WHERE movement_type = 'purchase') OVER (
        PARTITION BY warehouse_id, product_id
        ORDER BY movement_date
    )) AS days_since_last_purchase,
    -- Stock movement velocity
    COUNT(*) FILTER (WHERE movement_type IN ('sale', 'damage', 'adjustment_out')) OVER (
        PARTITION BY warehouse_id, product_id
        ORDER BY movement_date
        ROWS BETWEEN 6 PRECEDING AND CURRENT ROW
    ) AS outbound_movements_7days
FROM inventory_movements
WHERE movement_date >= '2024-01-01'
ORDER BY warehouse_id, product_id, movement_date, movement_id

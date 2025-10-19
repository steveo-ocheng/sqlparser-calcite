-- Moving averages with different window sizes
SELECT
    trade_date,
    stock_symbol,
    closing_price,
    AVG(closing_price) OVER (PARTITION BY stock_symbol ORDER BY trade_date ROWS BETWEEN 6 PRECEDING AND CURRENT ROW) AS ma_7day,
    AVG(closing_price) OVER (PARTITION BY stock_symbol ORDER BY trade_date ROWS BETWEEN 29 PRECEDING AND CURRENT ROW) AS ma_30day,
    AVG(closing_price) OVER (PARTITION BY stock_symbol ORDER BY trade_date ROWS BETWEEN 89 PRECEDING AND CURRENT ROW) AS ma_90day,
    AVG(closing_price) OVER (PARTITION BY stock_symbol ORDER BY trade_date ROWS BETWEEN 199 PRECEDING AND CURRENT ROW) AS ma_200day,
    SUM(volume) OVER (PARTITION BY stock_symbol ORDER BY trade_date ROWS BETWEEN 6 PRECEDING AND CURRENT ROW) AS volume_7day,
    STDDEV(closing_price) OVER (PARTITION BY stock_symbol ORDER BY trade_date ROWS BETWEEN 29 PRECEDING AND CURRENT ROW) AS volatility_30day
FROM stock_prices
WHERE trade_date >= '2023-01-01'
ORDER BY stock_symbol, trade_date DESC
LIMIT 1000

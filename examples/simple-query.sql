SELECT id, name, email, created_at
FROM users
WHERE age > 18 AND status = 'active'
ORDER BY created_at DESC
LIMIT 100

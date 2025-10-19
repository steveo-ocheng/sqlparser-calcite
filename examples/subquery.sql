SELECT
    employee_id,
    first_name,
    last_name,
    salary,
    department
FROM employees
WHERE salary > (
    SELECT AVG(salary)
    FROM employees
    WHERE department = 'Engineering'
)
AND department IN ('Engineering', 'Product', 'Design')
ORDER BY salary DESC
LIMIT 20

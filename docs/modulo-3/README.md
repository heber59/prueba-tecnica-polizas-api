# Módulo 3: Optimización de base de datos

## 1. Crear índices para el filtro y la relación

Evaluaría estos índices si no existe uno equivalente:

```sql
CREATE INDEX ix_customers_country_id
    ON customers (country, customer_id);

CREATE INDEX ix_orders_customer_id
    ON orders (customer_id);
```

El primero permite localizar clientes por país y obtener sus identificadores. El segundo permite buscar sus pedidos sin recorrer necesariamente toda la tabla `orders`.

## 2. Actualizar estadísticas y revisar el plan real

```sql
BEGIN
    DBMS_STATS.GATHER_TABLE_STATS(
        ownname          => USER,
        tabname          => 'CUSTOMERS',
        estimate_percent => DBMS_STATS.AUTO_SAMPLE_SIZE,
        method_opt       => 'FOR ALL COLUMNS SIZE AUTO',
        cascade          => TRUE
    );

    DBMS_STATS.GATHER_TABLE_STATS(
        ownname          => USER,
        tabname          => 'ORDERS',
        estimate_percent => DBMS_STATS.AUTO_SAMPLE_SIZE,
        method_opt       => 'FOR ALL COLUMNS SIZE AUTO',
        cascade          => TRUE
    );
END;
/
```

```sql
SELECT *
FROM TABLE(DBMS_XPLAN.DISPLAY_CURSOR(NULL, NULL, 'ALLSTATS LAST'));
```

## 3. Materializar el resultado si se tolera un desfase

no necesita reflejar cada cambio inmediatamente.

```sql
CREATE MATERIALIZED VIEW mv_orders_mexico
BUILD IMMEDIATE
REFRESH COMPLETE ON DEMAND
AS
SELECT o.order_id,
       o.order_date,
       c.customer_name,
       o.total_amount
FROM orders o
JOIN customers c ON o.customer_id = c.customer_id
WHERE c.country = 'México';
```

El reporte consultaría explícitamente la vista:

```sql
SELECT order_id, order_date, customer_name, total_amount
FROM mv_orders_mexico;
```

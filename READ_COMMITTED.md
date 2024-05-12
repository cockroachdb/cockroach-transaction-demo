### 1. Configure the cluster to support `READ COMMITTED` transactions
===

- In **SQL Shell 1** enable `READ COMMITTED` transactions.

```sql
SET CLUSTER SETTING sql.txn.read_committed_isolation.enabled = 'true';
```

> [!NOTE] This enables `READ COMMITTED` isolation level in the entire cluster, making it available in any shell. `READ COMMITTED` means that you can see committed results, even if you are the middle of a transaction.  However, this setting above doesn't automatically set this as the default isolation level. `READ COMMITTED` can be applied to individual transactions or applied to entire sessions.

By the way, CockroachDB defaults to `SERIALIZABLE` isolation level, which is why we have made this alteration.
The purpose of this exercise is to raise awareness of deviating from Cockroach's defaults.

To understand what you'll be doing in this exercise, check out the diagram below:

![Read committed transactions](../assets/exercise-01/01-read-committed-competing-transaction.svg)

This diagram depicts a user, Alice, attempting to purchase socks during a flash sale.

- Alice starts a transaction.
- She checks the `quantity` of the item to see if there are enough, and there are.
- For whatever reason, her transaction is delayed. Perhaps the user must click on a "confirmation" button, or the node she's going through is overloaded?
- While Alice's transaction is held up, Bob starts his own transaction.
- He also checks the `quantity` of the same item. Because nothing has changed, he also sees it as sufficient.
- Bob issues an update to reduce `quantity` by `190`, taking the total down to `10`. He commits his transaction.
- Because Alice didn't use any form of locking during her `SELECT` (e.g. `SELECT FOR UPDATE` which we'll cover in later sections of the course), Bob's update is allowed to be committed.
- Alice submits her own `UPDATE` to reduce the `quantity` by `20`. Remember, Alice checked the balance *before* Bob committed his reduction. She thinks there is enough.
- Because this is `READ COMMITTED` and Bob's `UPDATE` has already been committed, Alice's transaction won't detect any contention.
- Her `COMMIT` is allowed to go through, causing the balance to go down to `-10`, despite having checked in advance.

Let's reproduce this **isolation anomaly** between these competing transactions in the following steps, using a preloaded database a table of `items` and a column of `quantity`.

> [!NOTE] See [reference documentation](https://www.cockroachlabs.com/docs/stable/read-committed) for more detail.

### 2. Begin a transaction
===

- In the same SQL shell, begin a new transaction:

```sql
BEGIN TRANSACTION;
```

- Adjust the transaction level of the current transaction to `READ COMMITTED`:

```sql
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
```

- As shown in the diagram above, check the quantity of item `foo`:

```sql
SELECT quantity FROM items WHERE name = 'foo';
```

What are the quantity?

Before you attempt to update the quantity in this transaction, imagine another process launching a similar transaction that competes with this one by attempting to update the quantity of the same item.

### 3. Start a competing transaction
===

- In **SQL Shell 2**, begin a transaction:

```sql
BEGIN TRANSACTION;
```

- Adjust the transaction level of this transaction to `READ COMMITTED`:

```sql
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
```

- Check the quantity of item `foo`:

```sql
SELECT quantity FROM items WHERE name = 'foo';
```

Is the quantity at least `190`?
Since the other transaction didn't use `SELECT FOR UPDATE` nor started an `UPDATE` against item `foo`, you are able to continue with this transaction.

- Reduce the quantity of `foo` by `190`:

```sql
UPDATE items SET quantity = quantity - 190 WHERE name = 'foo';
```

- Commit the transaction that reduced the quantity by `190`:

```sql
COMMIT;
```

- Check the quantity of item `foo`:

```sql
SELECT quantity FROM items WHERE name = 'foo';
```

What is the quantity?
Is it what you expected?

### 4. Continue with the original transaction
===

In the previous action in **SQL Shell 1**, you checked the quantity.
It reported back `200` which appears sufficient for the original transaction.
Given that, proceed with the reduction by `20`.

- Back in **SQL Shell 1** , reduce the quantity of item `foo` by `20`:

```sql
UPDATE items SET quantity = quantity - 20 WHERE name = 'foo';
```

- Commit the current transaction:

```sql
COMMIT;
```

- Check the quantity of item `foo`:

```sql
SELECT quantity FROM items WHERE name = 'foo';
```

How many are there?
Is it less than `0`?
What caused that to happen?

This is what's known as an **isolation anomaly**.
Both `READ COMMITTED` (the isolation level in this exercise) as well as `SERIALIZABLE` (Cockroach's default isolation level) would have shown you the same value, `200`, during the `SELECT`.
However, it's the `READ COMMITTED` isolation level that allowed you to write to a row that had been updated by a different transaction. 

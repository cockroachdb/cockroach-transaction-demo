### 1. Check the cluster's default isolation level
===

- In [button label="SQL Shell 1" background="#6935ff"](tab-0) check on the default isolation level:

```sql
SHOW default_transaction_isolation;
```

CockroachDB defaults to `SERIALIZABLE` isolation.
Instead of letting anomalies through, it instead forces transactions to run as if they had happened one after the other.
Anything that contradicts this will force an error.

Check out the diagram below:

![Serializable transactions](../assets/exercise-02/02-serializable-competing-transaction.svg)

- Alice starts a transaction.
- She checks the `qty` of the item to see if there is enough. There is.
- For whatever reason, her transaction is delayed. Perhaps the user must click on a "confirmation" button, or the server hits a peak?
- While Alice's transaction is held up, Bob starts his own transaction.
- He also checks the `qty` of the same item. Because nothing has changed, he also sees it as sufficient.
- Bob issues an update to reduce `qty` by `190`, taking the total down to `10`. He commits his transaction.
- Because Alice didn't use any form of locking during her `SELECT` (e.g. `SELECT FOR UPDATE`), Bob's update is allowed to go through.
- Alice submits her own `UPDATE` to reduce the `qty` by `20`. Remember, Alice checked the balance *before* Bob committed his reduction. She thinks there is enough.
- Normally this would reduce the `qty` to `-10`. However, with a `SERIALIZABLE` transaction, a timestamp was collected when the transaction began.
- Because Bob's transaction has a later timestamp, Alice's attempt to `COMMIT` generates a `WriteTooOld` error, fails, and rolls back.
- This prevents Alice from taking the balance down to `-10`.

Let's attempt to reproduce these competing transactions in the following steps.

(See [reference documentation](https://www.cockroachlabs.com/docs/stable/demo-serializable) for more details about `SERIALIZABLE`.)

### 2. Begin a transaction
===

- In the [button label="same SQL shell" background="#6935ff"](tab-0), begin a new transaction:

```sql
BEGIN TRANSACTION;
```

Before you attempt to update the quantity in this transaction, imagine another process launching a similar transaction that competes with this one by attempting to update the quantity of the same item.

### 3. Start a competing transaction
===

- In [button label="SQL Shell 2" background="#6935ff"](tab-1), begin a transaction:

```sql
BEGIN TRANSACTION;
```

- Check the quantity of item `foo`:

```sql
SELECT quantity FROM items WHERE name = 'foo';
```

Is the quantity at least `190`?

- Reduce the quantity of `foo` by `190`:

```sql
UPDATE items SET quantity = quantity - 190 WHERE name = 'foo';
```

### 4. Check the quantity in the original transaction
===

- Back in [button label="SQL Shell 1" background="#6935ff"](tab-0), check out the quantity of item `foo`:

```sql
SELECT quantity FROM items WHERE name = 'foo';
```

What is the quantity?
Notice how it doesn't reflect the `UPDATE` just done?

### 5. Complete the competing transaction
===

- Over in [button label="SQL Shell 2" background="#6935ff"](tab-1), commit the transaction the reduced the quantity by `190`:

```sql
COMMIT;
```

- Check the quantity of item `foo`:

```sql
SELECT quantity FROM items WHERE name = 'foo';
```

What is the quantity?
Is it what you expected?

### 6. Continue with the original transaction
===

In the previous action in [button label="SQL Shell 1" background="#6935ff"](tab-0), you checked the quantity.
It reported back `200` which appears sufficient for the original transaction.
Given that, proceed with the reduction by `20`.

- Back in [button label="SQL Shell 1" background="#6935ff"](tab-0), reduce the quantity of item `foo` by `20`:

```sql
UPDATE items SET quantity = quantity - 20 WHERE name = 'foo';
```

What happened?
What type of error was produced?
Was it a `WriteTooOldError`?
What `SQLSTATE` code did it have?
(See [reference documentation](https://www.cockroachlabs.com/docs/stable/transaction-retry-error-reference) for more details about these errors.)

- Commit the current transaction:

```sql
COMMIT;
```

What happened in the SQL Shell?
Did the transaction go through or rollback?

- Check the quantity of item `foo`:

```sql
SELECT quantity FROM items WHERE name = 'foo';
```

How many are there this time?
How does this compare with the outcome of the competing transaction?
Can you imagine how to deal with this sort of error?

### HINT
===

The one way to resolve this is to retry the original transaction.
It ensures you are rechecking the quantity of the item.
Another option when doing a `SELECT` to check on a quantity right before an `UPDATE` is to leverage `SELECT FOR UPDATE`.

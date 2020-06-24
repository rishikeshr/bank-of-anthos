// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package anthos.samples.financedemo.ledgermonolith;

import java.util.LinkedList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository class for performing queries on the Transaction database
 */
@Repository
public interface TransactionRepository
    extends CrudRepository<Transaction, Long> {

    /**
     * Returns the id of the latest transaction, or -1 if no transactions exist.
     */
    @Query(value = "SELECT COALESCE("
        + "(SELECT MAX(transaction_id) FROM transactions), "
        + "CAST ('-1' AS BIGINT))",
        nativeQuery = true)
    Long latestId();

    @Query("SELECT t FROM Transaction t "
        + " WHERE (t.fromAccountNum=?1 AND t.fromRoutingNum=?2) "
        + "   OR (t.toAccountNum=?1 AND t.toRoutingNum=?2) "
        + " ORDER BY t.timestamp DESC")
    LinkedList<Transaction> findForAccount(String accountNum,
                                           String routingNum,
                                           Pageable pager);

    @Query("SELECT t FROM Transaction t "
        + " WHERE t.transactionId > ?1 ORDER BY t.transactionId ASC")
    List<Transaction> findLatest(long latestTransaction);

    @Query(value = "SELECT "
        + " (SELECT SUM(amount) FROM transactions t "
        + "     WHERE (to_acct = ?1 AND to_route = ?2)) - "
        + " (SELECT SUM(amount) FROM transactions t "
        + "     WHERE (from_acct = ?1 AND from_route = ?2))",
        nativeQuery = true)
    Long findBalance(String accountNum, String routeNum);
}